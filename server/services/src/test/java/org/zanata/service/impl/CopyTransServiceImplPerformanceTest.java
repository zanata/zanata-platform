package org.zanata.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jglue.cdiunit.AdditionalClasses;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.PerformanceProfiling;
import org.zanata.SlowTest;
import org.zanata.ZanataTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.EntityManagerFactoryRule;
import org.zanata.service.MockInitialContextRule;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.ZanataEntities;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import static com.github.huangp.entityunit.entity.EntityCleaner.deleteAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zanata.service.EntityManagerFactoryRule.mySqlPassword;
import static org.zanata.service.EntityManagerFactoryRule.mySqlUrl;
import static org.zanata.service.EntityManagerFactoryRule.mySqlUsername;

/**
 * This is a JUnit test that will setup large data and test the performance of
 * copyTrans.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class, VersionStateCacheImpl.class,
        ValidationServiceImpl.class })
public class CopyTransServiceImplPerformanceTest extends ZanataTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(CopyTransServiceImplPerformanceTest.class);

    @ClassRule
    public static EntityManagerFactoryRule emfRule =
            new EntityManagerFactoryRule(
                    EntityManagerFactoryRule.TestProfile.ManualPerformanceProfiling);
    private static final Context jndiContext = mock(Context.class);
    @ClassRule
    public static MockInitialContextRule initialContextRule =
            new MockInitialContextRule(jndiContext);
    protected EntityManager em;
    private int numOfTextFlows;
    @Inject
    private CopyTransServiceImpl copyTransService;
    private HDocument copyTransTargetDoc;

    @Produces
    protected EntityManager getEm() {
        return em;
    }

    @Produces
    @FullText
    FullTextEntityManager getFullTextEntityManager() {
        return Search.getFullTextEntityManager(getEm());
    }

    @Produces
    protected EntityManagerFactory getEmf() {
        return emfRule.getEmf();
    }

    @Produces
    protected Session getSession() {
        return (Session) em.getDelegate();
    }

    @Produces
    @Authenticated
    HAccount getAuthenticatedAccount(AccountDAO accountDAO) {
        return accountDAO.getByUsername("demo");
    }

    @Before
    public void setUp() throws Exception {
        // runLiquibase();
        log.debug("Setting up EM");
        em = emfRule.getEmf().createEntityManager();
        em.getTransaction().begin();
        deleteAll(getEm(), ZanataEntities.entitiesForRemoval());
        HLocale enUS = makeLocale(LocaleId.EN_US);
        HLocale de = makeLocale(LocaleId.DE);
        makeLocale(LocaleId.FR);
        makeLocale(LocaleId.ES);
        // makeLocale(new LocaleId("zh"));
        // makeLocale(new LocaleId("ja"));
        // makeLocale(new LocaleId("pl"));
        HDocument oldDoc = getEntityMakerBuilder().reuseEntity(enUS).build()
                .makeAndPersist(getEm(), HDocument.class);
        HProject theProject = oldDoc.getProjectIteration().getProject();
        copyTransTargetDoc =
                getEntityMakerBuilder().reuseEntities(theProject, enUS).build()
                        .makeAndPersist(getEm(), HDocument.class);
        // ensure two documents are from different version but same project
        // Assertions.assertThat(oldDoc.getProjectIteration().getId())
        // .isNotEqualTo(copyTransTargetDoc.getProjectIteration().getId());
        // Assertions
        // .assertThat(oldDoc.getProjectIteration().getProject().getId())
        // .isEqualTo(
        // copyTransTargetDoc.getProjectIteration().getProject()
        // .getId());
        HTextFlowBuilder textFlowBuilderForOldDoc = new HTextFlowBuilder()
                .withDocument(oldDoc).withTargetLocale(enUS);
        // make many text flows all with translations in DE
        numOfTextFlows = 5000;
        for (int i = 0; i < numOfTextFlows; i++) {
            textFlowBuilderForOldDoc.withResId("res" + i)
                    .withSourceContent("source " + i)
                    .withTargetContent("target " + i).withTargetLocale(de)
                    .withTargetState(ContentState.Translated).build();
        }
        HTextFlowBuilder textFlowBuilderForNewDoc = new HTextFlowBuilder()
                .withDocument(copyTransTargetDoc).withTargetLocale(enUS);
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

    @After
    public void tearDown() {
        log.debug("Shutting down EM");
        SessionFactory sessionFactory =
                ((Session) em.getDelegate()).getSessionFactory();
        try {
            sessionFactory.getCache().evictEntityRegions();
            sessionFactory.getCache().evictCollectionRegions();
        } catch (Exception e) {
            log.error(" *** Cache Exception " + e.getMessage());
        }
        em.getTransaction().commit();
        if (em.isOpen()) {
            em.close();
        }
        em = null;
    }
    // This method will ensure database schema is up to date

    private static void runLiquibase() throws Exception {
        when(jndiContext
                .lookup("java:global/zanata/files/document-storage-directory"))
                        .thenReturn("/tmp/doc");
        Connection conn = DriverManager.getConnection(mySqlUrl(),
                mySqlUsername(), mySqlPassword());
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
        return EntityMakerBuilder.builder()
                .addFieldOrPropertyMaker(HProject.class, "sourceViewURL",
                        FixedValueMaker.EMPTY_STRING_MAKER)
                .addConstructorParameterMaker(HDocument.class, 0,
                        FixedValueMaker.fix("doc1"))
                .addConstructorParameterMaker(HDocument.class, 1,
                        FixedValueMaker.fix("doc1"))
                .addConstructorParameterMaker(HDocument.class, 2,
                        FixedValueMaker.EMPTY_STRING_MAKER);
    }

    @Ignore("slow test")
    @Test
    @SlowTest
    @PerformanceProfiling
    public void testCopyTransForDocument() throws Exception {
        HCopyTransOptions options = new HCopyTransOptions(
                HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY,
                HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY,
                HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY);
        copyTransService.copyTransForDocument(copyTransTargetDoc, options,
                null);
        Long totalTranslation = getEm()
                .createQuery("select count(*) from HTextFlowTarget", Long.class)
                .getSingleResult();
        Assertions.assertThat(totalTranslation).isEqualTo(numOfTextFlows * 2);
    }
}
