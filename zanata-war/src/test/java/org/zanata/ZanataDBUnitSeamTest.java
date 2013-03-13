package org.zanata;

import java.io.InputStream;
import java.util.Map;

import org.dbunit.operation.DatabaseOperation;
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

public abstract class ZanataDBUnitSeamTest extends DBUnitSeamTest
{

   // begin config methods from DBUnitSeamTest
   @BeforeClass
   @Parameters("datasourceJndiName")
   public void setDatasourceJndiName(@Optional("java:jboss/datasources/zanataTestDatasource") String datasourceJndiName)
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

   @BeforeClass
   @Override
   public void setupClass() throws Exception
   {
      super.setupClass();
      this.clearHibernateSecondLevelCache(); // Clear the Hibernate cache after initial setup
   }

   @BeforeMethod
   @Override
   public void prepareDataBeforeTest()
   {
      super.prepareDataBeforeTest();
      this.clearHibernateSecondLevelCache(); // Clear the hibernate cache after data is modified before the test
   }

   @AfterMethod
   @Override
   public void cleanDataAfterTest()
   {
      super.cleanDataAfterTest();
   }

   // end of setup methods from DBUnitSeamTest

   // begin setup methods from SeamTest

   @BeforeMethod
   @Override
   public void begin()
   {
      super.begin();
   }

   @AfterMethod
   @Override
   public void end()
   {
      super.end();
   }

   @Override
   @AfterClass
   public void cleanupClass() throws Exception
   {
      super.cleanupClass();
   }

   @Override
   @BeforeSuite
   public void startSeam() throws Exception
   {
      super.startSeam();
   }

   @Override
   @AfterSuite
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

   protected static class DataSetOperation extends DBUnitSeamTest.DataSetOperation
   {
      public DataSetOperation()
      {
         super();
      }

      public DataSetOperation(String dataSetLocation, DatabaseOperation operation)
      {
         super(checkedLocation(dataSetLocation), operation);
      }

      public DataSetOperation(String dataSetLocation, String dtdLocation, DatabaseOperation operation)
      {
         super(checkedLocation(dataSetLocation), dtdLocation, operation);
      }

      public DataSetOperation(String dataSetLocation, String dtdLocation)
      {
         super(checkedLocation(dataSetLocation), dtdLocation);
      }

      public DataSetOperation(String dataSetLocation)
      {
         super(checkedLocation(dataSetLocation));
      }

      private static String checkedLocation(String dataSetLocation)
      {
         if (dataSetLocation == null)
         {
            return null;
         }

         // Check the base dataset file
         if (Thread.currentThread().getContextClassLoader().getResourceAsStream(dataSetLocation) == null)
         {
            throw new RuntimeException("Classpath resource not found: "+dataSetLocation);
         }
         return dataSetLocation;
      }
   }
}
