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
package org.zanata.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

/**
 * Provides DBUnit facilities.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DBUnitProvider {
    private static final H2DataTypeFactory H2_DATA_TYPE_FACTORY =
            new H2DataTypeFactory();
    private final Callable<IDatabaseConnection> connectionSupplier;
    protected String binaryDir;
    protected boolean replaceNull = true;
    protected List<DataSetOperation> beforeTestOperations =
            new ArrayList<DataSetOperation>();
    protected List<DataSetOperation> afterTestOperations =
            new ArrayList<DataSetOperation>();

    private boolean prepared = false;

    public DBUnitProvider(Callable<IDatabaseConnection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    public void setBinaryDir(String binaryDir) {
        if (binaryDir == null)
            return;
        this.binaryDir = binaryDir;
    }

    public String getBinaryDir() {
        return binaryDir;
    }

    public void setReplaceNull(Boolean replaceNull) {
        if (replaceNull == null)
            return;
        this.replaceNull = replaceNull;
    }

    public Boolean isReplaceNull() {
        return replaceNull;
    }

    public void prepareDataBeforeTest() {
        // This is not pretty but we unfortunately can not have dependencies
        // between @BeforeClass methods.
        // This was a basic design mistake and we can't change it now because we
        // need to be backwards
        // compatible. We can only "prepare" the datasets once all @BeforeClass
        // have been executed.
        if (!prepared) {
            for (DataSetOperation beforeTestOperation : beforeTestOperations) {
                beforeTestOperation.prepare(this);
            }
            for (DataSetOperation afterTestOperation : afterTestOperations) {
                afterTestOperation.prepare(this);
            }
            prepared = true;
        }

        executeOperations(beforeTestOperations);
        clearCache();
    }

    public void cleanDataAfterTest() {
        executeOperations(afterTestOperations);
        clearCache();
    }

    private void clearCache() {
        /*
         * Session session = getSession();
         * session.getSessionFactory().getCache().evictEntityRegions();
         * session.getSessionFactory().getCache().evictCollectionRegions();
         */
    }

    public void executeOperations(List<DataSetOperation> list) {
        IDatabaseConnection con = getConnection();
        try {
            editConfig(con.getConfig());
            disableReferentialIntegrity(con.getConnection());
            for (DataSetOperation op : list) {
                prepareExecution(con, op);
                op.execute(con);
                afterExecution(con, op);
            }
            enableReferentialIntegrity(con.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                con.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }

    // TODO combine both classes called DataSetOperation into one!
    public static class DataSetOperation {

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
            try (InputStream input =
                    Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(dataSetLocation)) {
                if (input == null) {
                    throw new RuntimeException("missing resource: " + dataSetLocation);
                }
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
            } catch (IOException | DataSetException ex) {
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

        public void prepare(DBUnitProvider provider) {
            if (dataSet == null)
                return;

            if (provider.isReplaceNull()) {
                dataSet.addReplacementObject("[NULL]", null);
            }
            if (provider.getBinaryDir() != null) {
                dataSet.addReplacementSubstring("[BINARY_DIR]", provider
                        .getBinaryDirFullpath().toString());
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

    /**
     * @return a DBUnit database connection (wrapped)
     */
    protected IDatabaseConnection getConnection() {
        try {
            return connectionSupplier.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute whatever statement is necessary to either defer or disable
     * foreign key constraint checking on the given database connection, which
     * is used by DBUnit to import datasets.
     *
     * @param conn
     *            A DBUnit connection wrapper, which is used afterwards for
     *            dataset operations
     */
    protected void disableReferentialIntegrity(Connection conn) {
        try (PreparedStatement s = conn
                .prepareStatement("set referential_integrity FALSE")) {
            s.execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Execute whatever statement is necessary to enable integrity constraint
     * checks after dataset operations.
     *
     * @param conn
     *            A DBUnit connection wrapper, before it is used by the
     *            application again
     */
    protected void enableReferentialIntegrity(Connection conn) {
        try (PreparedStatement s = conn
                .prepareStatement("set referential_integrity TRUE")) {
            s.execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Override this method if you require DBUnit configuration features or
     * additional properties.
     * <p/>
     * Called after a connection has been obtaind and before the connection is
     * used. Can be a NOOP method if no additional settings are necessary for
     * your DBUnit/DBMS setup.
     *
     * @param config
     *            A DBUnit <tt>DatabaseConfig</tt> object for setting properties
     *            and features
     */
    public static void editConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                H2_DATA_TYPE_FACTORY);
    }

    /**
     * Callback for each operation before DBUnit executes the operation, useful
     * if extra preparation of data/tables is necessary, e.g. additional SQL
     * commands on a per-operation (per table?) granularity on the given
     * database connection.
     *
     * @param con
     *            A DBUnit connection wrapper
     * @param operation
     *            The operation to be executed, call <tt>getDataSet()</tt> to
     *            access the data.
     */
    protected void prepareExecution(IDatabaseConnection con,
            DataSetOperation operation) {
    }

    /**
     * Callback for each operation, useful if extra preparation of data/tables
     * is necessary.
     *
     * @param con
     *            A DBUnit connection wrapper
     * @param operation
     *            The operation that was executed, call <tt>getDataSet()</tt> to
     *            access the data.
     */
    protected void afterExecution(IDatabaseConnection con,
            DataSetOperation operation) {
    }

    /**
     * Resolves the binary dir location with the help of the classloader, we
     * need the absolute full path of that directory.
     *
     * @return URL full absolute path of the binary directory
     */
    protected URL getBinaryDirFullpath() {
        if (getBinaryDir() == null) {
            throw new RuntimeException(
                    "Please set binaryDir TestNG property to location of binary test files");
        }
        return getResourceURL(getBinaryDir());
    }

    protected URL getResourceURL(String resource) {
        URL url =
                Thread.currentThread().getContextClassLoader()
                        .getResource(resource);
        if (url == null) {
            throw new RuntimeException(
                    "Could not find resource with classloader: " + resource);
        }
        return url;
    }

    /**
     * Load a file and return it as a <tt>byte[]</tt>. Useful for comparison
     * operations in an actual unit test, e.g. to compare an imported database
     * record against a known file state.
     *
     * @param filename
     *            the path of the file on the classpath, relative to configured
     *            <tt>binaryDir</tt> base path
     * @return the file content as bytes
     * @throws Exception
     *             when the file could not be found or read
     */
    protected byte[] getBinaryFile(String filename) throws Exception {
        if (getBinaryDir() == null) {
            throw new RuntimeException(
                    "Please set binaryDir TestNG property to location of binary test files");
        }
        File file =
                new File(getResourceURL(getBinaryDir() + "/" + filename)
                        .toURI());
        InputStream is = new FileInputStream(file);
        try {

            // Get the size of the file
            long length = file.length();

            if (length > Integer.MAX_VALUE) {
                throw new Exception("File is too large");
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "
                        + file.getName());
            }
            return bytes;
        } finally {
            is.close();
        }
    }

    public void addBeforeTestOperation(DataSetOperation operation) {
        this.beforeTestOperations.add(operation);
    }

    public void addAfterTestOperation(DataSetOperation operation) {
        this.afterTestOperations.add(operation);
    }
}
