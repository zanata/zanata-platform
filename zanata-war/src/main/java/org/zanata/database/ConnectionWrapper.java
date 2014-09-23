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
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.lang.reflect.Proxy.getInvocationHandler;
import static java.lang.reflect.Proxy.isProxyClass;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class ConnectionWrapper implements InvocationHandler {
    // For reference, this is what the mysql exception looks like:
    // Streaming result set com.mysql.jdbc.RowDataDynamic@1950740 is
    // still active. No statements may be issued when any streaming
    // result sets are open and in use on a given connection. Ensure
    // that you have called .close() on any active streaming result
    // sets before attempting more queries.
    public static final String CONCURRENT_RESULTSET =
            "Streaming ResultSet is still open on this Connection";
    private final Connection originalConnection;
    private Set<Throwable> resultSetsOpened = Sets.newHashSet();
    private Throwable streamingResultSetOpened;

    public static Connection wrap(Connection conn) {
        // avoid double-wrapping:
        if (isProxyClass(conn.getClass())
                && getInvocationHandler(conn) instanceof ConnectionWrapper) {
            return conn;
        }
        return ProxyUtil
                .newProxy(conn, new ConnectionWrapper(conn));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getName().equals("toString")) {
            return "ConnectionWrapper->" + originalConnection.toString();
        }
        if (method.getName().equals("close")) {
            if (streamingResultSetOpened != null) {
                log.error("Connection.close: streaming ResultSet still open",
                        streamingResultSetOpened);
            }
            if (!resultSetsOpened.isEmpty()) {
                log.error("Connection.close: ResultSet still open",
                        resultSetsOpened.iterator().next());
            }
        }
        try {
            Object result = method.invoke(originalConnection, args);
            if (result instanceof Statement) {
                Statement statement = (Statement) result;
                return StatementWrapper.wrap(statement, (Connection) proxy);
            }
            return result;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Notify ConnectionWrapper that Statement.execute() has been called.
     * @throws StreamingResultSetSQLException
     */
    public void executed() throws StreamingResultSetSQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        }
    }

    /**
     * Notify ConnectionWrapper that a Statement has opened a non-streaming
     * ResultSet.
     * @throws StreamingResultSetSQLException
     */
    public void resultSetOpened(Throwable throwable) throws StreamingResultSetSQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        }
        resultSetsOpened.add(throwable);
    }

    /**
     * Notify ConnectionWrapper that a Statement has opened a streaming
     * ResultSet.
     * @throws StreamingResultSetSQLException
     */
    public void streamingResultSetOpened(Throwable throwable)
            throws StreamingResultSetSQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        } else if (!resultSetsOpened.isEmpty()) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    resultSetsOpened.iterator().next());
        }
        streamingResultSetOpened = throwable;
    }

    /**
     * Notify ConnectionWrapper that a non-streaming ResultSet has been closed.
     * @throws StreamingResultSetSQLException
     */
    public void resultSetClosed(Throwable throwable) {
        resultSetsOpened.remove(throwable);
    }

    /**
     * Notify ConnectionWrapper that a streaming ResultSet has been closed.
     * @throws StreamingResultSetSQLException
     */
    public void streamingResultSetClosed() {
        streamingResultSetOpened = null;
    }
}
