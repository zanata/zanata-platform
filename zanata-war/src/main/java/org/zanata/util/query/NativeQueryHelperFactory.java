/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util.query;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.util.ServiceLocator;

/**
 * Factory that builds instances of Native Query helpers.
 * Note: The current implementation is heavily reliant on Hibernate.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("nativeQueryHelperFactory")
@Scope(ScopeType.STATELESS)
public class NativeQueryHelperFactory {

    // Expected Database types
    public static final String DB_TYPE_H2 = "H2";
    public static final String DB_TYPE_MYSQL = "MySQL";
    public static final String DB_TYPE_POSTGRESQL = "PostgreSQL";

    @In
    private ServiceLocator serviceLocator;

    public static String getDatabaseType(EntityManager em) throws SQLException {
        return getDatabaseType((Session) em.getDelegate());
    }

    public static String getDatabaseType(Session session) throws SQLException {
        String databaseType = session.doReturningWork(
                new ReturningWork<String>() {
                    @Override
                    public String execute(Connection connection)
                            throws SQLException {
                        return connection.getMetaData()
                                .getDatabaseProductName();
                    }
                });
        return databaseType;
    }

    public static NativeQueryHelper getNativeQueryHelper(Session session)
            throws SQLException {
        String dbType = getDatabaseType(session);
        if (DB_TYPE_H2.equals(dbType)) {
            return new H2NativeQueryHelper();
        } else if (DB_TYPE_MYSQL.equals(dbType)) {
            return new MysqlNativeQueryHelper();
        } else if (DB_TYPE_POSTGRESQL.equals(dbType)) {
            return new PostgresqlNativeQueryHelper();
        } else {
            throw new RuntimeException(
                    "Native queries are not supported for the '" + dbType
                            + "' database.");
        }
    }

    public static NativeQueryHelper getNativeQueryHelper(EntityManager em)
            throws SQLException {
        return getNativeQueryHelper((Session) em.getDelegate());
    }

    @Factory(autoCreate = true, scope = ScopeType.STATELESS)
    public NativeQueryHelper getNativeQueryHelper() throws SQLException {
        EntityManager em = serviceLocator.getEntityManager();
        return getNativeQueryHelper(em);
    }
}
