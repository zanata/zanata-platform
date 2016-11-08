package org.zanata;

import static com.github.huangp.entityunit.entity.EntityCleaner.deleteAll;

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
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.ZanataEntities;

// single threaded because of ehcache (perhaps other reasons too)
//@Test(singleThreaded = true)
public abstract class ZanataJpaTest extends ZanataTest {
    private static final Logger log = LoggerFactory.getLogger(ZanataJpaTest.class);
    private static final String PERSIST_NAME = "zanataDatasourcePU";

    private static EntityManagerFactory emf;

    protected EntityManager em;

    @Before
    public void setupEM() {
        log.debug("Setting up EM");
        emf.getCache().evictAll();
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @After
    public void shutdownEM() {
        log.debug("Shutting down EM");
        clearHibernateSecondLevelCache();
        if (rollbackBeforeClose()) {
            em.getTransaction().rollback();
        }
        if (em.isOpen()) {
            em.close();
        }
        em = null;
        emf.getCache().evictAll();
    }

    protected boolean rollbackBeforeClose() {
        return true;
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

    protected void deleteAllTables() {
        deleteAll(getEm(), ZanataEntities.entitiesForRemoval());
    }
}
