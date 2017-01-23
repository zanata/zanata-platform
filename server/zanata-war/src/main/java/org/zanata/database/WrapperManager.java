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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * This class wraps JDBC Connections/Statements/ResultSets to detect attempts to
 * use mysql's streaming ResultSet feature. It then watches for any usage which
 * would exceed the limitations of mysql's streaming ResultSets, and throws an
 * SQLException. This enables us to catch these problems without having to test
 * against mysql in our unit tests.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class WrapperManager {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WrapperManager.class);

    public static final String PROPERTY_USE_WRAPPER =
            "zanata.connection.use.wrapper";
    private static final String USE_WRAPPER =
            System.getProperty(PROPERTY_USE_WRAPPER);
    private boolean checkedFirstConnection = false;
    private boolean wrappingEnabled = false;

    public Connection wrapIfNeeded(Connection conn) throws SQLException {
        if (!checkedFirstConnection) {
            DatabaseMetaData metaData = conn.getMetaData();
            checkSupported(metaData);
            wrappingEnabled = shouldWrap(metaData);
            checkedFirstConnection = true;
        }
        if (wrappingEnabled) {
            Connection wrapped = ConnectionWrapper.wrap(conn);
            log.debug("Connection {} is wrapped by {}", conn, wrapped);
            return wrapped;
        } else {
            return conn;
        }
    }

    /**
     * Log warnings or errors if the database or driver is not supported.
     *
     * @param metaData
     * @throws SQLException
     */
    private static void checkSupported(DatabaseMetaData metaData)
            throws SQLException {
        String dbName = metaData.getDatabaseProductName();
        String dbVer = metaData.getDatabaseProductVersion();
        int dbMaj = metaData.getDatabaseMajorVersion();
        int dbMin = metaData.getDatabaseMinorVersion();
        log.info("Database product: {} version: {} ({}.{})", dbName, dbVer,
                dbMaj, dbMin);
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
        log.info("JDBC driver: {} version: {} ({}.{})", drvName, drvVer, drvMaj,
                drvMin);
    }

    private static boolean shouldWrap(DatabaseMetaData metaData)
            throws SQLException {
        if (USE_WRAPPER != null) {
            if ("false".equals(USE_WRAPPER)) {
                log.info(
                        "Not wrapping JDBC connection (disabled by system property {})",
                        PROPERTY_USE_WRAPPER);
                return false;
            }
            if ("true".equals(USE_WRAPPER)) {
                log.info(
                        "Wrapping JDBC connection (forced by system property {})",
                        PROPERTY_USE_WRAPPER);
                return true;
            }
            if (!("auto".equals(USE_WRAPPER))) {
                log.warn("Unknown value for system property {}: {}",
                        PROPERTY_USE_WRAPPER, USE_WRAPPER);
            }
        }
        String driverName = metaData.getDriverName();
        if (driverName.equals("MySQL Connector Java")
                || driverName.equals("MySQL-AB JDBC Driver")
                || driverName.equals("mariadb-jdbc")) {
            // these drivers are known to use streaming result sets
            // when fetchSize == Integer.MIN_VALUE
            log.info("No need to wrap JDBC connection: driver: {}", driverName);
            return false;
        } else if (driverName.toLowerCase().contains("mysql")
                || driverName.toLowerCase().contains("mariadb")) {
            // NB: if a future mysql/mariadb driver does away with the
            // fetchSize trick for streaming, please add a special case and
            // return true, or remove the Zanata code which calls
            // setFetchSize(Integer.MIN_VALUE). See StreamingEntityIterator.
            log.warn("Unrecognised mysql/mariadb driver: {}", driverName);
            log.warn("Streaming results may not work");
            return false;
        } else {
            log.info(
                    "Wrapping JDBC connection: found non-mysql/mariadb driver: {}",
                    driverName);
            return true;
        }
    }
}
