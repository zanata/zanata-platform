package org.fedorahosted.flies.rest.service;

import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;

public abstract class FliesDBUnitSeamTest extends DBUnitSeamTest {

	@BeforeClass
	public void setDatabaseParameters() throws Exception {
		setDatabase("hsql");
		setDatasourceJndiName("java:/fliesDatasource");
		setBinaryDir("");
	}
}
