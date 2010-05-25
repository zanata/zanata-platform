package org.fedorahosted.flies;

import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class FliesDBUnitSeamTest extends DBUnitSeamTest {

	@BeforeClass
	@Parameters("datasourceJndiName")
	public void setDatasourceJndiName(@Optional("java:/fliesDatasource") String datasourceJndiName)
	{
		super.setDatasourceJndiName(datasourceJndiName);
	}

	@BeforeClass
	@Parameters("binaryDir")
	public void setBinaryDir(@Optional("") String binaryDir)
	{
		super.setBinaryDir(binaryDir);
	}

	@BeforeClass
	@Parameters("database")
	public void setDatabase(@Optional("hsql") String database)
	{
		super.setDatabase(database);
	}

}
