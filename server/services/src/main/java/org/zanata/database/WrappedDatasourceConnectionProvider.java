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
import java.util.Map;

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.Stoppable;

/**
 * Decorator for DatasourceConnectionProviderImpl which adds support for JBDC
 * wrapping. This Hibernate ConnectionProvider is used, in Arquillian
 * integration tests, to simulate MySQL's support for streaming ResultSets,
 * by allowing fetchSize=Integer.MIN_VALUE (which H2 rejects) and by
 * detecting violations of MySQL's streaming ResultSet limitations (ie
 * attempts to perform other queries while a streaming ResultSet is open).
 * <p>
 * NB: this class uses the internal Hibernate class DatasourceConnectionProviderImpl.
 * It tries a couple of different packages for it, corresponding to Hibernate
 * 4.2 and 4.3/5.x. If, in a new version of Hibernate, that class is changed
 * or removed, this ConnectionProvider may need to be updated (eg to use a new
 * class name, to implement new interfaces) or removed.
 * </p>
 * @see DatasourceConnectionProviderImpl
 * @see org.zanata.database.WrappedDriverManagerConnectionProvider
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class WrappedDatasourceConnectionProvider
        // NB: we should implement the same interfaces as Hibernate's DatasourceConnectionProviderImpl
        implements ConnectionProvider, Configurable, Stoppable {

    private static final long serialVersionUID = 1L;
    private final WrapperManager wrapperManager = new WrapperManager();
    private final ConnectionProvider delegate = new DatasourceConnectionProviderImpl();

    @Override
    public Connection getConnection() throws SQLException {
        return wrapperManager.wrapIfNeeded(delegate.getConnection());
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        delegate.closeConnection(conn);
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return delegate.supportsAggressiveRelease();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean isUnwrappableAs(Class unwrapType) {
        return delegate.isUnwrappableAs(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return delegate.unwrap(unwrapType);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void configure(Map configurationValues) {
        ((Configurable) delegate).configure(configurationValues);
    }

    @Override
    public void stop() {
        ((Stoppable) delegate).stop();
    }

}
