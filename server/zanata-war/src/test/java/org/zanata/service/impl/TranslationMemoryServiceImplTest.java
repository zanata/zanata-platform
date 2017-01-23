package org.zanata.service.impl;

import com.google.common.collect.Lists;
import org.assertj.core.api.Condition;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.TestFixture;
import org.zanata.model.po.HPotEntryData;
import org.zanata.test.CdiUnitRunner;
import org.zanata.test.CdiUnitRunnerWithParameters;
import org.zanata.test.DBUnitDataSetRunner;
import org.zanata.test.ParamTestCdiExtension;
import org.zanata.test.rule.DataSetOperation;
import org.zanata.test.rule.JpaRule;
import org.zanata.util.UrlUtil;
import org.zanata.util.Zanata;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.test.rule.FunctionalTestRule.reentrant;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link TranslationFinderTest}
 */
@RunWith(Enclosed.class)
public class TranslationMemoryServiceImplTest {

    @RunWith(CdiUnitRunner.class)
    @AdditionalClasses({ IndexingServiceImpl.class })
    public static class TranslationMemoryServiceNonParameterizedTest {

        @ClassRule
        @Rule
        public static JpaRule jpaRule = new JpaRule();
        private HLocale sourceLocale;
        private HLocale targetLocale;
        @Inject
        TranslationMemoryServiceImpl service;
        @Inject
        TextFlowDAO textFlowDAO;
        @Inject
        LocaleDAO localeDAO;
        @Produces
        @Mock
        private UrlUtil urlUtil;

        @Produces
        @Zanata
        EntityManagerFactory getEntityManagerFactory() {
            return jpaRule.getEntityManagerFactory();
        }

        @Produces
        @FullText
        FullTextEntityManager getFullTextEntityManager() {
            return Search.getFullTextEntityManager(jpaRule.getEntityManager());
        }

        @Produces
        EntityManager getEntityManager() {
            return jpaRule.getEntityManager();
        }

        @Produces
        Session getSession() {
            return jpaRule.getSession();
        }

        @Before
        @InRequestScope
        public void prepareDBUnitOperations() {
            new DBUnitDataSetRunner(jpaRule.getEntityManager())
                    .runDataSetOperations(
                            new DataSetOperation(
                                    "org/zanata/test/model/ClearAllTables.dbunit.xml",
                                    DatabaseOperation.CLEAN_INSERT),
                            new DataSetOperation(
                                    "org/zanata/test/model/LocalesData.dbunit.xml",
                                    DatabaseOperation.CLEAN_INSERT),
                            new DataSetOperation(
                                    "org/zanata/test/model/TranslationMemoryData.dbunit.xml",
                                    DatabaseOperation.CLEAN_INSERT));
            sourceLocale = localeDAO.findByLocaleId(LocaleId.EN_US);
            targetLocale = localeDAO.findByLocaleId(LocaleId.DE);
        }

        @Test
        @InRequestScope
        public void testGetTransMemoryDetail() {
            HTextFlow hTextFlow =
                    TestFixture.makeApprovedHTextFlow(1, sourceLocale);
            HPotEntryData potEntryData = new HPotEntryData();
            potEntryData.setContext("msgContext");
            hTextFlow.setPotEntryData(potEntryData);
            hTextFlow.getTargets().put(sourceLocale.getId(),
                    addHTextFlowTarget(sourceLocale, hTextFlow, "admin"));
            setProjectAndIterationSlug(hTextFlow, "project", "master");
            TransMemoryDetails detail =
                    service.getTransMemoryDetail(sourceLocale, hTextFlow);
            assertThat(detail.getMsgContext()).isEqualTo("msgContext");
            assertThat(detail.getProjectName()).isEqualTo("project");
            assertThat(detail.getIterationName()).isEqualTo("master");
            assertThat(detail.getDocId())
                    .isEqualTo(hTextFlow.getDocument().getDocId());
            assertThat(detail.getLastModifiedBy()).isEqualTo("admin");
        }

        @Test
        @InRequestScope
        public void basicNonPluralTMSearchTest() {
            final String searchString = "file";
            TransMemoryQuery tmQuery = new TransMemoryQuery(searchString,
                    HasSearchType.SearchType.FUZZY);
            List<TransMemoryResultItem> results =
                    service.searchTransMemory(targetLocale.getLocaleId(),
                            sourceLocale.getLocaleId(), tmQuery);
            assertThat(results).hasSize(3);
            checkSourceContainQuery(results, searchString);
        }

