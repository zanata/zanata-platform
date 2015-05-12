package org.zanata.service.impl;

import static com.github.huangp.entityunit.entity.EntityCleaner.deleteAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.jpa.Search;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.zanata.PerformanceProfiling;
import org.zanata.SlowTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.seam.AutowireTransaction;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.CopyTransService;
import org.zanata.service.SearchIndexManager;
import org.zanata.util.ZanataEntities;

import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.google.common.collect.ImmutableMap;

/**
 * This is a JUnit test that will setup large data and test the performance of
 * copyTrans. JUnit will NOT be executed by maven at the moment (only testNg)
 * which is desirable right now, as we don't have a benchmark and do not want to
 * slow down our build.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class CopyTransServiceImplPerformanceTest {
    private static final String PERSIST_NAME = "zanataTestDatasourcePU";
    private static final String MYSQL_TEST_DB_URL =
            "jdbc:log4jdbc:mysql://localhost:3306/zanata_unit_test?characterEncoding=UTF-8";
    private static final String MYSQL_DIALECT =
            MySQL5Dialect.class.getCanonicalName();

    protected static boolean useMysql = true;
    // NOTE: if you use mysql and you have the schema created, just change it to
    // update will speed up subsequent test run
    // private static String hbm2ddl = "create";
    protected static String hbm2ddl = "update";
    // protected static String hbm2ddl = "validate";
    private static final Map<String, String> MySQLProps =
            ImmutableMap
                    .<String, String> builder()
                    .put(Environment.URL, MYSQL_TEST_DB_URL)
                    .put(Environment.USER, "root")
                    .put(Environment.PASS, "root")
                    .put(Environment.HBM2DDL_AUTO, "")
                    // .put(Environment.USE_DIRECT_REFERENCE_CACHE_ENTRIES,
                    // "true")
                    // .put(Environment.DEFAULT_BATCH_FETCH_SIZE, "50")
                    // .put(Environment.STATEMENT_BATCH_SIZE, "50")
                    // .put(Environment.USE_SECOND_LEVEL_CACHE, "false")
                    // .put(Environment.USE_QUERY_CACHE, "false")
                    .put(Environment.DIALECT, MYSQL_DIALECT).build();

    @ClassRule
    public static TestRule entityManagerFactoryRule = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            log.debug("Initializing EMF");
            emf =
                    Persistence.createEntityManagerFactory(PERSIST_NAME,
                            useMysql ? MySQLProps : null);
        }

        @Override
        protected void after() {
            log.debug("Shutting down EMF");
            emf.close();
            emf = null;
        }
    };

    @Rule
    public TestRule emRule = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            log.debug("Setting up EM");
            em = emf.createEntityManager();
            em.getTransaction().begin();
        }

        @Override
        protected void after() {
            log.debug("Shutting down EM");
            clearHibernateSecondLevelCache();
            em.getTransaction().commit();
            if (em.isOpen()) {
                em.close();
            }
            em = null;
        }

        /**
         * Clears the Hibernate Second Level cache.
         */
        private void clearHibernateSecondLevelCache() {
            SessionFactory sessionFactory =
                    ((Session) em.getDelegate()).getSessionFactory();
            try {
                sessionFactory.getCache().evictEntityRegions();
                sessionFactory.getCache().evictCollectionRegions();
            } catch (Exception e) {
                System.out.println(" *** Cache Exception " + e.getMessage());
            }
        }
    };

    private static final Context jndiContext = mock(Context.class);
    @ClassRule
    public static MockInitialContextRule initialContextRule =
            new MockInitialContextRule(
                    jndiContext);

    private static EntityManagerFactory emf;
    protected EntityManager em;
    private int numOfTextFlows;
    private CopyTransService copyTransService;
    private HDocument copyTransTargetDoc;
    private static SeamAutowire seam = SeamAutowire.instance();

    protected EntityManager getEm() {
        return em;
    }

    protected EntityManagerFactory getEmf() {
        return emf;
    }

    protected Session getSession() {
        return (Session) em.getDelegate();
    }

    @Before
    public void setUp() throws Exception {
        // runLiquibase();

        seam.reset()
                .use("entityManager", Search.getFullTextEntityManager(getEm()))
                .use("entityManagerFactory", getEmf())
                .use("session", new FullTextSessionImpl(getSession()))
                .use(JpaIdentityStore.AUTHENTICATED_USER,
                        seam.autowire(AccountDAO.class).getByUsername("demo"))
                .useImpl(LocaleServiceImpl.class)
                .useImpl(TranslationMemoryServiceImpl.class)
                .useImpl(VersionStateCacheImpl.class)
                .useImpl(ValidationServiceImpl.class).ignoreNonResolvable();

        seam.autowire(SearchIndexManager.class).reindex(true, true, false);
        AutowireTransaction.instance().rollback();

        copyTransService = seam.autowire(CopyTransServiceImpl.class);

        deleteAll(getEm(), ZanataEntities.entitiesForRemoval());

        HLocale enUS = makeLocale(LocaleId.EN_US);
        HLocale de = makeLocale(LocaleId.DE);
        makeLocale(LocaleId.FR);
        makeLocale(LocaleId.ES);
        // makeLocale(new LocaleId("zh"));
        // makeLocale(new LocaleId("ja"));
        // makeLocale(new LocaleId("pl"));

        HDocument oldDoc =
                getEntityMakerBuilder()
                        .reuseEntity(enUS).build()
                        .makeAndPersist(getEm(), HDocument.class);

        HProject theProject = oldDoc.getProjectIteration().getProject();

        copyTransTargetDoc = getEntityMakerBuilder().reuseEntities(theProject,
                enUS).build()
                .makeAndPersist(getEm(), HDocument.class);
        // ensure two documents are from different version but same project
        // Assertions.assertThat(oldDoc.getProjectIteration().getId())
        // .isNotEqualTo(copyTransTargetDoc.getProjectIteration().getId());
        // Assertions
        // .assertThat(oldDoc.getProjectIteration().getProject().getId())
        // .isEqualTo(
        // copyTransTargetDoc.getProjectIteration().getProject()
        // .getId());

        HTextFlowBuilder textFlowBuilderForOldDoc =
                new HTextFlowBuilder().withDocument(oldDoc)
                        .withTargetLocale(enUS);

        // make many text flows all with translations in DE
        numOfTextFlows = 5000;
        for (int i = 0; i < numOfTextFlows; i++) {
            textFlowBuilderForOldDoc.withResId("res" + i)
                    .withSourceContent("source " + i)
                    .withTargetContent("target " + i)
                    .withTargetLocale(de)
                    .withTargetState(ContentState.Translated).build();
        }

        HTextFlowBuilder textFlowBuilderForNewDoc =
                new HTextFlowBuilder().withDocument(copyTransTargetDoc)
                        .withTargetLocale(enUS);
        for (int i = 0; i < numOfTextFlows; i++) {
            textFlowBuilderForNewDoc.withResId("res" + i)
                    .withSourceContent("source " + i).build();
        }
        getEm().flush();
        getEm().clear();

        // Long totalTextFlows =
        // getEm().createQuery("select count(*) from HTextFlow",
        // Long.class).getSingleResult();
        //
        // Assertions.assertThat(totalTextFlows).isEqualTo(numOfTextFlows * 2);
        // Long totalTranslation =
        // getEm().createQuery("select count(*) from HTextFlowTarget",
        // Long.class).getSingleResult();
        // Assertions.assertThat(totalTranslation).isEqualTo(numOfTextFlows);

        // enable logging
        LogManager.getLogger(CopyTransServiceImpl.class.getPackage().getName())
                .setLevel(Level.DEBUG);
    }

    // This method will ensure database schema is up to date
    private static void runLiquibase() throws Exception {
        when(
                jndiContext
                        .lookup("java:global/zanata/files/document-storage-directory"))
                .thenReturn("/tmp/doc");
        Connection conn = DriverManager
                .getConnection(MYSQL_TEST_DB_URL, "root", "root");

        Statement statement = conn.createStatement();
        statement.executeUpdate("drop schema zanata_unit_test");
        statement.executeUpdate("create schema zanata_unit_test");
        statement.executeUpdate("use zanata_unit_test");

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(conn));

        Liquibase liquibase = new Liquibase("db/db.changelog.xml",
                new ClassLoaderResourceAccessor(), database);
        liquibase.update("");
    }

    private HLocale makeLocale(LocaleId localeId) {
        HLocale hLocale = new HLocale(localeId);
        hLocale.setActive(true);
        hLocale.setEnabledByDefault(true);
        getEm().persist(hLocale);
        return hLocale;
    }

    private static EntityMakerBuilder getEntityMakerBuilder() {
        return EntityMakerBuilder
                .builder()
                .addFieldOrPropertyMaker(HProject.class,
                        "sourceViewURL",
                        FixedValueMaker.EMPTY_STRING_MAKER)
                .addConstructorParameterMaker(HDocument.class, 0,
                        FixedValueMaker.fix("doc1"))
                .addConstructorParameterMaker(HDocument.class, 1,
                        FixedValueMaker.fix("doc1"))
                .addConstructorParameterMaker(HDocument.class, 2,
                        FixedValueMaker.EMPTY_STRING_MAKER);
    }

    @Test
    @Ignore
    @SlowTest
    @PerformanceProfiling
    public void testCopyTransForDocument() throws Exception {
        HCopyTransOptions options =
                new HCopyTransOptions(
                        HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY,
                        HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY,
                        HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY);
        copyTransService
                .copyTransForDocument(copyTransTargetDoc, options, null);

        Long totalTranslation =
                getEm().createQuery("select count(*) from HTextFlowTarget",
                        Long.class).getSingleResult();
        Assertions.assertThat(totalTranslation).isEqualTo(numOfTextFlows * 2);
    }

    public static class MockInitialContextFactory implements
            InitialContextFactory {

        private static final ThreadLocal<Context> currentContext =
                new ThreadLocal<Context>();

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws
                NamingException {
            return currentContext.get();
        }

        public static void setCurrentContext(Context context) {
            currentContext.set(context);
        }

        public static void clearCurrentContext() {
            currentContext.remove();
        }

    }

    public static class MockInitialContextRule extends ExternalResource {

        private final Context context;

        private MockInitialContextRule(Context context) {
            this.context = context;
        }

        @Override
        protected void before() throws Throwable {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    MockInitialContextFactory.class.getName());
            MockInitialContextFactory.setCurrentContext(context);
        }

        @Override
        protected void after() {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
            MockInitialContextFactory.clearCurrentContext();
        }
    }

}
