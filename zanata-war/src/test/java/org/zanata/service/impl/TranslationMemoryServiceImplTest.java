package org.zanata.service.impl;

import java.util.Date;
import java.util.List;

import com.binarytweed.test.DelegateRunningTo;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import lombok.ToString;
import org.assertj.core.api.Condition;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.jpa.Search;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.zanata.ImmutableDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.TestFixture;
import org.zanata.model.po.HPotEntryData;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.SearchIndexManager;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
// see also TranslationFinderTest
@DelegateRunningTo(DataProviderRunner.class)
public class TranslationMemoryServiceImplTest extends ImmutableDbunitJpaTest {
    private SeamAutowire seam = SeamAutowire.instance();
    private TranslationMemoryServiceImpl service;

    private HLocale sourceLocale;
    private HLocale targetLocale;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TranslationMemoryData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        service =
                seam.reset()
                        .use("entityManager",
                                Search.getFullTextEntityManager(getEm()))
                        .use("entityManagerFactory", getEmf())
                        .use("session", new FullTextSessionImpl(getSession()))
                        .useImpl(IndexingServiceImpl.class)
                        .ignoreNonResolvable()
                        .autowire(TranslationMemoryServiceImpl.class);
        seam.autowire(SearchIndexManager.class).reindex(true, true, false);
        LocaleDAO localeDAO = seam.autowire(LocaleDAO.class);

        sourceLocale = localeDAO.findByLocaleId(LocaleId.EN_US);
        targetLocale = localeDAO.findByLocaleId(LocaleId.DE);
    }

    @Test
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
        assertThat(detail.getDocId()).isEqualTo(
                hTextFlow.getDocument().getDocId());
        assertThat(detail.getLastModifiedBy()).isEqualTo("admin");
    }

    @Test
    public void basicNonPluralTMSearchTest() {
        final String searchString = "file";
        TransMemoryQuery tmQuery =
                new TransMemoryQuery(searchString,
                        HasSearchType.SearchType.FUZZY);

        List<TransMemoryResultItem> results =
                service.searchTransMemory(targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), tmQuery);

        assertThat(results).hasSize(3);

        checkSourceContainQuery(results, searchString);
    }

    @Test
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
    public void searchBestMatchTMTest() {
        TextFlowDAO textFlowDAO = seam.autowire(TextFlowDAO.class);

        // content0="Yet Another File removed" content1="%d files removed"
        // best matches has 83.33% similarity
        HTextFlow textFlow = textFlowDAO.findById(105L, false);
        assert textFlow != null;

        executeFindBestTMMatch(textFlow, 70, true);
        executeFindBestTMMatch(textFlow, 80, true);
        executeFindBestTMMatch(textFlow, 90, false);
    }

    @Test
    public void searchBestMatchTMTest2() {
        TextFlowDAO textFlowDAO = seam.autowire(TextFlowDAO.class);

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
                        targetLocale.getLocaleId(), sourceLocale.getLocaleId(),
                        false, false, false, threshold);
        assertThat(match.isPresent()).isEqualTo(hasMatch);
    }

    @Test
    @UseDataProvider("tmTestParams")
    public void testTMSearch(TransMemoryExecution exec) {

        List<TransMemoryResultItem> results =
                service.searchTransMemory(targetLocale.getLocaleId(),
                        sourceLocale.getLocaleId(), exec.getQuery());

        assertThat(results).hasSize(exec.getResultSize());
    }

    @DataProvider
    public static Object[][] tmTestParams() {
        // Should return 1 records if all checked
        String validQuery = "file";
        String validProjectSlug = "same-project";
        String validDocId = "/same/document0";
        String validResId = "bbb5da9ad2bd9d24df29caead537b840";

        List<TransMemoryExecution> executions = Lists.newArrayList();

        executions.add(createExecution(validQuery, new Boolean[] { true, true,
                true },
                new String[] { validProjectSlug, validDocId, validResId }, 1));

        executions.add(createExecution(validQuery, new Boolean[] { true, true,
                false }, new String[] { validProjectSlug, validDocId,
                validResId }, 2));

        executions.add(createExecution(validQuery, new Boolean[] { false,
                false, false }, new String[] { validProjectSlug, validDocId,
                validResId }, 3));

        executions.add(createExecution(validQuery, new Boolean[] { true, false,
                false }, new String[] { validProjectSlug, validDocId,
                validResId }, 3));

        executions.add(createExecution(validQuery, new Boolean[] { true, false,
                true },
                new String[] { validProjectSlug, validDocId, validResId }, 1));

        executions.add(createExecution(validQuery, new Boolean[] { false, true,
                true },
                new String[] { validProjectSlug, validDocId, validResId }, 1));

        executions.add(createExecution(validQuery, new Boolean[] { false,
                false, true }, new String[] { validProjectSlug, validDocId,
                validResId }, 1));

        executions.add(createExecution(validQuery, new Boolean[] { false, true,
                false }, new String[] { validProjectSlug, validDocId,
                validResId }, 2));

        Object[][] val = new Object[executions.size()][1];
        int i = 0;
        for (TransMemoryExecution exec : executions) {
            val[i++][0] = exec;
        }
        return val;
    }

    private static TransMemoryExecution createExecution(String query,
            Boolean[] checks, String[] values, int expectedSize) {

        return new TransMemoryExecution(new TransMemoryQuery(query,
                HasSearchType.SearchType.FUZZY, getCondition(checks[0],
                        values[0]), getCondition(checks[1], values[1]),
                getCondition(checks[2], values[2])), expectedSize);
    }

    @Getter
    @AllArgsConstructor
    @ToString
    private static class TransMemoryExecution {

        private TransMemoryQuery query;
        private int resultSize;
    }

    // to check if any of the sourceContents contain searchString
    private void checkSourceContainQuery(List<TransMemoryResultItem> results,
            final String searchString) {
        assertThat(results).extracting("sourceContents").has(
                new Condition<List<Object>>() {
                    @Override
                    public boolean matches(List<Object> value) {
                        for (Object obj : value) {
                            for (String content : (List<String>) obj) {
                                if (content.contains(searchString)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });
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
