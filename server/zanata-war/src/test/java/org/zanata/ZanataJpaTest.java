package org.zanata;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test(groups = { "jpa-tests" })
public abstract class ZanataJpaTest
{

   private static final String PERSIST_NAME = "zanataTestDatasource";

   private static EntityManagerFactory emf;

   protected EntityManager em;

   Log log = Logging.getLog(ZanataJpaTest.class);

   @BeforeMethod
   public void setupEM()
   {
      log.debug("Setting up EM");
      em = emf.createEntityManager();
      em.getTransaction().begin();
   }

   @AfterMethod
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

   @BeforeSuite
   public void initializeEMF()
   {
      log.debug("Initializing EMF");
      emf = Persistence.createEntityManagerFactory(PERSIST_NAME);
   }

   @AfterSuite
   public void shutDownEMF()
   {
      log.debug("Shutting down EMF");
      emf.close();
      emf = null;
   }

}