        @Test
        @InRequestScope
        public void basicPluralTMSearchTest() {
            String query1 = "One file removed_";
            String query2 = "%d";
            TransMemoryQuery tmQuery =
                    new TransMemoryQuery(Lists.newArrayList(query1, query2),
                            HasSearchType.SearchType.FUZZY_PLURAL);
            List<TransMemoryResultItem> results =
                    service.searchTransMemory(targetLocale.getLocaleId(),
                            sourceLocale.getLocaleId(), tmQuery);
            assertThat(results).hasSize(3);
            checkSourceContainQuery(results, query1);
            checkSourceContainQuery(results, query2);
        }

        @Test
        @InRequestScope
        public void searchBestMatchTMTest() {
            // content0="Yet Another File removed" content1="%d files removed"
            // best matches has 83.33% similarity
            HTextFlow textFlow = textFlowDAO.findById(105L, false);
            assert textFlow != null;
            executeFindBestTMMatch(textFlow, 70, true);
            executeFindBestTMMatch(textFlow, 80, true);
            executeFindBestTMMatch(textFlow, 90, false);
        }

        @Test
        @InRequestScope
        public void searchBestMatchTMTest2() {
            // content0="One file removed" content1="%d files removed"
            // best matches has 100% similarity
            HTextFlow textFlow = textFlowDAO.findById(101L, false);
            assert textFlow != null;
            executeFindBestTMMatch(textFlow, 80, true);
            executeFindBestTMMatch(textFlow, 90, true);
            executeFindBestTMMatch(textFlow, 100, true);
        }

        private void executeFindBestTMMatch(HTextFlow textFlow, int threshold,
                boolean hasMatch) {
            Optional<TransMemoryResultItem> match =
                    service.searchBestMatchTransMemory(textFlow,
                            targetLocale.getLocaleId(),
                            sourceLocale.getLocaleId(), false, false, false,
                            threshold);
            assertThat(match.isPresent()).isEqualTo(hasMatch);
        }
        // to check if any of the sourceContents contain searchString

