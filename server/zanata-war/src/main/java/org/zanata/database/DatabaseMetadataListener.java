/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Log database and database driver information at context start time.
 * This listener should appear before liquibase listener so that if liquibase
 * run into any error we can use this information for troubleshooting.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DatabaseMetadataListener implements
        ServletContextListener {
    private static final Logger log = LoggerFactory
            .getLogger(DatabaseMetadataListener.class);

    @Resource(lookup = "java:jboss/datasources/zanataDatasource")
    private DataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            String dbProductName = metaData.getDatabaseProductName();
            int dbMajorVer = metaData.getDatabaseMajorVersion();
            int dbMinorVer = metaData.getDatabaseMinorVersion();

            String dbVersion =
                    metaData.getDatabaseProductVersion();

            log.info("===================================");
            log.info("  Database: name {}, version {} (major: {}, minor: {})",
                    dbProductName, dbVersion, dbMajorVer, dbMinorVer);
            log.info("  JDBC Driver: name {}, version {}",
                    metaData.getDriverName(), metaData.getDriverVersion());
            log.info("===================================");
        } catch (Exception e) {
            log.warn("fail on getting database metadata", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
