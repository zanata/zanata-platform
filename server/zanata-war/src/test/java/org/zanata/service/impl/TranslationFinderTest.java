package org.zanata.service.impl;

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
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.jpa.FullText;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.SearchIndexManager;
import org.zanata.service.TranslationFinder;
import org.zanata.test.CdiUnitRunner;
import org.zanata.test.CdiUnitRunnerWithParameters;
import org.zanata.test.DBUnitDataSetRunner;
import org.zanata.test.ParamTestCdiExtension;
import org.zanata.test.rule.DataSetOperation;
import org.zanata.test.rule.JpaRule;
import org.zanata.util.UrlUtil;
import org.zanata.util.Zanata;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.zanata.service.impl.ExecutionHelper.cartesianProduct;
import static org.zanata.test.rule.FunctionalTestRule.reentrant;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(Enclosed.class)
public class TranslationFinderTest {

    static boolean searchIndexesCreated = false;

    static void recreateSearchIndexes(SearchIndexManager searchIndexManager)
            throws Exception {
        // NB: Only create the indexes once per test run. This assumes the
        // test doesn't change the data at all. If it were to do that, the
        // search indexes would need to be recreated
        if (!searchIndexesCreated) {
            searchIndexManager.reindex(true, true, false);
            searchIndexesCreated = true;
        }
    }

    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(CdiUnitRunnerWithParameters.Factory.class)
    @AdditionalClasses({ IndexingServiceImpl.class,
            ParamTestCdiExtension.class })
    public static class TranslationFinderParameterizedTest {

        private HLocale sourceLocale;
        @ClassRule
        @Rule
        public static JpaRule jpaRule = reentrant(new JpaRule());
        @Inject
        ProjectIterationDAO projectIterationDAO;
        @Inject
        LocaleDAO localeDAO;
        @Inject
        SearchIndexManager searchIndexManager;
        @Inject
        TextFlowTargetDAO textFlowTargetDAO;
        @Inject
        TranslationMemoryServiceImpl translationMemoryService;
        @Produces
        @Mock
        private UrlUtil urlUtil;
        @Parameterized.Parameter(0)
        Execution execution;

        @Produces
        @FullText
        FullTextEntityManager getFullTextEntityManager() {
            return Search.getFullTextEntityManager(jpaRule.getEntityManager());
        }

        @Produces
        @Zanata
        EntityManagerFactory getEntityManagerFactory() {
            return jpaRule.getEntityManagerFactory();
        }

        @Produces
        Session getSession() {
            return jpaRule.getSession();
        }
        // First
        // currently 64 combinations per translation finder

        @Parameterized.Parameters(name = "{index}: execution: [{0}]")
        public static Execution[] copyTransCombinations() {
            return generateAllExecutions().toArray(new Execution[] {});
        }

