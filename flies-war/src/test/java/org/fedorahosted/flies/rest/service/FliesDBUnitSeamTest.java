package org.fedorahosted.flies.rest.service;

import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;

public abstract class FliesDBUnitSeamTest extends DBUnitSeamTest {

	private boolean databaseSet;
	private boolean jndiNameSet;
	private boolean binaryDirSet;

	@BeforeClass
	public void setDatabaseParameters() throws Exception {
		if(!binaryDirSet)
			setBinaryDir("");
		if (!databaseSet)
			setDatabase("hsql");
		if (jndiNameSet)
			setDatasourceJndiName("java:/fliesDatasource");
	}
	
	@Override
	public void setBinaryDir(String binaryDir) {
		super.setBinaryDir(binaryDir);
		binaryDirSet = true;
	}
	
	@Override
	public void setDatabase(String database) {
		super.setDatabase(database);
		databaseSet = true;
	}
	
	@Override
	public void setDatasourceJndiName(String datasourceJndiName) {
		super.setDatasourceJndiName(datasourceJndiName);
		jndiNameSet = true;
	}
}