        private void checkSourceContainQuery(
                List<TransMemoryResultItem> results,
                final String searchString) {
            assertThat(results).extracting("sourceContents")
                    .has(new Condition<>(contentsList -> {
                        for (Object contents : contentsList) {
                            for (String content : (List<String>) contents) {
                                if (content.contains(searchString)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }, "contains \"" + searchString + "\""));
        }
    }

    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(CdiUnitRunnerWithParameters.Factory.class)
    @AdditionalClasses({ IndexingServiceImpl.class,
            ParamTestCdiExtension.class })
    public static class TranslationMemoryServiceImplParameterizedTest {

        @ClassRule
        @Rule
        public static JpaRule jpaRule = reentrant(new JpaRule());
        @Parameterized.Parameter(0)
        TransMemoryExecution exec;
        private HLocale sourceLocale;
        private HLocale targetLocale;
        @Inject
        TranslationMemoryServiceImpl service;
        @Inject
        TextFlowDAO textFlowDAO;
        @Inject
        LocaleDAO localeDAO;
        @Produces
        @Mock
        private UrlUtil urlUtil;

        @Produces
        @Zanata
        EntityManagerFactory getEntityManagerFactory() {
            return jpaRule.getEntityManagerFactory();
        }

        @Produces
        EntityManager getEntityManager() {
            return jpaRule.getEntityManager();
        }

        @Produces
        Session getSession() {
            return jpaRule.getSession();
        }

        @Produces
        @FullText
        FullTextEntityManager getFullTextEntityManager() {
            return Search.getFullTextEntityManager(jpaRule.getEntityManager());
        }

        @Before
        @InRequestScope
        public void prepareDBUnitOperations() {
            new DBUnitDataSetRunner(jpaRule.getEntityManager())
                    .runDataSetOperations(
                            new DataSetOperation(
                                    "org/zanata/test/model/ClearAllTables.dbunit.xml",
                                    DatabaseOperation.CLEAN_INSERT),
                            new DataSetOperation(
                                    "org/zanata/test/model/LocalesData.dbunit.xml",
                                    DatabaseOperation.CLEAN_INSERT),
                            new DataSetOperation(
                                    "org/zanata/test/model/TranslationMemoryData.dbunit.xml",
                                    DatabaseOperation.CLEAN_INSERT));
            sourceLocale = localeDAO.findByLocaleId(LocaleId.EN_US);
            targetLocale = localeDAO.findByLocaleId(LocaleId.DE);
        }

        @Test
        @InRequestScope
        public void testTMSearch() {
            List<TransMemoryResultItem> results =
                    service.searchTransMemory(targetLocale.getLocaleId(),
                            sourceLocale.getLocaleId(), exec.getQuery());
            assertThat(results).hasSize(exec.getResultSize());
        }

        @Parameterized.Parameters(name = "{index}: execution: [{0}]")
        public static Iterable<TransMemoryExecution> tmTestParams() {
            // Should return 1 records if all checked
            String validQuery = "file";
            String validProjectSlug = "same-project";
            String validDocId = "/same/document0";
            String validResId = "bbb5da9ad2bd9d24df29caead537b840";
            List<TransMemoryExecution> executions = Lists.newArrayList();
            executions.add(createExecution(validQuery,
                    new Boolean[] { true, true, true },
                    new String[] { validProjectSlug, validDocId, validResId },
                    1));
            executions.add(createExecution(validQuery,
                    new Boolean[] { true, true, false },
                    new String[] { validProjectSlug, validDocId, validResId },
                    2));
            executions.add(createExecution(validQuery,
                    new Boolean[] { false, false, false },
                    new String[] { validProjectSlug, validDocId, validResId },
                    3));
            executions.add(createExecution(validQuery,
                    new Boolean[] { true, false, false },
                    new String[] { validProjectSlug, validDocId, validResId },
                    3));
            executions.add(createExecution(validQuery,
                    new Boolean[] { true, false, true },
                    new String[] { validProjectSlug, validDocId, validResId },
                    1));
            executions.add(createExecution(validQuery,
                    new Boolean[] { false, true, true },
                    new String[] { validProjectSlug, validDocId, validResId },
                    1));
            executions.add(createExecution(validQuery,
                    new Boolean[] { false, false, true },
                    new String[] { validProjectSlug, validDocId, validResId },
                    1));
            executions.add(createExecution(validQuery,
                    new Boolean[] { false, true, false },
                    new String[] { validProjectSlug, validDocId, validResId },
                    2));
            return executions;
        }
    }

    private static TransMemoryExecution createExecution(String query,
            Boolean[] checks, String[] values, int expectedSize) {
        return new TransMemoryExecution(
                new TransMemoryQuery(query, HasSearchType.SearchType.FUZZY,
                        getCondition(checks[0], values[0]),
                        getCondition(checks[1], values[1]),
                        getCondition(checks[2], values[2])),
                expectedSize);
    }

    private static class TransMemoryExecution {
        private TransMemoryQuery query;
        private int resultSize;

        public TransMemoryQuery getQuery() {
            return this.query;
        }

        public int getResultSize() {
            return this.resultSize;
        }

        @java.beans.ConstructorProperties({ "query", "resultSize" })
        public TransMemoryExecution(final TransMemoryQuery query,
                final int resultSize) {
            this.query = query;
            this.resultSize = resultSize;
        }

        @Override
        public String toString() {
            return "TranslationMemoryServiceImplTest.TransMemoryExecution(query="
                    + this.getQuery() + ", resultSize=" + this.getResultSize()
                    + ")";
        }
    }

    private static TransMemoryQuery.Condition getCondition(boolean isCheck,
            String value) {
        return new TransMemoryQuery.Condition(isCheck, value);
    }

    private static HTextFlowTarget addHTextFlowTarget(HLocale hLocale,
            HTextFlow hTextFlow, String username) {
        HTextFlowTarget hTextFlowTarget =
                new HTextFlowTarget(hTextFlow, hLocale);
        HPerson lastModifiedBy = new HPerson();
        HAccount account = new HAccount();
        account.setUsername(username);
        lastModifiedBy.setAccount(account);
        hTextFlowTarget.setLastModifiedBy(lastModifiedBy);
        hTextFlowTarget.setLastChanged(new Date());
        return hTextFlowTarget;
    }

    private static void setProjectAndIterationSlug(HTextFlow hTextFlow,
            String projectSlug, String iterationSlug) {
        HProjectIteration projectIteration = new HProjectIteration();
        projectIteration.setSlug(iterationSlug);
        HProject project = new HProject();
        project.setName(projectSlug);
        projectIteration.setProject(project);
        hTextFlow.getDocument().setProjectIteration(projectIteration);
    }
}
