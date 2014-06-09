/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.jpa.Search;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.SlowTest;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.seam.AutowireTransaction;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.CopyTransService;
import org.zanata.service.SearchIndexManager;
import org.zanata.service.VersionStateCache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import static org.zanata.common.ContentState.Approved;
import static org.zanata.common.ContentState.NeedReview;
import static org.zanata.common.ContentState.New;
import static org.zanata.common.ContentState.Translated;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.IGNORE;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;
import static org.zanata.service.impl.ExecutionHelper.cartesianProduct;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class CopyTransServiceImplTest extends ZanataDbunitJpaTest {
    private SeamAutowire seam = SeamAutowire.instance();

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/CopyTransTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeMethod
    protected void beforeMethod() throws Exception {
        seam.reset()
                .use("entityManager", Search.getFullTextEntityManager(getEm()))
                .use("entityManagerFactory", getEmf())
                .use("session", new FullTextSessionImpl(getSession()))
                .use(JpaIdentityStore.AUTHENTICATED_USER,
                        seam.autowire(AccountDAO.class).getByUsername("demo"))
                .useImpl(LocaleServiceImpl.class)
                .useImpl(TranslationMemoryServiceImpl.class)
                .useImpl(AsyncTaskManagerServiceImpl.class)
                .useImpl(VersionStateCacheImpl.class)
                .useImpl(ValidationServiceImpl.class).ignoreNonResolvable();

        seam.autowire(SearchIndexManager.class).reindex(true, true, false);
        AutowireTransaction.instance().rollback();
    }

    /**
     * Use this test to individually test copy trans scenarios.
     */
    @Test(enabled = false)
    public void individualTest() {

        this.testCopyTrans(new CopyTransExecution(REJECT, IGNORE,
                DOWNGRADE_TO_FUZZY, false, true, false, false, Approved)
                .expectTransState(NeedReview));
    }

    @DataProvider(name = "CopyTrans")
    protected Object[][] createCopyTransTestParams() {
        Set<CopyTransExecution> expandedExecutions = generateExecutions();

        Object[][] val = new Object[expandedExecutions.size()][1];
        int i = 0;
        for (CopyTransExecution exe : expandedExecutions) {
            val[i++][0] = exe;
        }

        return val;
    }

    @Test(dataProvider = "CopyTrans")
    @SlowTest
    public void testCopyTrans(CopyTransExecution execution) {
        // Prepare Execution
        ProjectIterationDAO iterationDAO =
                seam.autowire(ProjectIterationDAO.class);
        LocaleDAO localeDAO = seam.autowire(LocaleDAO.class);

        // Get the project iteration
        HProjectIteration projectIteration;
        if (execution.projectMatches) {
            projectIteration =
                    iterationDAO.getBySlug("same-project", "different-version");
        } else {
            projectIteration =
                    iterationDAO.getBySlug("different-project",
                            "different-version");
        }
        assert projectIteration != null;

        // Set require translation review
        projectIteration
                .setRequireTranslationReview(execution.requireTranslationReview);

        // Change all targets to have the execution's match state
        for (HDocument doc : projectIteration.getDocuments().values()) {
            for (HTextFlow tf : doc.getAllTextFlows().values()) {
                for (HTextFlowTarget tft : tf.getTargets().values()) {
                    tft.setState(execution.matchState);
                }
            }
        }

        // Create the document
        HDocument doc = new HDocument();
        doc.setContentType(ContentType.TextPlain);
        doc.setLocale(localeDAO.findByLocaleId(LocaleId.EN_US));
        doc.setProjectIteration(projectIteration);
        if (execution.documentMatches) {
            doc.setFullPath("/same/document");
        } else {
            doc.setFullPath("/different/document");
        }
        projectIteration.getDocuments().put(doc.getDocId(), doc);

        // Create the text Flow
        HTextFlow textFlow = new HTextFlow();
        textFlow.setContents("Source Content"); // Source content matches
        textFlow.setPlural(false);
        textFlow.setObsolete(false);
        textFlow.setDocument(doc);
        if (execution.contextMatches) {
            textFlow.setResId("same-context");
        } else {
            textFlow.setResId("different-context");
        }
        doc.getTextFlows().add(textFlow);

        projectIteration = iterationDAO.makePersistent(projectIteration);
        getEm().flush(); // So the rest of the test sees the results

        HCopyTransOptions options =
                new HCopyTransOptions(execution.getContextMismatchAction(),
                        execution.getDocumentMismatchAction(),
                        execution.getProjectMismatchAction());
        CopyTransService copyTransService =
                seam.autowire(CopyTransServiceImpl.class);
        copyTransService.copyTransForIteration(projectIteration, options);
        getEm().flush();

        // Validate execution
        HTextFlow targetTextFlow =
                (HTextFlow) getEm()
                        .createQuery(
                                "from HTextFlow tf where tf.document.projectIteration = :projectIteration "
                                        + "and tf.document.docId = :docId and tf.resId = :resId")
                        .setParameter("projectIteration", projectIteration)
                        .setParameter("docId", doc.getDocId())
                        .setParameter("resId", textFlow.getResId())
                        .getSingleResult();
        // Id: 3L for Locale de
        HTextFlowTarget target = targetTextFlow.getTargets().get(3L);

        if (execution.isExpectUntranslated()) {
            if (target != null && target.getState() != ContentState.New) {
                throw new AssertionError(
                        "Expected untranslated text flow but got state "
                                + target.getState());
            }
        } else if (execution.getExpectedTranslationState() != New) {
            if (target == null) {
                throw new AssertionError("Expected state "
                        + execution.getExpectedTranslationState()
                        + ", but got untranslated.");
            } else if (execution.getExpectedTranslationState() != target
                    .getState()) {
                throw new AssertionError("Expected state "
                        + execution.getExpectedTranslationState()
                        + ", but got " + target.getState());
            }
        }

        // Contents
        if (execution.getExpectedContents() != null) {
            if (target == null) {
                throw new AssertionError("Expected contents "
                        + Arrays.toString(execution.getExpectedContents())
                        + ", but got untranslated.");
            } else if (!Arrays.equals(execution.getExpectedContents(), target
                    .getContents().toArray())) {
                throw new AssertionError("Expected contents "
                        + Arrays.toString(execution.getExpectedContents())
                        + ", but got "
                        + Arrays.toString(target.getContents().toArray()));
            }
        }
    }

    @Test
    public void ignoreTranslationsFromObsoleteProjectAndVersion()
            throws Exception {
        ProjectIterationDAO projectIterationDAO =
                seam.autowire(ProjectIterationDAO.class);
        ProjectDAO projectDAO = seam.autowire(ProjectDAO.class);

        // Make versions and projects obsolete
        HProjectIteration version =
                projectIterationDAO.getBySlug("same-project", "same-version");
        assert version != null;
        version.setStatus(EntityStatus.OBSOLETE);
        projectIterationDAO.makePersistent(version);

        HProject project = projectDAO.getBySlug("different-project");
        assert project != null;
        project.setStatus(EntityStatus.OBSOLETE);
        projectDAO.makePersistent(project);

        // Run the copy trans scenario (very liberal, but nothing should be
        // translated)
        CopyTransExecution execution =
                new CopyTransExecution(IGNORE, IGNORE, IGNORE, true, true,
                        true, true, Approved).expectUntranslated();
        testCopyTrans(execution);
    }

    @Test
    public void reuseTranslationsFromObsoleteDocuments() throws Exception {
        ProjectIterationDAO projectIterationDAO =
                seam.autowire(ProjectIterationDAO.class);
        DocumentDAO documentDAO = seam.autowire(DocumentDAO.class);

        // Make all documents obsolete
        HProjectIteration version =
                projectIterationDAO.getBySlug("same-project", "same-version");
        assert version != null;
        for (HDocument doc : version.getDocuments().values()) {
            doc.setObsolete(true);
            documentDAO.makePersistent(doc);
        }

        ProjectDAO projectDAO = seam.autowire(ProjectDAO.class);
        HProject project = projectDAO.getBySlug("different-project");
        assert project != null;
        for (HProjectIteration it : project.getProjectIterations()) {
            for (HDocument doc : it.getDocuments().values()) {
                doc.setObsolete(true);
                documentDAO.makePersistent(doc);
            }
        }

        // Run the copy trans scenario
        CopyTransExecution execution =
                new CopyTransExecution(IGNORE, IGNORE, IGNORE, true, true,
                        true, true, Approved).expectTransState(Approved);
        testCopyTrans(execution);
    }

    private ContentState getExpectedContentState(CopyTransExecution execution) {
        ContentState expectedContentState =
                execution.getRequireTranslationReview() ? Approved : Translated;

        expectedContentState =
                getExpectedContentState(execution.getContextMatches(),
                        execution.getContextMismatchAction(),
                        expectedContentState);
        expectedContentState =
                getExpectedContentState(execution.getProjectMatches(),
                        execution.getProjectMismatchAction(),
                        expectedContentState);
        expectedContentState =
                getExpectedContentState(execution.getDocumentMatches(),
                        execution.getDocumentMismatchAction(),
                        expectedContentState);
        return expectedContentState;
    }

    private static ContentState getExpectedContentState(boolean match,
            HCopyTransOptions.ConditionRuleAction action,
            ContentState currentState) {
        if (currentState == New) {
            return currentState;
        } else if (CopyTransWork.shouldReject(match, action)) {
            return New;
        } else if (CopyTransWork.shouldDowngradeToFuzzy(match, action)) {
            return NeedReview;
        } else {
            return currentState;
        }
    }

    private Set<CopyTransExecution> generateExecutions() {
        Set<CopyTransExecution> allExecutions =
                new HashSet<CopyTransExecution>();
        // NB combinations which affect the query parameters
        // (context match/mismatch, etc) are tested in TranslationFinderTest
        Set<Object[]> paramsSet =
                cartesianProduct(Arrays.asList(REJECT), Arrays.asList(REJECT),
                        Arrays.asList(REJECT), Arrays.asList(true),
                        Arrays.asList(true), Arrays.asList(true),
                        Arrays.asList(true, false),
                        Arrays.asList(Translated, Approved));

        for (Object[] params : paramsSet) {
            CopyTransExecution exec =
                    new CopyTransExecution((ConditionRuleAction) params[0],
                            (ConditionRuleAction) params[1],
                            (ConditionRuleAction) params[2],
                            (Boolean) params[3], (Boolean) params[4],
                            (Boolean) params[5], (Boolean) params[6],
                            (ContentState) params[7]);

            ContentState expectedContentState =
                    this.getExpectedContentState(exec);
            if (expectedContentState == New) {
                exec.expectUntranslated();
            } else {
                exec.expectTransState(expectedContentState).withContents(
                        "target-content-de");
            }
            allExecutions.add(exec);
        }
        return allExecutions;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    private static class CopyTransExecution implements Cloneable {
        private ConditionRuleAction contextMismatchAction;
        private ConditionRuleAction projectMismatchAction;
        private ConditionRuleAction documentMismatchAction;
        private Boolean contextMatches;
        private Boolean projectMatches;
        private Boolean documentMatches;
        private Boolean requireTranslationReview;
        private ContentState expectedTranslationState;
        private boolean expectUntranslated;
        private String[] expectedContents;
        public ContentState matchState;

        private CopyTransExecution(ConditionRuleAction contextMismatchAction,
                ConditionRuleAction projectMismatchAction,
                ConditionRuleAction documentMismatchAction,
                Boolean contextMatches, Boolean projectMatches,
                Boolean documentMatches, Boolean requireTranslationReview,
                ContentState matchState) {
            this.contextMismatchAction = contextMismatchAction;
            this.projectMismatchAction = projectMismatchAction;
            this.documentMismatchAction = documentMismatchAction;
            this.contextMatches = contextMatches;
            this.projectMatches = projectMatches;
            this.documentMatches = documentMatches;
            this.requireTranslationReview = requireTranslationReview;
            this.matchState = matchState;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public CopyTransExecution expectTransState(ContentState state) {
            this.expectedTranslationState = state;
            this.expectUntranslated = state == New;
            return this;
        }

        public CopyTransExecution expectUntranslated() {
            this.expectedTranslationState = New;
            this.expectUntranslated = true;
            return this;
        }

        public CopyTransExecution withContents(String... contents) {
            this.expectedContents = contents;
            return this;
        }
    }
}
