package org.zanata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.internal.SessionImpl;
import org.junit.After;
import org.junit.Before;

/**
 * Adapted from org.jboss.seam.mock.DBUnitSeamTest.
 * (Refer to org.jboss.seam.mock.DBUnitSeamTest
 * and org.jboss.seam.mock.DBJUnitSeamTest.)
 * <p>
 * For a better way to prepare a test database, see also
 * {@link org.zanata.test.DBUnitDataSetRunner},
 * {@link org.zanata.test.rule.JpaRule}.
 * </p>
 */
public abstract class ZanataDbunitJpaTest extends ZanataJpaTest {
    protected String binaryDir;
    protected boolean replaceNull = true;
    protected List<DataSetOperation> beforeTestOperations =
            new ArrayList<DataSetOperation>();
    protected List<DataSetOperation> afterTestOperations =
            new ArrayList<DataSetOperation>();
    private boolean prepared = false;

    protected String getBinaryDir() {
        return binaryDir;
    }

    protected Boolean isReplaceNull() {
        return replaceNull;
    }

    @Before
    public void prepareDataBeforeTest() {
        // This is not pretty but we unfortunately can not have dependencies
        // between @BeforeClass methods.
        // This was a basic design mistake and we can't change it now because we
        // need to be backwards
        // compatible. We can only "prepare" the datasets once all @BeforeClass
        // have been executed.
        if (!prepared) {
            prepareDBUnitOperations();
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

    @After
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

    protected void executeOperations(List<DataSetOperation> list) {
        IDatabaseConnection con = null;
        con = getConnection();
        disableReferentialIntegrity(con);
        for (DataSetOperation op : list) {
            prepareExecution(con, op);
            op.execute(con);
            afterExecution(con, op);
        }
        enableReferentialIntegrity(con);
    }

    protected static class DataSetOperation {
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
            InputStream input = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(dataSetLocation);
            if (input == null) {
                throw new RuntimeException(
                        "missing resource: " + dataSetLocation);
            }
            try {
                FlatXmlDataSetBuilder dataSetBuilder =
                        new FlatXmlDataSetBuilder();
                dataSetBuilder.setColumnSensing(true);
                InputStream dtdInput = null;
                if (dtdLocation != null) {
                    dtdInput = Thread.currentThread().getContextClassLoader()
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

        public void prepare(ZanataDbunitJpaTest test) {
            if (dataSet == null)
                return;
            if (test.isReplaceNull()) {
                dataSet.addReplacementObject("[NULL]", null);
            }
            if (test.getBinaryDir() != null) {
                dataSet.addReplacementSubstring("[BINARY_DIR]",
                        test.getBinaryDirFullpath().toString());
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
    // TODO This is hibernate specific

    /**
     * Override this method if you want to provide your own DBUnit
     * <tt>IDatabaseConnection</tt> instance.
     * <p/>
     * If you do not override this, default behavior is to use the * configured
     * datasource name and to obtain a connection with a JNDI lookup.
     *
     * @return a DBUnit database connection (wrapped)
     */
    protected IDatabaseConnection getConnection() {
        try {
            DatabaseConnection dbConn = new DatabaseConnection(
                    ((SessionImpl) getSession()).connection());
            editConfig(dbConn.getConfig());
            return dbConn;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Execute whatever statement is necessary to either defer or disable
     * foreign key constraint checking on the given database connection, which
     * is used by DBUnit to import datasets.
     *
     * @param con
     *            A DBUnit connection wrapper, which is used afterwards for
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
     * @param con
     *            A DBUnit connection wrapper, before it is used by the
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
    protected void editConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                new H2DataTypeFactory());
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
        URL url = Thread.currentThread().getContextClassLoader()
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
        File file = new File(
                getResourceURL(getBinaryDir() + "/" + filename).toURI());
        InputStream is = new FileInputStream(file);
        try {
            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }
            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];
            // Read in the bytes
            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead =
                    is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException(
                        "Could not completely read file " + file.getName());
            }
            return bytes;
        } finally {
            is.close();
        }
    }

    /**
     * Implement this in a subclass.
     * <p/>
     * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with the
     * <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
     */
    protected abstract void prepareDBUnitOperations();
}
