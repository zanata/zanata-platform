/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.util.Naming;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.zanata.provider.DBUnitProvider;

/**
 * Base class for all Arquillian tests. Provides DB Unit facilities for
 * in-container tests.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(Arquillian.class)
public abstract class ArquillianTest {

    protected DBUnitProvider dbUnitProvider = new DBUnitProvider() {
        @Override
        protected IDatabaseConnection getConnection() {
            return ArquillianTest.getConnection();
        }
    };

    /**
     * Implement this in a subclass.
     * <p/>
     * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with the
     * <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
     */
    protected abstract void prepareDBUnitOperations();

    public void
            addBeforeTestOperation(DBUnitProvider.DataSetOperation operation) {
        dbUnitProvider.addBeforeTestOperation(operation);
    }

    public void
            addAfterTestOperation(DBUnitProvider.DataSetOperation operation) {
        dbUnitProvider.addAfterTestOperation(operation);
    }

    /**
     * Invoked on the arquillian container before the test is run.
     */
    @Before
    public void prepareDataBeforeTest() {
        prepareDBUnitOperations();
        dbUnitProvider.prepareDataBeforeTest();
    }

    /**
     * Invoked in the arquillian container after the test is run.
     */
    @After
    public void cleanDataAfterTest() {
        dbUnitProvider.cleanDataAfterTest();
    }

    private static IDatabaseConnection getConnection() {
        try {
            DataSource dataSource =
                    (DataSource) Naming.getInitialContext().lookup(
                            "java:jboss/datasources/zanataTestDatasource");
            DatabaseConnection dbConn =
                    new DatabaseConnection(dataSource.getConnection());
            // NB: Specific to H2
            dbConn.getConfig().setProperty(
                    DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    new H2DataTypeFactory());
            return dbConn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
