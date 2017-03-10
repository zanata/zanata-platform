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
import java.sql.Statement;
import java.util.Set;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import javax.annotation.Nullable;
import static java.lang.reflect.Proxy.getInvocationHandler;
import static java.lang.reflect.Proxy.isProxyClass;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class ConnectionWrapper implements InvocationHandler {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ConnectionWrapper.class);
    // For reference, this is what the mysql exception looks like:
    // Streaming result set com.mysql.jdbc.RowDataDynamic@1950740 is
    // still active. No statements may be issued when any streaming
    // result sets are open and in use on a given connection. Ensure
    // that you have called .close() on any active streaming result
    // sets before attempting more queries.
    public static final String CONCURRENT_RESULTSET =
            "Streaming ResultSet is still open on this Connection";
    private final Connection originalConnection;
    private final Set<Throwable> resultSetsOpened = Sets.newHashSet();
    @Nullable
    private Throwable streamingResultSetOpened;
    @Nullable
    private Throwable firstExecuted;
    private boolean autoCommit = true;
    @VisibleForTesting
    boolean transactionActive = false;

    public static Connection wrap(Connection conn) {
        // avoid double-wrapping:
        if (isProxyClass(conn.getClass())
                && getInvocationHandler(conn) instanceof ConnectionWrapper) {
            return conn;
        }
        return ProxyUtil.newProxy(conn, new ConnectionWrapper(conn));
    }

    @VisibleForTesting
    static ConnectionWrapper getConnectionWrapper(Connection connectionProxy) {
        return (ConnectionWrapper) getInvocationHandler(connectionProxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getName().equals("toString") && args == null) {
            return "ConnectionWrapper->" + originalConnection.toString();
        }
        if (method.getName().equals("close") && args == null) {
            beforeClose();
        } else if ((method.getName().equals("commit")
                || method.getName().equals("rollback")) && args == null) {
            beforeTransactionComplete();
        } else if (method.getName().equals("setAutoCommit") && args.length == 1
                && args[0] instanceof Boolean) {
            beforeSetAutoCommit((boolean) args[0]);
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

    private void beforeTransactionComplete() {
        transactionActive = false;
        firstExecuted = null;
    }

    private void beforeSetAutoCommit(boolean autoCommit) {
        if (this.autoCommit != autoCommit) {
            this.autoCommit = autoCommit;
            // According to Connection's javadocs, changing autocommit will
            // commit the current transaction:
            beforeTransactionComplete();
        }
    }

    private void beforeClose() {
        if (streamingResultSetOpened != null) {
            log.error("Connection.close: streaming ResultSet still open",
                    streamingResultSetOpened);
        }
        if (!resultSetsOpened.isEmpty()) {
            log.error("Connection.close: ResultSet still open",
                    resultSetsOpened.iterator().next());
        }
        if (transactionActive) {
            throw new RuntimeException(
                    "Connection.close() called with transaction active. First executed statement was here:",
                    firstExecuted);
        }
        firstExecuted = null;
    }

    /**
     * Notify ConnectionWrapper that Statement.execute() has been called.
     *
     * @throws StreamingResultSetSQLException
     */
    public void afterStatementExecute() throws StreamingResultSetSQLException {
        if (!autoCommit) {
            // TODO it might be a little better to do this in
            // beforeStatementExecute
            // If we read/write the database with autoCommit off, but we
            // later forget to commit/rollback before close, we want to know
            // about it, so we track the first execute() in the transaction.
            transactionActive = true;
            if (firstExecuted == null) {
                firstExecuted = new Throwable();
            }
        }
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        }
    }

    /**
     * Notify ConnectionWrapper that a Statement has opened a non-streaming
     * ResultSet.
     *
     * @throws StreamingResultSetSQLException
     */
    public void afterResultSetOpened(Throwable throwable)
            throws StreamingResultSetSQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        }
        resultSetsOpened.add(throwable);
    }

    /**
     * Notify ConnectionWrapper that a Statement has opened a streaming
     * ResultSet.
     *
     * @throws StreamingResultSetSQLException
     */
    public void afterStreamingResultSetOpened(Throwable throwable)
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
     *
     * @throws StreamingResultSetSQLException
     */
    public void afterResultSetClosed(Throwable throwable) {
        resultSetsOpened.remove(throwable);
    }

    /**
     * Notify ConnectionWrapper that a streaming ResultSet has been closed.
     *
     * @throws StreamingResultSetSQLException
     */
    public void afterStreamingResultSetClosed() {
        streamingResultSetOpened = null;
    }

    private ConnectionWrapper(final Connection originalConnection) {
        this.originalConnection = originalConnection;
    }
}
