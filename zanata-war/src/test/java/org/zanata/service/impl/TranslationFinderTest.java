package org.zanata.service.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.service.impl.ExecutionHelper.cartesianProduct;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import org.assertj.core.api.Condition;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.jpa.Search;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.ImmutableDbunitJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.SearchIndexManager;
import org.zanata.service.TranslationFinder;

import com.google.common.base.Optional;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TranslationFinderTest extends ImmutableDbunitJpaTest {
    private SeamAutowire seam = SeamAutowire.instance();
    private HLocale sourceLocale;

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

    @BeforeClass
    public void beforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        seam.reset()
                .use("entityManager",
                        Search.getFullTextEntityManager(getEm()))
                .use("entityManagerFactory", getEmf())
                .use("session", new FullTextSessionImpl(getSession()))
                .useImpl(AsyncTaskManagerServiceImpl.class)
                .ignoreNonResolvable();
        seam.autowire(SearchIndexManager.class).reindex(true, true, false);
        LocaleDAO localeDAO = seam.autowire(LocaleDAO.class);

        sourceLocale = localeDAO.findByLocaleId(LocaleId.EN_US);
    }

    @Test
    public void testTextFlowTargetDAOMostRecent() {
        TranslationFinder service = seam.autowire(TextFlowTargetDAO.class);
        testMostRecentMatch(service);
    }

    @Test
    public void testTranslationMemoryServiceImplMostRecent() throws Exception {
        TranslationFinder service = seam.autowire(TranslationMemoryServiceImpl.class);
        testMostRecentMatch(service);
    }

    /**
     * Makes sure that given two equal results, it will reuse the most recent
     * translation.
     */
    private void testMostRecentMatch(TranslationFinder service) {
        ProjectIterationDAO projectIterationDAO =
                seam.autowire(ProjectIterationDAO.class);

        HProjectIteration version =
                projectIterationDAO.getBySlug("same-project", "same-version");
        assert version != null;

        HDocument hDoc = version.getDocuments().get("/same/document0");

        HTextFlow textFlow = hDoc.getTextFlows().get(0);
        Optional<HTextFlowTarget> match =
                service.searchBestMatchTransMemory(textFlow,
                        LocaleId.DE,
                        hDoc.getSourceLocaleId(), true, true,
                        true);
        Assert.assertTrue(match.isPresent());
        checkTargetContents(match.get(), "most recent content");
    }

    private void checkTargetContents(HTextFlowTarget target,
            final String searchString) {
        assertThat(target.getContents()).has(
                new Condition<List<String>>("contains \""+searchString+"\"") {
            @Override
            public boolean matches(List<String> strings) {
                for (String value : strings) {
                    if (value.contains(searchString)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Use this test to individually test copy trans scenarios.
     */
    @Test(enabled = false)
    public void individualTest() {
        testExecution(
                TranslationMemoryServiceImpl.class,
                new Execution(false, true, true, true, true, true));
    }

    @Test(dataProvider = "CopyTrans")
    public void testTextFlowTargetDAO(Execution execution) {
        testExecution(TextFlowTargetDAO.class, execution);
    }


    @Test(dataProvider = "CopyTrans")
    public void testTranslationMemoryServiceImpl(Execution execution) {
        testExecution(TranslationMemoryServiceImpl.class, execution);
    }


    private void testExecution(Class<? extends TranslationFinder> impl, Execution execution) {
        TranslationFinder service = seam.autowire(impl);
        // Prepare Execution
        ProjectIterationDAO iterationDAO =
                seam.autowire(ProjectIterationDAO.class);

        // Get the project iteration
        HProjectIteration queryProjIter =
                iterationDAO.getBySlug(execution.getProject(), execution.getVersion());

        assert queryProjIter != null;

        // Create the document
        HDocument queryDoc = new HDocument();
        queryDoc.setContentType(ContentType.TextPlain);
        queryDoc.setLocale(sourceLocale);
        queryDoc.setProjectIteration(queryProjIter);
        queryDoc.setFullPath(execution.getDocument());

        // Create the text Flow
        HTextFlow queryTextFlow = new HTextFlow();
        // TODO test that the query textflow is excluded from results
        // (when the query textflow has been persisted and indexed)
        queryTextFlow.setId(-999L);
        queryTextFlow.setContents(execution.getContent()); // Source content matches
        queryTextFlow.setPlural(false);
        queryTextFlow.setObsolete(false);
        queryTextFlow.setDocument(queryDoc);
        queryTextFlow.setResId(execution.getContext());

        // For all the executions whose queries are expected to find a match,
        // the target which is expected to match is HTextFlowTarget 100
        // (which belongs to HTextFlow 100, HDocument 100,
        // HProjectIteration 100, HProject 100)

        Optional<HTextFlowTarget> matchingTarget =
                service.searchBestMatchTransMemory(
                        queryTextFlow, LocaleId.DE, LocaleId.EN_US,
                        execution.isCheckContext(), execution.isCheckDocument(),
                        execution.isCheckProject());

        assertThat(matchingTarget.isPresent()).as("match present").isEqualTo(
                execution.expectMatch());

        if (matchingTarget.isPresent()) {
            HTextFlowTarget target = matchingTarget.get();
            HTextFlow tf = target.getTextFlow();
            assertThat(target.getLocaleId()).isEqualTo(LocaleId.DE);
            assertThat(tf.getContents()).containsExactly(execution.getContent());

            if (execution.isCheckContext()) {
                assertThat(tf.getResId()).isEqualTo(execution.getContext());
            }

            if (execution.isCheckDocument()) {
                assertThat(tf.getDocument().getDocId()).isEqualTo(execution.getDocument());
            }

            if (execution.isCheckProject()) {
                assertThat(tf.getDocument().getProjectIteration().getProject().getSlug()).isEqualTo(
                        execution.getProject());
            }
        }


    }

    @DataProvider(name = "CopyTrans")
    protected Object[][] createCombinations() {
        Set<Execution> expandedExecutions = generateAllExecutions();

        Object[][] val = new Object[expandedExecutions.size()][1];
        int i = 0;
        for (Execution exe : expandedExecutions) {
            val[i++][0] = exe;
        }

        return val;
    }

    private Set<Execution> generateAllExecutions() {
        Set<Execution> allExecutions =
                new HashSet<Execution>();
        List<Boolean> booleans = asList(true, false);
        Set<Object[]> paramsSet =
                cartesianProduct(booleans, booleans, booleans, booleans,
                        booleans, booleans, booleans);

        for (Object[] params : paramsSet) {
            Execution exec =
                    new Execution(
                            (Boolean) params[0], (Boolean) params[1],
                            (Boolean) params[2], (Boolean) params[3],
                            (Boolean) params[4], (Boolean) params[5]);
            allExecutions.add(exec);
        }
        return allExecutions;
    }

    @Data
    private static class Execution {
        // including this field causes Lombok @ToString to include getMatch()
        @SuppressWarnings("unused")
        private String match;

        /** Whether the query should check for matching context */
        final boolean checkContext;
        /** Whether the query should check for matching project */
        final boolean checkProject;
        /** Whether the query should check for matching doc id */
        final boolean checkDocument;
        /** Whether there should be a translation with matching context */
        final boolean matchingContext;
        /** Whether there should be a translation with matching project */
        final boolean matchingProject;
        /** Whether there should be a translation with matching doc id */
        final boolean matchingDocument;

        /** Include expectMatch() in toString() */
        String getMatch() {
            return String.valueOf(expectMatch()).toUpperCase();
        }

        /** Whether the query is expected to find a matching translation */
        boolean expectMatch() {
            return (matchingContext || !checkContext) &&
                    (matchingProject || !checkProject) &&
                    (matchingDocument || !checkDocument);
        }

        /** source content of textflow to search for */
        String getContent() {
            return "Source Content";
        }

        /** context of textflow to search for */
        String getContext() {
            return matchingContext ? "same-context" : "different-context";
        }

        /** project of textflow to search for */
        String getProject() {
            return matchingProject ? "same-project" : "different-project";
        }

        /** project version of textflow to search for */
        String getVersion() {
            return matchingProject ? "same-version" : "different-version";
        }

        /** document of textflow to search for */
        String getDocument() {
            return matchingDocument ? "/same/document0" : "/different/document";
        }
    }
}
