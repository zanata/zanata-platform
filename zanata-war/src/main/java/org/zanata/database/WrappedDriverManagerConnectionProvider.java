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
import java.sql.SQLException;

import org.hibernate.service.jdbc.connections.internal.DriverManagerConnectionProviderImpl;

/**
 * Modified version of DriverManagerConnectionProviderImpl which adds support
 * for JBDC wrapping. This Hibernate ConnectionProvider is used, in unit
 * tests, to simulate MySQL's support for streaming ResultSets, by allowing
 * fetchSize=Integer.MIN_VALUE (which H2 rejects) and by detecting violations
 * of MySQL's streaming ResultSet limitations (ie attempts to perform other
 * queries while a streaming ResultSet is open).
 * <p>
 * NB: this class uses the internal Hibernate class DriverManagerConnectionProviderImpl.
 * When Hibernate is updated, this ConnectionProvider may need to be updated
 * (eg to use a new class name, to implement new interfaces) or removed.
 * </p>
 * <p>
 * The delegation approach used in WrappedDatasourceConnectionProvider would
 * allow support for multiple Hibernate versions, but we only use a single
 * version of Hibernate when running unit tests, so it's probably not worth it.
 * </p>
 * @see org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl
 * @see org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
 * @see org.zanata.database.WrappedDatasourceConnectionProvider
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class WrappedDriverManagerConnectionProvider extends
        DriverManagerConnectionProviderImpl {
    private static final long serialVersionUID = 1L;
    private final WrapperManager wrapperManager = new WrapperManager();

    @Override
    public Connection getConnection() throws SQLException {
        return wrapperManager.wrapIfNeeded(super.getConnection());
    }

}
