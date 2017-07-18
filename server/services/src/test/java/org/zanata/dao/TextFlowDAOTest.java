package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.webtrans.shared.search.FilterConstraints;
import org.zanata.webtrans.shared.model.DocumentId;

public class TextFlowDAOTest extends ZanataDbunitJpaTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TextFlowDAOTest.class);

    private static final boolean PRINT_TEST_DATA = false;
    private TextFlowDAO dao;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setup() {
        dao = new TextFlowDAO((Session) getEm().getDelegate());
        if (PRINT_TEST_DATA) {
            printTestData();
        }
    }

    private void printTestData() {
        // single text flow with 4 targets
        HTextFlow textFlow = dao.findById(1L, false);
        log.info("text flow: {}", textFlow);
        for (Map.Entry<Long, HTextFlowTarget> entry : textFlow.getTargets()
                .entrySet()) {
            log.debug("locale id: {} - target state: {}", entry.getKey(),
                    entry.getValue().getState());
        }
        // 3 text flows with single en-US fuzzy target
        // List<HTextFlow> doc2TextFlows = dao.getTextFlowsByDocumentId(new
        // DocumentId(2L, ""), hLocale, 0, 9999);
        // for (HTextFlow doc2tf : doc2TextFlows)
        // {
        // log.debug("text flow id {} - targets {}", doc2tf.getId(),
        // doc2tf.getTargets());
        // }
        // single text flow no target
        HTextFlow textFlow6 = dao.findById(6L, false);
        log.debug("text flow {} target: {}", textFlow6.getId(),
                textFlow6.getTargets());
    }
    // FIXME looks like this test does not take more recently added states into
    // account
    // should ensure all states are in test data and check test logic

    @Test
    public void canGetAllUntranslatedTextFlowForADocument() {
        HLocale deLocale = getEm().find(HLocale.class, 3L);
        log.info("locale: {}", deLocale);
        FilterConstraints untranslated = FilterConstraints.builder().keepAll()
                .excludeFuzzy().excludeTranslated().build();
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                new DocumentId(1L, ""), deLocale, untranslated, 0, 10);
        assertThat(result.size(), is(0));
        HLocale frLocale = getEm().find(HLocale.class, 6L);
        result = dao.getTextFlowByDocumentIdWithConstraints(
                new DocumentId(1L, ""), frLocale, untranslated, 0, 10);
        assertThat(result.size(), is(1));
    }

    @Test
    public void canGetTextFlowWithNullTarget() {
        HLocale deLocale = getEm().find(HLocale.class, 3L);
        FilterConstraints untranslated = FilterConstraints.builder().keepAll()
                .excludeFuzzy().excludeTranslated().build();
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                new DocumentId(4L, ""), deLocale, untranslated, 0, 10);
        assertThat(result, Matchers.hasSize(1));
    }

    @Test
    public void canGetTextFlowsByStatusNotNew() {
        HLocale enUSLocale = getEm().find(HLocale.class, 4L);
        // all 3 text flows are fuzzy for en-US in this document
        DocumentId documentId2 = new DocumentId(2L, "");
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                documentId2, enUSLocale,
                FilterConstraints.builder().keepAll().excludeNew().build(), 0,
                10);
        assertThat(result, Matchers.hasSize(3));
    }

    @Test
    public void canGetTextFlowsByStatusNotFuzzy() {
        // frLocale new in this document
        DocumentId documentId = new DocumentId(1L, "");
        HLocale frLocale = getEm().find(HLocale.class, 6L);
        FilterConstraints notFuzzy =
                FilterConstraints.builder().keepAll().excludeFuzzy().build();
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                documentId, frLocale, notFuzzy, 0, 10);
        assertThat(result, Matchers.hasSize(1));
    }

    @Test
    public void canGetTextFlowsByStatusNotTranslatedNotNew() {
        // esLocale fuzzy in this document
        DocumentId documentId = new DocumentId(1L, "");
        HLocale esLocale = getEm().find(HLocale.class, 5L);
        FilterConstraints notNewOrTranslated = FilterConstraints.builder()
                .keepAll().excludeTranslated().excludeNew().build();
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                documentId, esLocale, notNewOrTranslated, 0, 10);
        assertThat(result, Matchers.hasSize(1));
    }

    @Test
    public void canGetTextFlowsByStatusNotFuzzyNotNew() {
        // deLocale approved in this document
        DocumentId documentId = new DocumentId(1L, "");
        HLocale deLocale = getEm().find(HLocale.class, 3L);
        FilterConstraints notNewOrFuzzy = FilterConstraints.builder().keepAll()
                .excludeFuzzy().excludeNew().build();
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                documentId, deLocale, notNewOrFuzzy, 0, 10);
        assertThat(result, Matchers.hasSize(1));
    }

    @Test
    public void canGetTextFlowsByStatusNotFuzzyNotTranslated() {
        HLocale enUSLocale = getEm().find(HLocale.class, 4L);
        // all 3 text flows are fuzzy for en-US in this document
        DocumentId documentId2 = new DocumentId(2L, "");
        FilterConstraints notFuzzyOrTranslated = FilterConstraints.builder()
                .keepAll().excludeTranslated().excludeFuzzy().build();
        List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
                documentId2, enUSLocale, notFuzzyOrTranslated, 0, 10);
        assertThat(result, Matchers.<HTextFlow> empty());
    }

    @Ignore
    @Test
    public void thisBreaksForSomeReason() {
        // fails regardless of using different documentId, locale or constraints
        DocumentId id = new DocumentId(1L, "");
        HLocale locale = getEm().find(HLocale.class, 3L);
        FilterConstraints constraints = FilterConstraints.builder().build();
        dao.getTextFlowByDocumentIdWithConstraints(id, locale, constraints, 0,
                10);
        dao.getTextFlowByDocumentIdWithConstraints(id, locale, constraints, 0,
                10);
    }

    @Test
    public void testGetTextFlowByDocumentIdWithConstraint() {
        HLocale deLocale = getEm().find(HLocale.class, 3L);
        List<HTextFlow> result =
                dao.getTextFlowByDocumentIdWithConstraints(
                        new DocumentId(new Long(4), ""), deLocale,
                        FilterConstraints.builder().filterBy("mssg")
                                .excludeTranslated().excludeFuzzy().build(),
                        0, 10);
        assertThat(result, Matchers.hasSize(1));
    }

    @Test
    public void testGetTranslationsByMatchedContext() {
        List<DataSetOperation> testOperations = Lists.newArrayList();
        testOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        testOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        testOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        testOperations.add(new DataSetOperation(
                "org/zanata/test/model/MergeTranslationsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        for (DataSetOperation operation : testOperations) {
            operation.prepare(this);
        }
        executeOperations(testOperations);
        String projectSlug = "sample-project";
        String fromVersionSlug = "1.0";
        String toVersionSlug = "2.0";
        ProjectIterationDAO projectIterationDAO =
                new ProjectIterationDAO((Session) getEm().getDelegate());
        HProjectIteration fromVersion =
                projectIterationDAO.getBySlug(projectSlug, fromVersionSlug);
        Assertions.assertThat(fromVersion).isNotNull();
        HProjectIteration toVersion =
                projectIterationDAO.getBySlug(projectSlug, toVersionSlug);
        Assertions.assertThat(toVersion).isNotNull();
        List<HTextFlow[]> results = dao.getSourceByMatchedContext(
                fromVersion.getId(), toVersion.getId(), 0, 100);
        Assertions.assertThat(results).isNotEmpty();
        for (HTextFlow[] result : results) {
            Assertions.assertThat(result[0].getContentHash())
                    .isEqualTo(result[1].getContentHash());
            Assertions.assertThat(result[0].getDocument().getDocId())
                    .isEqualTo(result[1].getDocument().getDocId());
            Assertions.assertThat(result[0].getResId())
                    .isEqualTo(result[1].getResId());
            Assertions.assertThat(result[0]).isNotEqualTo(result[1]);
        }
    }
}
