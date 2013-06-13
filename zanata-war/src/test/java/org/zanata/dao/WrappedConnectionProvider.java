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

package org.zanata.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.service.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.zanata.jdbc.ConnectionWrapper;

/**
 * This class wraps JDBC Connections/Statements/ResultSets to detect
 * attempts to use mysql's streaming ResultSet feature.  It then watches
 * for any usage which would exceed the limitations of mysql's streaming
 * ResultSets, and throws an SQLException.  This enables us to catch
 * these problems without having to test against mysql in our unit tests.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class WrappedConnectionProvider extends DriverManagerConnectionProviderImpl
{
   @Override
   public Connection getConnection() throws SQLException
   {
      Connection connection = super.getConnection();
      return ConnectionWrapper.wrap(connection);
   }

}
