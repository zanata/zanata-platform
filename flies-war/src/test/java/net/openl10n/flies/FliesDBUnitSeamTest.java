package net.openl10n.flies;

import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test(groups = { "seam-tests" })
public abstract class FliesDBUnitSeamTest extends DBUnitSeamTest
{

   // begin config methods from DBUnitSeamTest
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

   // end of config methods from DBUnitSeamTest

   // begin setup methods from DBUnitSeamTest

   @BeforeClass(groups = { "seam-tests" })
   @Override
   public void setupClass() throws Exception
   {
      super.setupClass();
   }

   @BeforeMethod(groups = { "seam-tests" })
   @Override
   public void prepareDataBeforeTest()
   {
      super.prepareDataBeforeTest();
   }

   @AfterMethod(groups = { "seam-tests" })
   @Override
   public void cleanDataAfterTest()
   {
      super.cleanDataAfterTest();
   }

   // end of setup methods from DBUnitSeamTest

   // begin setup methods from SeamTest

   @BeforeMethod(groups = { "seam-tests" })
   @Override
   public void begin()
   {
      super.begin();
   }

   @AfterMethod(groups = { "seam-tests" })
   @Override
   public void end()
   {
      super.end();
   }

   @Override
   @AfterClass(groups = { "seam-tests" })
   public void cleanupClass() throws Exception
   {
      super.cleanupClass();
   }

   @Override
   @BeforeSuite(groups = { "seam-tests" })
   public void startSeam() throws Exception
   {
      super.startSeam();
   }

   @Override
   @AfterSuite(groups = { "seam-tests" })
   protected void stopSeam() throws Exception
   {
      super.stopSeam();
   }
   // end of setup methods from SeamTest

}