        private static Set<Execution> generateAllExecutions() {
            Set<Execution> allExecutions = new HashSet<Execution>();
            List<Boolean> booleans = asList(true, false);
            // NB 2 ^ 6 = 64 combinations
            Iterable[] colls = { booleans, booleans, booleans, booleans,
                    booleans, booleans };
            try {
                // The use of reflection is a little clumsy, but it helps
                // to ensure that we get the number of constructor arguments
                // correct for the cartesian product generator.
                Class[] paramTypes = new Class[colls.length];
                Arrays.fill(paramTypes, Boolean.TYPE);
                Constructor ctor = Execution.class.getConstructor(paramTypes);
                Set<Object[]> paramsSet = cartesianProduct(colls);
                for (Object[] params : paramsSet) {
                    Execution exec = (Execution) ctor.newInstance(params);
                    allExecutions.add(exec);
                }
            } catch (NoSuchMethodException | InstantiationException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return allExecutions;
        }

        @Before
        @InRequestScope
        public void before() throws Exception {
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
            recreateSearchIndexes(searchIndexManager);
            sourceLocale = localeDAO.findByLocaleId(LocaleId.EN_US);
        }
        // @Ignore
        // @Test
        // @InRequestScope

        /**
         * Use this test to individually test scenarios.
         */
        public void individualTest() {
            testExecution(TranslationMemoryServiceImpl.class,
                    new Execution(false, true, true, true, true, true));
        }
        // currently 64 combinations
        // TODO reduce the combinations, or reduce the cost per execution

        @Test
        @InRequestScope
        public void testTextFlowTargetDAO() {
            testExecution(TextFlowTargetDAO.class, execution);
        }
        // currently 64 combinations
        // TODO reduce the combinations, or reduce the cost per execution

        @Test
        @InRequestScope
        public void testTranslationMemoryServiceImpl() {
            testExecution(TranslationMemoryServiceImpl.class, execution);
        }

        private void testExecution(Class<? extends TranslationFinder> impl,
                Execution execution) {
            TranslationFinder service;
            if (TranslationMemoryServiceImpl.class.equals(impl)) {
                service = translationMemoryService;
            } else if (TextFlowTargetDAO.class.equals(impl)) {
                service = textFlowTargetDAO;
            } else {
                throw new RuntimeException(
                        "Unkown TranslationFinder type: " + impl.getName());
            }
            // Get the project iteration
            HProjectIteration queryProjIter = projectIterationDAO
                    .getBySlug(execution.getProject(), execution.getVersion());
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
            queryTextFlow.setContents(execution.getContent()); // Source content
                                                               // matches
            queryTextFlow.setPlural(false);
            queryTextFlow.setObsolete(false);
            queryTextFlow.setDocument(queryDoc);
            queryTextFlow.setResId(execution.getContext());
            // For all the executions whose queries are expected to find a
            // match,
            // the target which is expected to match is HTextFlowTarget 100
            // (which belongs to HTextFlow 100, HDocument 100,
            // HProjectIteration 100, HProject 100)
            Optional<HTextFlowTarget> matchingTarget = service
                    .searchBestMatchTransMemory(queryTextFlow, LocaleId.DE,
                            LocaleId.EN_US, execution.isCheckContext(),
                            execution.isCheckDocument(),
                            execution.isCheckProject());
            assertThat(matchingTarget.isPresent()).as("match present")
                    .isEqualTo(execution.expectMatch());
            if (matchingTarget.isPresent()) {
                HTextFlowTarget target = matchingTarget.get();
                HTextFlow tf = target.getTextFlow();
                assertThat(target.getLocaleId()).isEqualTo(LocaleId.DE);
                assertThat(tf.getContents())
                        .containsExactly(execution.getContent());
                if (execution.isCheckContext()) {
                    assertThat(tf.getResId()).isEqualTo(execution.getContext());
                }
                if (execution.isCheckDocument()) {
                    assertThat(tf.getDocument().getDocId())
                            .isEqualTo(execution.getDocument());
                }
                if (execution.isCheckProject()) {
                    assertThat(tf.getDocument().getProjectIteration()
                            .getProject().getSlug())
                                    .isEqualTo(execution.getProject());
                }
            }
        }
    }

    @RunWith(CdiUnitRunner.class)
    @AdditionalClasses({ IndexingServiceImpl.class })
    public static class TranslationFinderNonParameterizedTest {

        @ClassRule
        @Rule
        public static JpaRule jpaRule = reentrant(new JpaRule());
        @Inject
        TextFlowTargetDAO textFlowTargetDAO;
        @Inject
        TranslationMemoryServiceImpl translationMemoryService;
        @Inject
        ProjectIterationDAO projectIterationDAO;
        @Inject
        SearchIndexManager searchIndexManager;

        @Produces
        @FullText
        FullTextEntityManager getFullTextEntityManager() {
            return Search.getFullTextEntityManager(jpaRule.getEntityManager());
        }

        @Produces
        @Mock
        private UrlUtil urlUtil;

        @Produces
        @Zanata
        EntityManagerFactory getEntityManagerFactory() {
            return jpaRule.getEntityManagerFactory();
        }

        @Produces
        Session getSession() {
            return jpaRule.getSession();
        }

        @Before
        @InRequestScope
        public void before() throws Exception {
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
            recreateSearchIndexes(searchIndexManager);
        }

        @Test
        @InRequestScope
        public void testTextFlowTargetDAOMostRecent() {
            testMostRecentMatch(textFlowTargetDAO);
        }

        @Test
        @InRequestScope
        public void testTranslationMemoryServiceImplMostRecent()
                throws Exception {
            testMostRecentMatch(translationMemoryService);
        }

