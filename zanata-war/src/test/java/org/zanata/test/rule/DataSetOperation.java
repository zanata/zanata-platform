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
package org.zanata.test.rule;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * Describes a DBUnit data set operation.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link org.zanata.test.DBUnitDataSetRunner}
 * @see {@link @org.zanata.ZanataDbunitJpaTest.DataSetOperation}
 */
public class DataSetOperation {
    String dataSetLocation;
    ReplacementDataSet dataSet;
    DatabaseOperation operation;

    protected DataSetOperation() {
        // Support subclassing
    }

    /**
     * Defaults to <tt>DatabaseOperation.CLEAN_INSERT</tt>
     *
     * @param dataSetLocation
     *            location of DBUnit dataset
     */
    public DataSetOperation(String dataSetLocation) {
        this(dataSetLocation, DatabaseOperation.CLEAN_INSERT);
    }

    /**
     * Defaults to <tt>DatabaseOperation.CLEAN_INSERT</tt>
     *
     * @param dataSetLocation
     *            location of DBUnit dataset
     * @param dtdLocation
     *            optional (can be null) location of XML file DTD on
     *            classpath
     */
    public DataSetOperation(String dataSetLocation, String dtdLocation) {
        this(dataSetLocation, dtdLocation, DatabaseOperation.CLEAN_INSERT);
    }

    /**
     * @param dataSetLocation
     *            location of DBUnit dataset
     * @param operation
     *            operation to execute
     */
    public DataSetOperation(String dataSetLocation,
            DatabaseOperation operation) {
        this(dataSetLocation, null, operation);
    }

    public DataSetOperation(String dataSetLocation, String dtdLocation,
            DatabaseOperation operation) {
        if (dataSetLocation == null) {
            this.operation = operation;
            return;
        }

        // Load the base dataset file
        InputStream input =
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(dataSetLocation);
        try {
            FlatXmlDataSetBuilder dataSetBuilder =
                    new FlatXmlDataSetBuilder();
            dataSetBuilder.setColumnSensing(true);

            InputStream dtdInput = null;
            if (dtdLocation != null) {
                dtdInput =
                        Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream(dtdLocation);
            }
            if (dtdInput == null) {
                this.dataSet =
                        new ReplacementDataSet(dataSetBuilder.build(input));
            } else {
                dataSetBuilder.setMetaDataSetFromDtd(dtdInput);
                this.dataSet =
                        new ReplacementDataSet(dataSetBuilder.build(input));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.operation = operation;
        this.dataSetLocation = dataSetLocation;
    }

    public IDataSet getDataSet() {
        return dataSet;
    }

    public DatabaseOperation getOperation() {
        return operation;
    }

    public void prepare(boolean replaceNull) {
        if (dataSet == null)
            return;

        if (replaceNull) {
            dataSet.addReplacementObject("[NULL]", null);
        }
    }

    public void execute(IDatabaseConnection connection) {
        if (dataSet == null || operation == null)
            return;
        try {
            this.operation.execute(connection, dataSet);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " with dataset location: "
                + dataSetLocation;
    }
}
