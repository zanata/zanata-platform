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
package org.zanata.test;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.hibernate.Session;
import org.zanata.test.rule.DataSetOperation;

import javax.persistence.EntityManager;

/**
 * Utility test class to run DBUnit data sets using a provided entity manager.
 * Use this instead of extending {@link org.zanata.ZanataDbunitJpaTest}.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DBUnitDataSetRunner {

    private final EntityManager em;

    public DBUnitDataSetRunner(EntityManager em) {
        this.em = em;
    }

    public void runDataSetOperations(DataSetOperation... operations) {
        for (DataSetOperation dso : operations) {
            dso.prepare(true);
        }
        executeOperations(operations);
    }

    protected void executeOperations(DataSetOperation... operations) {
        // NB: Hibernate specific
        em.unwrap(Session.class).doWork(connection -> {
            try {
                DatabaseConnection dbunitConn =
                        new DatabaseConnection(connection);
                disableReferentialIntegrity(dbunitConn);
                for (DataSetOperation op : operations) {
                    op.execute(dbunitConn);
                }
                enableReferentialIntegrity(dbunitConn);
            } catch (DatabaseUnitException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Execute whatever statement is necessary to either defer or disable
     * foreign key constraint checking on the given database connection, which
     * is used by DBUnit to import datasets.
     *
     * @param con A DBUnit connection wrapper, which is used afterwards for
     *            dataset operations
     */
    protected void disableReferentialIntegrity(IDatabaseConnection con) {
        try {
            con.getConnection()
                    .prepareStatement("set referential_integrity FALSE")
                    .execute(); // HSQL
            // DB
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Execute whatever statement is necessary to enable integrity constraint
     * checks after dataset operations.
     *
     * @param con A DBUnit connection wrapper, before it is used by the
     *            application again
     */
    protected void enableReferentialIntegrity(IDatabaseConnection con) {
        try {
            con.getConnection()
                    .prepareStatement("set referential_integrity TRUE")
                    .execute(); // HSQL
            // DB
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
