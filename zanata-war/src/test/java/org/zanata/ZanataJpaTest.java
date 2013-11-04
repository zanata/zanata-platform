package org.zanata;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.zanata.testng.TestMethodListener;

@Listeners(TestMethodListener.class)
@Test(groups = { "jpa-tests" })
public abstract class ZanataJpaTest {
    private static final Logger log = LoggerFactory.getLogger(ZanataJpaTest.class);
    private static final String PERSIST_NAME = "zanataTestDatasourcePU";

    private static EntityManagerFactory emf;

    protected EntityManager em;

    @BeforeMethod
    @Before
    public void setupEM() {
        log.debug("Setting up EM");
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @AfterMethod
    @After
    public void shutdownEM() {
        log.debug("Shutting down EM");
        clearHibernateSecondLevelCache();
        em.getTransaction().rollback();
        if (em.isOpen()) {
            em.close();
        }
        em = null;
    }

    protected EntityManager getEm() {
        return em;
    }

    protected EntityManagerFactory getEmf() {
        return emf;
    }

    protected Session getSession() {
        return (Session) em.getDelegate();
    }

    @BeforeSuite
    @BeforeClass
    public static void initializeEMF() {
        log.debug("Initializing EMF");
        emf =
                Persistence.createEntityManagerFactory(PERSIST_NAME,
                        createPropertiesMap());
    }

    protected static Map<?, ?> createPropertiesMap() {
        return null;
    }

    @AfterSuite
    @AfterClass
    public static void shutDownEMF() {
        log.debug("Shutting down EMF");
        emf.close();
        emf = null;
    }

    /**
     * Commits the changes on the current session and starts a new one. This
     * method is useful whenever multi-session tests are needed.
     *
     * @return The newly started session
     */
    protected Session newSession() {
        em.getTransaction().commit();
        setupEM();
        return getSession();
    }

    /**
     * This method is used to test multiple Entity Managers (or hibernate
     * sessions) working together simultaneously. Use
     * {@link org.zanata.ZanataJpaTest#getEm()} for all other tests.
     *
     * @return A new instance of an entity manager.
     */
    protected EntityManager newEntityManagerInstance() {
        return emf.createEntityManager();
    }

    /**
     * Clears the Hibernate Second Level cache.
     */
    protected void clearHibernateSecondLevelCache() {
        SessionFactory sessionFactory =
                ((Session) em.getDelegate()).getSessionFactory();
        try {
            sessionFactory.getCache().evictEntityRegions();
            sessionFactory.getCache().evictCollectionRegions();
        } catch (Exception e) {
            System.out.println(" *** Cache Exception " + e.getMessage());
        }
    }

}
