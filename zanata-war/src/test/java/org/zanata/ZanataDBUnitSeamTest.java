package org.zanata;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.seam.contexts.TestLifecycle;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.jboss.seam.mock.MockHttpSession;
import org.jboss.seam.servlet.ServletSessionMap;
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
public abstract class ZanataDBUnitSeamTest extends DBUnitSeamTest
{

   // begin config methods from DBUnitSeamTest
   @BeforeClass
   @Parameters("datasourceJndiName")
   public void setDatasourceJndiName(@Optional("java:/zanataTestDatasource") String datasourceJndiName)
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
      this.clearHibernateSecondLevelCache(); // Clear the Hibernate cache after initial setup
   }

   @BeforeMethod(groups = { "seam-tests" })
   @Override
   public void prepareDataBeforeTest()
   {
      super.prepareDataBeforeTest();
      this.clearHibernateSecondLevelCache(); // Clear the hibernate cache after data is modified before the test
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
   
   /**
    * Clears the Hibernate Second Level Cache.
    * This method starts a Test lifecycle just for the purpose of clearing the Hibernate session factory's
    * second level cache. The hibernate session being used is a Seam component and as such can only be
    * retrieved when a test is "active".
    * TODO Maybe there is a way of doing this without intruding into TestNGs internals.
    */
   protected void clearHibernateSecondLevelCache()
   {
      TestLifecycle.beginTest(servletContext, new ServletSessionMap(new MockHttpSession(servletContext)));
      try
      {
         Session session = (Session)getInstance("session");
         SessionFactory sessionFactory = session.getSessionFactory();
         
         // Clear the Entity cache
         Map classMetadata = sessionFactory.getAllClassMetadata();
         for( Object obj : classMetadata.values() )
         {
            EntityPersister p = (EntityPersister)obj;
            if( p.hasCache() )
            {
               sessionFactory.evictEntity( p.getEntityName() );
            }
         }
         
         // Clear the Collection cache
         Map collMetadata = sessionFactory.getAllCollectionMetadata();
         for( Object obj : collMetadata.values() )
         {
            AbstractCollectionPersister p = (AbstractCollectionPersister)obj;
            if( p.hasCache() )
            {
               sessionFactory.evictCollection( p.getRole() );
            }
         }
      }
      finally
      {
         TestLifecycle.endTest();
      }
   }   

}
