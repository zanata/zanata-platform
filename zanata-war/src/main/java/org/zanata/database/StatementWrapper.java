/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.database;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StatementWrapper implements InvocationHandler
{
   public static Statement wrap(Statement statement, Connection connection)
   {
      if (Proxy.isProxyClass(statement.getClass()) && Proxy.getInvocationHandler(statement) instanceof StatementWrapper)
      {
         return statement;
      }
      StatementWrapper h = new StatementWrapper(statement, connection);
      ClassLoader cl = h.getClass().getClassLoader();
      return (Statement) Proxy.newProxyInstance(cl, statement.getClass().getInterfaces(), h);
   }

   private final Statement statement;
   private final Connection connection;
   private boolean makeStreamingResultSet;

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (method.getName().equals("getConnection"))
      {
         return connection;
      }
      if (method.getName().equals("toString"))
      {
         return "StatementWrapper->"+statement.toString();
      }
      if (method.getName().equals("setFetchSize") && args[0].equals(Integer.MIN_VALUE))
      {
         // don't pass it to wrapped connection since it is probably not going to understand it
         makeStreamingResultSet = true;
         return null;
      }
      try
      {
         Object result = method.invoke(statement, args);
         if (result instanceof ResultSet)
         {
            ResultSet resultSet = (ResultSet) result;
            ConnectionWrapper connectionWrapper = (ConnectionWrapper) Proxy.getInvocationHandler(connection);
            ResultSet rsProxy = ResultSetWrapper.wrap(resultSet, connection, makeStreamingResultSet);
            ResultSetWrapper rsWrap = (ResultSetWrapper) Proxy.getInvocationHandler(rsProxy);
            if (makeStreamingResultSet)
            {
               connectionWrapper.streamingResultSetOpened(rsWrap.getThrowable());
            }
            else
            {
               connectionWrapper.resultSetOpened(rsWrap.getThrowable());
            }
            return rsProxy;
         }
         else if (method.getName().startsWith("execute"))
         {
            ConnectionWrapper connectionWrapper = (ConnectionWrapper) Proxy.getInvocationHandler(connection);
            connectionWrapper.executed();
         }
         return result;
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }

}
