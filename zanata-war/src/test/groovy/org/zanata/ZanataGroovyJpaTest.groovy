package org.zanata

import org.hibernate.Session
import org.jboss.seam.log.Log
import org.jboss.seam.log.Logging
import org.testng.annotations.AfterMethod
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Listeners
import org.zanata.testng.TestMethodListener

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

/**
 * Base class for all groovy tests that initialize a JPA environment.
 *
 * This class is a groovy parallel to {@link ZanataJpaTest} with small changes to make it runnable by groovy,
 * such as no @Test annotation at the class level as TestNG will attempt to run all groovy-generated methods.
 *
 * Groups must be declared on all callback methods in order for them to get picked up when running frouped tests.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link ZanataJpaTest}
 */
@Listeners(TestMethodListener.class)
abstract class ZanataGroovyJpaTest
{
   private static final String PERSIST_NAME = "zanataTestDatasourcePU";

   private static EntityManagerFactory emf;

   protected EntityManager em;

   Log log = Logging.getLog(ZanataJpaTest.class);

   /**
    * @return A list of Hibernate Entities to create
    */
   public List getEntitiesToCreate()
   {
      []
   }

   /**
    * @return A list of Groovy script classes that contain appropriate elements.
    */
   public List getConfigScriptClasses()
   {
      []
   }

   @BeforeMethod(dependsOnMethods = "setupEM", groups = [ "jpa-tests" ])
   public void prepareDataBeforeTest()
   {
      // Data scripts
      for( def script : getConfigScriptClasses() )
      {
         def config = new ConfigSlurper().parse(script)

         for( def entity : config.zanata.test.insert )
         {
            getEm().persist( entity );
         }
      }

      // Individual classes
      for( def entity : getEntitiesToCreate() )
      {
         getEm().persist( entity );
      }
   }

   @BeforeMethod(groups = [ "jpa-tests" ])
   public void setupEM()
   {
      log.debug("Setting up EM");
      em = emf.createEntityManager();
      em.getTransaction().begin();
   }

   @AfterMethod(groups = [ "jpa-tests" ])
   public void shutdownEM()
   {
      log.debug("Shutting down EM");
      em.getTransaction().rollback();
      em = null;
   }

   protected EntityManager getEm()
   {
      return em;
   }

   protected Session getSession()
   {
      return (Session) em.getDelegate();
   }

   @BeforeSuite(groups = [ "jpa-tests" ])
   public void initializeEMF()
   {
      log.debug("Initializing EMF");
      emf = Persistence.createEntityManagerFactory(PERSIST_NAME);
   }

   @AfterSuite(groups = [ "jpa-tests" ])
   public void shutDownEMF()
   {
      log.debug("Shutting down EMF");
      emf.close();
      emf = null;
   }

   /**
    * Commits the changes on the current session and starts a new one.
    * This method is useful whenever multi-session tests are needed.
    *
    * @return The newly started session
    */
   protected Session newSession()
   {
      em.getTransaction().commit();
      setupEM();
      return getSession();
   }
}