        /**
         * Makes sure that given two equal results, it will reuse the most
         * recent translation.
         */
        private void testMostRecentMatch(TranslationFinder service) {
            HProjectIteration version = projectIterationDAO
                    .getBySlug("same-project", "same-version");
            assert version != null;
            HDocument hDoc = version.getDocuments().get("/same/document0");
            HTextFlow textFlow = hDoc.getTextFlows().get(0);
            Optional<HTextFlowTarget> match =
                    service.searchBestMatchTransMemory(textFlow, LocaleId.DE,
                            hDoc.getSourceLocaleId(), true, true, true);
            assertTrue(match.isPresent());
            checkTargetContents(match.get(), "most recent content");
        }

        private void checkTargetContents(HTextFlowTarget target,
                final String searchString) {
            assertThat(target.getContents()).has(new Condition<>(contents -> {
                for (String content : contents) {
                    if (content.contains(searchString)) {
                        return true;
                    }
                }
                return false;
            }, "contains \"" + searchString + "\""));
        }
    }

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
            return (matchingContext || !checkContext)
                    && (matchingProject || !checkProject)
                    && (matchingDocument || !checkDocument);
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

        @java.beans.ConstructorProperties({ "checkContext", "checkProject",
                "checkDocument", "matchingContext", "matchingProject",
                "matchingDocument" })
        public Execution(final boolean checkContext, final boolean checkProject,
                final boolean checkDocument, final boolean matchingContext,
                final boolean matchingProject, final boolean matchingDocument) {
            this.checkContext = checkContext;
            this.checkProject = checkProject;
            this.checkDocument = checkDocument;
            this.matchingContext = matchingContext;
            this.matchingProject = matchingProject;
            this.matchingDocument = matchingDocument;
        }

        public boolean isCheckContext() {
            return this.checkContext;
        }

        public boolean isCheckProject() {
            return this.checkProject;
        }

        public boolean isCheckDocument() {
            return this.checkDocument;
        }

        public boolean isMatchingContext() {
            return this.matchingContext;
        }

        public boolean isMatchingProject() {
            return this.matchingProject;
        }

        public boolean isMatchingDocument() {
            return this.matchingDocument;
        }

        public void setMatch(final String match) {
            this.match = match;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof TranslationFinderTest.Execution))
                return false;
            final Execution other = (Execution) o;
            if (!other.canEqual((Object) this))
                return false;
            final Object this$match = this.getMatch();
            final Object other$match = other.getMatch();
            if (this$match == null ? other$match != null
                    : !this$match.equals(other$match))
                return false;
            if (this.isCheckContext() != other.isCheckContext())
                return false;
            if (this.isCheckProject() != other.isCheckProject())
                return false;
            if (this.isCheckDocument() != other.isCheckDocument())
                return false;
            if (this.isMatchingContext() != other.isMatchingContext())
                return false;
            if (this.isMatchingProject() != other.isMatchingProject())
                return false;
            if (this.isMatchingDocument() != other.isMatchingDocument())
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof TranslationFinderTest.Execution;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $match = this.getMatch();
            result = result * PRIME + ($match == null ? 43 : $match.hashCode());
            result = result * PRIME + (this.isCheckContext() ? 79 : 97);
            result = result * PRIME + (this.isCheckProject() ? 79 : 97);
            result = result * PRIME + (this.isCheckDocument() ? 79 : 97);
            result = result * PRIME + (this.isMatchingContext() ? 79 : 97);
            result = result * PRIME + (this.isMatchingProject() ? 79 : 97);
            result = result * PRIME + (this.isMatchingDocument() ? 79 : 97);
            return result;
        }

        @Override
        public String toString() {
            return "TranslationFinderTest.Execution(match=" + this.getMatch()
                    + ", checkContext=" + this.isCheckContext()
                    + ", checkProject=" + this.isCheckProject()
                    + ", checkDocument=" + this.isCheckDocument()
                    + ", matchingContext=" + this.isMatchingContext()
                    + ", matchingProject=" + this.isMatchingProject()
                    + ", matchingDocument=" + this.isMatchingDocument() + ")";
        }
    }
}
