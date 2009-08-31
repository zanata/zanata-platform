package org.fedorahosted.flies.util;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * Imports some data into the database with the help of DBUnit. This allows us
 * to use the same dataset files as in unit testing, but in the regular
 * application startup during development. Also helps to avoid maintaining the
 * crude Hibernate import.sql file.
 * 
 * @author Christian Bauer
 */
public class DBUnitImporter {

	public enum Database {
		hsql, mysql
	}

	protected Database database;

	private String datasourceJndiName;

	String binaryDir;

	List<String> datasets = new ArrayList<String>();

	public void setDatabase(String database) {
		this.database = Database.valueOf(database);
	}

	public String getDatasourceJndiName() {
		return datasourceJndiName;
	}

	public void setDatasourceJndiName(String datasourceJndiName) {
		this.datasourceJndiName = datasourceJndiName;
	}

	public String getBinaryDir() {
		return binaryDir;
	}

	public void setBinaryDir(String binaryDir) {
		this.binaryDir = binaryDir;
	}

	public List<String> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<String> datasets) {
		this.datasets = datasets;
	}

	public void importDatasets() throws Exception {

		List<DataSetOperation> dataSetOperations = new ArrayList<DataSetOperation>();

		for (String dataset : datasets) {
			dataSetOperations.add(new DataSetOperation(dataset));
		}

		IDatabaseConnection con = null;
		try {
			con = getConnection();
			disableReferentialIntegrity(con);
			for (DataSetOperation op : dataSetOperations) {
				op.execute(con);
			}
			enableReferentialIntegrity(con);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		}

	}

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
			DataSource datasource = ((DataSource) new InitialContext()
					.lookup(datasourceJndiName));

			// Get a JDBC connection from JNDI datasource
			Connection con = datasource.getConnection();
			IDatabaseConnection dbUnitCon = new DatabaseConnection(con);
			editConfig(dbUnitCon.getConfig());
			return dbUnitCon;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Override this method if you aren't using HSQL DB.
	 * <p/>
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
			if (database.equals(Database.hsql)) {
				con.getConnection().prepareStatement(
						"set referential_integrity FALSE").execute(); // HSQL DB
			} else if (database.equals(Database.mysql)) {
				con.getConnection()
						.prepareStatement("set foreign_key_checks=0").execute(); // MySQL
																					// >
																					// 4.1.1
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Override this method if you aren't using HSQL DB.
	 * <p/>
	 * Execute whatever statement is necessary to enable integrity constraint
	 * checks after dataset operations.
	 * 
	 * @param con
	 *            A DBUnit connection wrapper, before it is used by the
	 *            application again
	 */
	protected void enableReferentialIntegrity(IDatabaseConnection con) {
		try {
			if (database.equals(Database.hsql)) {
				con.getConnection().prepareStatement(
						"set referential_integrity TRUE").execute(); // HSQL DB
			} else if (database.equals(Database.mysql)) {
				con.getConnection()
						.prepareStatement("set foreign_key_checks=1").execute(); // MySQL
																					// >
																					// 4.1.1
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Override this method if you require DBUnit configuration features or
	 * additional properties.
	 * <p>
	 * Called after a connection has been obtaind and before the connection is
	 * used. Can be a NOOP method if no additional settings are necessary for
	 * your DBUnit/DBMS setup.
	 * 
	 * @param config
	 *            A DBUnit <tt>DatabaseConfig</tt> object for setting properties
	 *            and features
	 */
	protected void editConfig(DatabaseConfig config) {

		if (database.equals(Database.hsql)) {
			// DBUnit/HSQL bugfix
			// http://www.carbonfive.com/community/archives/2005/07/dbunit_hsql_and.html
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
					new DefaultDataTypeFactory() {
						@Override
						public DataType createDataType(int sqlType,
								String sqlTypeName) throws DataTypeException {
							if (sqlType == Types.BOOLEAN) {
								return DataType.BOOLEAN;
							}
							return super.createDataType(sqlType, sqlTypeName);
						}
					});
		}

	}

	/**
	 * Resolves the binary dir location with the help of the classloader, we
	 * need the absolute full path of that directory.
	 * 
	 * @return String full absolute path of the binary directory
	 */
	protected String getBinaryDirFullpath() {
		if (binaryDir == null) {
			throw new RuntimeException(
					"Please set binaryDir property to location of binary test files");
		}
		URL url = Thread.currentThread().getContextClassLoader().getResource(
				getBinaryDir());
		if (url == null) {
			throw new RuntimeException(
					"Could not find full path with classloader of binaryDir: "
							+ getBinaryDir());
		}
		return url.toString();
	}

	protected class DataSetOperation {
		String dataSetLocation;
		ReplacementDataSet dataSet;
		DatabaseOperation operation;

		/**
		 * Defaults to <tt>DatabaseOperation.CLEAN_INSERT</tt>
		 */
		public DataSetOperation(String dataSetLocation) {
			this(dataSetLocation, DatabaseOperation.INSERT);
		}

		public DataSetOperation(String dataSetLocation,
				DatabaseOperation operation) {
			// Load the base dataset file
			InputStream input = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(dataSetLocation);
			try {
				this.dataSet = new ReplacementDataSet(new FlatXmlDataSet(input));
			} catch (Exception ex) {
				throw new RuntimeException(
						"Could not load dataset for import: " + dataSetLocation,
						ex);
			}
			this.dataSet.addReplacementObject("[NULL]", null);
			this.dataSet.addReplacementSubstring("[BINARY_DIR]",
					getBinaryDirFullpath());
			this.operation = operation;
			this.dataSetLocation = dataSetLocation;
		}

		public IDataSet getDataSet() {
			return dataSet;
		}

		public DatabaseOperation getOperation() {
			return operation;
		}

		public void execute(IDatabaseConnection connection) {
			try {
				this.operation.execute(connection, dataSet);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public String toString() {
			// TODO: This is not pretty because DBUnit's DatabaseOperation
			// doesn't implement toString() properly
			return operation.getClass() + " with dataset: " + dataSetLocation;
		}
	}

}
