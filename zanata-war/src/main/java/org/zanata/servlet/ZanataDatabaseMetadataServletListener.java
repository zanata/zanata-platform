/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.ZanataDatabaseDriverMetadata;
import org.zanata.util.ZanataDatabaseMetaData;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataDatabaseMetadataServletListener implements
        ServletContextListener {
    private static final Logger log = LoggerFactory
            .getLogger(ZanataDatabaseMetadataServletListener.class);

    @Resource(lookup = "java:jboss/datasources/zanataDatasource")
    private DataSource dataSource;
    private static ZanataDatabaseMetaData databaseMetaData;
    private static ZanataDatabaseDriverMetadata dbDriverMetaData;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            String dbProductName = metaData.getDatabaseProductName();
            int dbMajorVer = metaData.getDatabaseMajorVersion();
            int dbMinorVer = metaData.getDatabaseMinorVersion();

            String dbVersion =
                    metaData.getDatabaseProductVersion();

            databaseMetaData = new ZanataDatabaseMetaData(dbProductName,
                    dbMajorVer, dbMinorVer, dbVersion); ;
            dbDriverMetaData = new ZanataDatabaseDriverMetadata(
                    metaData.getDriverName(), metaData.getDriverVersion());

        } catch (Exception e) {
            log.warn("fail on getting database metadata", e);
        }
    }

    @Produces
    ZanataDatabaseMetaData databaseMetaData() {
        return databaseMetaData;
    }

    @Produces
    ZanataDatabaseDriverMetadata databaseDriverMetadata() {
        return dbDriverMetaData;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
