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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final Connection connection;
    private Set<Throwable> resultSetsOpened = Sets.newHashSet();
    private Throwable streamingResultSetOpened;
    public static final String PROPERTY_USE_WRAPPER =
            "zanata.connection.use.wrapper";
    private static final String USE_WRAPPER =
            System.getProperty(PROPERTY_USE_WRAPPER);

    public static Connection wrap(Connection connection) {
        if (Proxy.isProxyClass(connection.getClass())
                && Proxy.getInvocationHandler(connection) instanceof ConnectionWrapper) {
            return connection;
        }
        return ProxyUtil
                .newProxy(connection, new ConnectionWrapper(connection));
    }

    public static boolean shouldWrap(DatabaseMetaData metaData)
            throws SQLException {
        if (USE_WRAPPER != null) {
            if ("false".equals(USE_WRAPPER)) {
                log.info("Not wrapping JDBC connection (disabled by system " +
                        "property {})", PROPERTY_USE_WRAPPER);
                return false;
            }
            if ("true".equals(USE_WRAPPER)) {
                log.info("Wrapping JDBC connection (forced by system " +
                        "property {})", PROPERTY_USE_WRAPPER);
                return true;
            }
            if (!("auto".equals(USE_WRAPPER))) {
                log.warn("Unknown value for system property {}: {}",
                        PROPERTY_USE_WRAPPER, USE_WRAPPER);
            }
        }
        String driverName = metaData.getDriverName();
        if (driverName.equals("MySQL Connector Java") ||
                driverName.equals("mariadb-jdbc")) {
            // these drivers are known to use streaming result sets
            // when fetchSize == Integer.MIN_VALUE
            log.info("No need to wrap JDBC connection: driver: {}", driverName);
            return false;
        } else if (driverName.toLowerCase().contains("mysql") ||
                driverName.toLowerCase().contains("mariadb")) {
            // NB: if a future mysql/mariadb driver does away with the
            // fetchSize trick for streaming, please add a special case and
            // return true, or remove the Zanata code which calls
            // setFetchSize(Integer.MIN_VALUE). See StreamingEntityIterator.
            log.warn("Unrecognised mysql/mariadb driver: {}", driverName);
            log.warn("Streaming results may not work");
            return false;
        } else {
            log.info("Wrapping JDBC connection: found non-mysql/mariadb " +
                    "driver: {}", driverName);
            return true;
        }
    }

    /**
     * Log warnings or errors if the database or driver is not supported.
     * @param metaData
     * @throws SQLException
     */
    public static void checkSupported(DatabaseMetaData metaData)
            throws SQLException {
        String dbName = metaData.getDatabaseProductName();
        String dbVer = metaData.getDatabaseProductVersion();
        int dbMaj = metaData.getDatabaseMajorVersion();
        int dbMin = metaData.getDatabaseMinorVersion();
        log.info("Database product: {} version: {} ({}.{})",
                dbName, dbVer, dbMaj, dbMin);
        String lcName = dbName.toLowerCase();
        if (lcName.contains("mysql")) {
            log.info("Using MySQL database");
            if (dbMaj != 5) {
                log.warn("Unsupported MySQL major version: {}", dbMaj);
            }
            if (dbMin > 5) {
                log.warn("Unsupported MySQL minor version: {}", dbMin);
            }
        } else if (lcName.contains("h2")) {
            log.info("Using H2 database (not for production)");
        } else {
            log.warn("Unsupported database");
        }
        String drvName = metaData.getDriverName();
        String drvVer = metaData.getDriverVersion();
        int drvMaj = metaData.getDriverMajorVersion();
        int drvMin = metaData.getDriverMinorVersion();
        log.info("JDBC driver: {} version: {} ({}.{})",
                drvName, drvVer, drvMaj, drvMin);
    }

    public static Connection wrapUnlessMysql(Connection connection)
            throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        checkSupported(metaData);
        if (shouldWrap(metaData)) {
            Connection wrappedConnection = wrap(connection);
            log.info("Connection {} is wrapped by {}",
                    connection, wrappedConnection);
            return wrappedConnection;
        } else {
            return connection;
        }
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.getName().equals("toString")) {
            return "ConnectionWrapper->" + connection.toString();
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
            Object result = method.invoke(connection, args);
            if (result instanceof Statement) {
                Statement statement = (Statement) result;
                return StatementWrapper.wrap(statement, (Connection) proxy);
            }
            return result;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public void executed() throws SQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        }
    }

    public void resultSetOpened(Throwable throwable) throws SQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        }
        resultSetsOpened.add(throwable);
    }

    public void streamingResultSetOpened(Throwable throwable)
            throws SQLException {
        if (streamingResultSetOpened != null) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    streamingResultSetOpened);
        } else if (!resultSetsOpened.isEmpty()) {
            throw new StreamingResultSetSQLException(CONCURRENT_RESULTSET,
                    resultSetsOpened.iterator().next());
        }
        streamingResultSetOpened = throwable;
    }

    public void resultSetClosed(Throwable throwable) {
        resultSetsOpened.remove(throwable);
    }

    public void streamingResultSetClosed() {
        streamingResultSetOpened = null;
    }
}
