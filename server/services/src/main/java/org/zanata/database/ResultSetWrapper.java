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

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class ResultSetWrapper implements InvocationHandler {

    public static ResultSet wrap(ResultSet resultSet, Statement statementProxy,
            Connection connectionProxy, boolean streaming) {
        if (Proxy.isProxyClass(resultSet.getClass()) && Proxy
                .getInvocationHandler(resultSet) instanceof ResultSetWrapper) {
            return resultSet;
        }
        return ProxyUtil.newProxy(resultSet, new ResultSetWrapper(resultSet,
                statementProxy, connectionProxy, streaming));
    }

    private final ResultSet resultSet;
    private final Statement statementProxy;
    private final Connection connectionProxy;
    private final boolean streaming;
    private final Throwable throwable =
            new Throwable("Unclosed ResultSet was created here");

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getName().equals("getStatement") && args == null) {
            return statementProxy;
        } else if (method.getName().equals("toString") && args == null) {
            return "ResultSetWrapper->" + resultSet.toString();
        }
        try {
            Object result = method.invoke(resultSet, args);
            if (method.getName().equals("close") && args == null) {
                afterClose();
            }
            return result;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private void afterClose() {
        ConnectionWrapper connectionWrapper =
                (ConnectionWrapper) Proxy.getInvocationHandler(connectionProxy);
        if (streaming) {
            connectionWrapper.afterStreamingResultSetClosed();
        } else {
            connectionWrapper.afterResultSetClosed(throwable);
        }
    }

    private ResultSetWrapper(final ResultSet resultSet,
            final Statement statementProxy, final Connection connectionProxy,
            final boolean streaming) {
        this.resultSet = resultSet;
        this.statementProxy = statementProxy;
        this.connectionProxy = connectionProxy;
        this.streaming = streaming;
    }

    public ResultSet getResultSet() {
        return this.resultSet;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }
}
