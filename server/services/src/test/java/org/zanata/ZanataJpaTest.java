package org.zanata;

import static com.github.huangp.entityunit.entity.EntityCleaner.deleteAll;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.stat.Statistics;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.ZanataEntities;

// single threaded because of ehcache (perhaps other reasons too)
//@Test(singleThreaded = true)
public abstract class ZanataJpaTest extends ZanataTest {
    private static final Logger log = LoggerFactory.getLogger(ZanataJpaTest.class);
    private static final String PERSIST_NAME = "zanataDatasourcePU";

    private static HibernateEntityManagerFactory emf;

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

    protected HibernateEntityManagerFactory getEmf() {
        return emf;
    }

    protected Session getSession() {
        return (Session) em.getDelegate();
    }

    @BeforeClass
    public static void initializeEMF() {
        log.debug("Initializing EMF");
        emf =
                (HibernateEntityManagerFactory) Persistence.createEntityManagerFactory(PERSIST_NAME,
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

    /**
     * Misnomer: deletes all table data, but not the tables themselves
     */
    protected void deleteAllTables() {
        doInTransaction((em) -> {
            deleteAll(em, ZanataEntities.entitiesForRemoval());
        });
    }

    protected void purgeLuceneIndexes() {
        purgeLuceneIndexes(em);
    }

    // see also org.zanata.util.SampleDataProfile.purgeLuceneIndexes
    public static void purgeLuceneIndexes(EntityManager em) {
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        ftem.purgeAll(Object.class);
        ftem.flushToIndexes();

        Statistics stats = ftem.getSearchFactory().getStatistics();
        stats.getIndexedClassNames().forEach(clazz  -> {
            int entities = stats.getNumberOfIndexedEntities(clazz);
            if (entities != 0) {
                String msg = "Purge failed to remove " + entities +
                        " entities from Lucene index for " + clazz +
                        ". This may affect later tests.";
                throw new RuntimeException(msg);
            }
        });
    }

    protected void doInTransaction(Consumer<EntityManager> function) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();
        function.accept(em);
        em.getTransaction().commit();
    }
}
