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

package org.zanata.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectionWrapper implements InvocationHandler
{
   // For reference, this is what the mysql exception looks like:
   // Streaming result set com.mysql.jdbc.RowDataDynamic@1950740 is
   // still active. No statements may be issued when any streaming
   // result sets are open and in use on a given connection. Ensure
   // that you have called .close() on any active streaming result
   // sets before attempting more queries.
   public static final String CONCURRENT_RESULTSET = "Streaming ResultSet is still open on this Connection";
   private final Connection connection;
   private int resultSetsOpen;
   private boolean streamingResultSetOpen;

   public static Connection wrap(Connection connection) 
   {
      if (Proxy.isProxyClass(connection.getClass()) && Proxy.getInvocationHandler(connection) instanceof ConnectionWrapper)
      {
         return connection;
      }
      ConnectionWrapper h = new ConnectionWrapper(connection);
      ClassLoader cl = h.getClass().getClassLoader();
      return (Connection) Proxy.newProxyInstance(cl, connection.getClass().getInterfaces(), h);
   }

   /**
    * @return the connection
    */
   public Connection getConnection()
   {
      return connection;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (method.getName().equals("toString"))
      {
         return "ConnectionWrapper->"+connection.toString();
      }
      try
      {
         Object result = method.invoke(connection, args);
         if (result instanceof Statement)
         {
            Statement statement = (Statement) result;
            return StatementWrapper.wrap(statement, (Connection) proxy);
         }
         return result;
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }

   public void executed() throws SQLException
   {
      if (streamingResultSetOpen)
      {
         throw new SQLException(CONCURRENT_RESULTSET);
      }
   }

   public void resultSetOpened() throws SQLException
   {
      if (streamingResultSetOpen)
      {
         throw new SQLException(CONCURRENT_RESULTSET);
      }
      ++resultSetsOpen;
   }

   public void streamingResultSetOpened() throws SQLException
   {
      if (streamingResultSetOpen || resultSetsOpen != 0)
      {
         throw new SQLException(CONCURRENT_RESULTSET);
      }
      streamingResultSetOpen = true;
   }

   public void resultSetClosed()
   {
      --resultSetsOpen;
   }

   public void streamingResultSetClosed()
   {
      streamingResultSetOpen = false;
   }
}
