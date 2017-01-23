/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import com.google.common.cache.CacheLoader;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.zanata.SlowTest;
import org.zanata.async.handle.CopyTransTaskHandle;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.cdi.TestTransaction;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.VersionLocaleKey;
import org.zanata.test.CdiUnitRunnerWithParameters;
import org.zanata.test.DBUnitDataSetRunner;
import org.zanata.test.ParamTestCdiExtension;
import org.zanata.test.rule.DataSetOperation;
import org.zanata.test.rule.JpaRule;
import org.zanata.transaction.TransactionUtilImpl;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.IServiceLocator;
import org.zanata.util.UrlUtil;
import org.zanata.util.Zanata;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationId;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.common.ContentState.Approved;
import static org.zanata.common.ContentState.NeedReview;
import static org.zanata.common.ContentState.New;
import static org.zanata.common.ContentState.Translated;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;
import static org.zanata.service.impl.ExecutionHelper.cartesianProduct;
import static org.zanata.test.rule.FunctionalTestRule.reentrant;

/**
 * This is a parameterized version of the CopyTransServiceImplTest class. It is
 * split to allow for non-parameterized tests.
 *
 * @See {@link CopyTransServiceImplTest}
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(Parameterized.class)
@SupportDeltaspikeCore
@Parameterized.UseParametersRunnerFactory(CdiUnitRunnerWithParameters.Factory.class)
@AdditionalClasses({ ParamTestCdiExtension.class, LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class, VersionStateCacheImpl.class,
        TranslationStateCacheImpl.class, ValidationServiceImpl.class,
        TransactionUtilImpl.class, UrlUtil.class })
public class CopyTransServiceImplParameterizedTest {

    @ClassRule
    @Rule
    public static JpaRule jpaRule = reentrant(new JpaRule());
    @Inject
    ProjectIterationDAO iterationDAO;
    @Inject
    LocaleDAO localeDAO;
    @Inject
    ProjectDAO projectDAO;
    @Inject
    DocumentDAO documentDAO;
    @Inject
    CopyTransServiceImpl copyTransService;
    @Produces
    @Mock
    IServiceLocator serviceLocator;
    @Produces
    @Mock
    @FullText
    FullTextEntityManager fullTextEntityManager;
    @Produces
    @Mock
    private UrlUtil urlUtil;
    @Produces
    @Mock
    private CacheLoader<DocumentLocaleKey, WordStatistic> documentStatisticLoader;
    @Produces
    @Mock
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;
    @Produces
    @Mock
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;
    @Produces
    @Mock
    private CacheLoader<VersionLocaleKey, WordStatistic> versionStatisticLoader;
    @Parameterized.Parameter(0)
    CopyTransExecution copyTransExecution;

    @Produces
    protected EntityManager getEm() {
        return jpaRule.getEntityManager();
    }

    @Produces
    protected EntityManagerFactory getEmf() {
        return jpaRule.getEntityManagerFactory();
    }

    @Produces
    protected Session getSession() {
        return jpaRule.getSession();
    }

    @Produces
    @Zanata
    protected CacheContainer getCacheContainer() {
        return new InfinispanTestCacheContainer();
    }

    @Produces
    @Authenticated
    HAccount getAuthenticatedAccount(AccountDAO accountDAO) {
        return accountDAO.getByUsername("demo");
    }

    @Before
    public void prepareDBUnitOperations() {
        DBUnitDataSetRunner runner =
                new DBUnitDataSetRunner(jpaRule.getEntityManager());
        runner.runDataSetOperations(
                new DataSetOperation(
                        "org/zanata/test/model/ClearAllTables.dbunit.xml",
                        DatabaseOperation.CLEAN_INSERT),
                new DataSetOperation(
                        "org/zanata/test/model/LocalesData.dbunit.xml",
                        DatabaseOperation.CLEAN_INSERT),
                new DataSetOperation(
                        "org/zanata/test/model/AccountData.dbunit.xml",
                        DatabaseOperation.CLEAN_INSERT),
                new DataSetOperation(
                        "org/zanata/test/model/CopyTransTestData.dbunit.xml",
                        DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void beforeMethod() throws Exception {
        when(serviceLocator.getJndiComponent("java:jboss/UserTransaction",
                UserTransaction.class))
                        .thenReturn(new TestTransaction(getEm()));
    }

    @Parameterized.Parameters(name = "{index}: Copy Trans Execution: {0}")
    public static Iterable<Object[]> copyTransExecution() {
        Set<CopyTransExecution> expandedExecutions = generateExecutions();
        return expandedExecutions.stream().map(cte -> new Object[] { cte })
                .collect(Collectors.toList());
    }
    // @Ignore
    // @Test
    // public void individualTest() throws Exception {
    // this.testCopyTrans(new CopyTransExecution(REJECT, IGNORE,
    // DOWNGRADE_TO_FUZZY, false, true, false, false, Approved)
    // .expectTransState(NeedReview));
    // }
    // @DataProvider
    // public static Object[][] copyTransParams() {
    // Set<CopyTransExecution> expandedExecutions = generateExecutions();
    //
    // Object[][] val = new Object[expandedExecutions.size()][1];
    // int i = 0;
    // for (CopyTransExecution exe : expandedExecutions) {
    // val[i++][0] = exe;
    // }
    //
    // return val;
    // }
    // @UseDataProvider("copyTransParams")
    // (about 2 seconds)

    /**
     * Use this test to individually test copy trans scenarios.
     */
    @Test
    @InRequestScope
    @SlowTest
    public void testCopyTrans() throws Exception {
        // Get the project iteration
        HProjectIteration projectIteration;
        if (copyTransExecution.projectMatches) {
            projectIteration =
                    iterationDAO.getBySlug("same-project", "different-version");
        } else {
            projectIteration = iterationDAO.getBySlug("different-project",
                    "different-version");
        }
        assert projectIteration != null;
        // Set require translation review
        projectIteration.setRequireTranslationReview(
                copyTransExecution.requireTranslationReview);
        // Change all targets to have the copyTransExecution's match state
        for (HDocument doc : projectIteration.getDocuments().values()) {
            for (HTextFlow tf : doc.getAllTextFlows().values()) {
                for (HTextFlowTarget tft : tf.getTargets().values()) {
                    tft.setState(copyTransExecution.matchState);
                }
            }
        }
        // Create the document
        HDocument doc = new HDocument();
        doc.setContentType(ContentType.TextPlain);
        doc.setLocale(localeDAO.findByLocaleId(LocaleId.EN_US));
        doc.setProjectIteration(projectIteration);
        if (copyTransExecution.documentMatches) {
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
        if (copyTransExecution.contextMatches) {
            textFlow.setResId("same-context");
        } else {
            textFlow.setResId("different-context");
        }
        doc.getTextFlows().add(textFlow);
        projectIteration = iterationDAO.makePersistent(projectIteration);
        getEm().flush(); // So the rest of the test sees the results
        HCopyTransOptions options = new HCopyTransOptions(
                copyTransExecution.getContextMismatchAction(),
                copyTransExecution.getDocumentMismatchAction(),
                copyTransExecution.getProjectMismatchAction());
        copyTransService.copyTransForIteration(projectIteration, options,
                new CopyTransTaskHandle());
        getEm().flush();
        // Validate copyTransExecution
        HTextFlow targetTextFlow = (HTextFlow) getEm()
                .createQuery(
                        "from HTextFlow tf where tf.document.projectIteration = :projectIteration and tf.document.docId = :docId and tf.resId = :resId")
                .setParameter("projectIteration", projectIteration)
                .setParameter("docId", doc.getDocId())
                .setParameter("resId", textFlow.getResId()).getSingleResult();
        // Id: 3L for Locale de
        HTextFlowTarget target = targetTextFlow.getTargets().get(3L);
        if (target != null) {
            assertThat(target.getSourceType())
                    .isEqualTo(TranslationSourceType.COPY_TRANS);
        }
        if (copyTransExecution.isExpectUntranslated()) {
            if (target != null && target.getState() != ContentState.New) {
                throw new AssertionError(
                        "Expected untranslated text flow but got state "
                                + target.getState());
            }
        } else if (copyTransExecution.getExpectedTranslationState() != New) {
            if (target == null) {
                throw new AssertionError("Expected state "
                        + copyTransExecution.getExpectedTranslationState()
                        + ", but got untranslated.");
            } else if (copyTransExecution
                    .getExpectedTranslationState() != target.getState()) {
                throw new AssertionError("Expected state "
                        + copyTransExecution.getExpectedTranslationState()
                        + ", but got " + target.getState());
            }
        }
        // Contents
        if (copyTransExecution.getExpectedContents() != null) {
            if (target == null) {
                throw new AssertionError("Expected contents "
                        + Arrays.toString(
                                copyTransExecution.getExpectedContents())
                        + ", but got untranslated.");
            } else if (!Arrays.equals(copyTransExecution.getExpectedContents(),
                    target.getContents().toArray())) {
                throw new AssertionError("Expected contents "
                        + Arrays.toString(
                                copyTransExecution.getExpectedContents())
                        + ", but got "
                        + Arrays.toString(target.getContents().toArray()));
            }
        }
    }

    private static ContentState
            getExpectedContentState(CopyTransExecution execution) {
        ContentState expectedContentState =
                execution.getRequireTranslationReview() ? Approved : Translated;
        expectedContentState = getExpectedContentState(
                execution.getContextMatches(),
                execution.getContextMismatchAction(), expectedContentState);
        expectedContentState = getExpectedContentState(
                execution.getProjectMatches(),
                execution.getProjectMismatchAction(), expectedContentState);
        expectedContentState = getExpectedContentState(
                execution.getDocumentMatches(),
                execution.getDocumentMismatchAction(), expectedContentState);
        return expectedContentState;
    }

    private static ContentState getExpectedContentState(boolean match,
            HCopyTransOptions.ConditionRuleAction action,
            ContentState currentState) {
        if (currentState == New) {
            return currentState;
        } else if (CopyTransWorkFactory.shouldReject(match, action)) {
            return New;
        } else if (CopyTransWorkFactory.shouldDowngradeToFuzzy(match, action)) {
            return NeedReview;
        } else {
            return currentState;
        }
    }

    private static Set<CopyTransExecution> generateExecutions() {
        Set<CopyTransExecution> allExecutions =
                new HashSet<CopyTransExecution>();
        // NB combinations which affect the query parameters
        // (context match/mismatch, etc) are tested in TranslationFinderTest
        Set<Object[]> paramsSet = cartesianProduct(Arrays.asList(REJECT),
                Arrays.asList(REJECT), Arrays.asList(REJECT),
                Arrays.asList(true), Arrays.asList(true), Arrays.asList(true),
                Arrays.asList(true, false),
                Arrays.asList(Translated, Approved));
        for (Object[] params : paramsSet) {
            CopyTransExecution exec = new CopyTransExecution(
                    (HCopyTransOptions.ConditionRuleAction) params[0],
                    (HCopyTransOptions.ConditionRuleAction) params[1],
                    (HCopyTransOptions.ConditionRuleAction) params[2],
                    (Boolean) params[3], (Boolean) params[4],
                    (Boolean) params[5], (Boolean) params[6],
                    (ContentState) params[7]);
            ContentState expectedContentState = getExpectedContentState(exec);
            if (expectedContentState == New) {
                exec.expectUntranslated();
            } else {
                exec.expectTransState(expectedContentState)
                        .withContents("target-content-de");
            }
            allExecutions.add(exec);
        }
        return allExecutions;
    }

    private static class CopyTransExecution implements Cloneable {
        private HCopyTransOptions.ConditionRuleAction contextMismatchAction;
        private HCopyTransOptions.ConditionRuleAction projectMismatchAction;
        private HCopyTransOptions.ConditionRuleAction documentMismatchAction;
        private Boolean contextMatches;
        private Boolean projectMatches;
        private Boolean documentMatches;
        private Boolean requireTranslationReview;
        private ContentState expectedTranslationState;
        private boolean expectUntranslated;
        private String[] expectedContents;
        public ContentState matchState;

        private CopyTransExecution(
                HCopyTransOptions.ConditionRuleAction contextMismatchAction,
                HCopyTransOptions.ConditionRuleAction projectMismatchAction,
                HCopyTransOptions.ConditionRuleAction documentMismatchAction,
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

        public HCopyTransOptions.ConditionRuleAction
                getContextMismatchAction() {
            return this.contextMismatchAction;
        }

        public HCopyTransOptions.ConditionRuleAction
                getProjectMismatchAction() {
            return this.projectMismatchAction;
        }

        public HCopyTransOptions.ConditionRuleAction
                getDocumentMismatchAction() {
            return this.documentMismatchAction;
        }

        public Boolean getContextMatches() {
            return this.contextMatches;
        }

        public Boolean getProjectMatches() {
            return this.projectMatches;
        }

        public Boolean getDocumentMatches() {
            return this.documentMatches;
        }

        public Boolean getRequireTranslationReview() {
            return this.requireTranslationReview;
        }

        public ContentState getExpectedTranslationState() {
            return this.expectedTranslationState;
        }

        public boolean isExpectUntranslated() {
            return this.expectUntranslated;
        }

        public String[] getExpectedContents() {
            return this.expectedContents;
        }

        public ContentState getMatchState() {
            return this.matchState;
        }

        public void setContextMismatchAction(
                final HCopyTransOptions.ConditionRuleAction contextMismatchAction) {
            this.contextMismatchAction = contextMismatchAction;
        }

        public void setProjectMismatchAction(
                final HCopyTransOptions.ConditionRuleAction projectMismatchAction) {
            this.projectMismatchAction = projectMismatchAction;
        }

        public void setDocumentMismatchAction(
                final HCopyTransOptions.ConditionRuleAction documentMismatchAction) {
            this.documentMismatchAction = documentMismatchAction;
        }

        public void setContextMatches(final Boolean contextMatches) {
            this.contextMatches = contextMatches;
        }

        public void setProjectMatches(final Boolean projectMatches) {
            this.projectMatches = projectMatches;
        }

        public void setDocumentMatches(final Boolean documentMatches) {
            this.documentMatches = documentMatches;
        }

        public void setRequireTranslationReview(
                final Boolean requireTranslationReview) {
            this.requireTranslationReview = requireTranslationReview;
        }

        public void setExpectedTranslationState(
                final ContentState expectedTranslationState) {
            this.expectedTranslationState = expectedTranslationState;
        }

        public void setExpectUntranslated(final boolean expectUntranslated) {
            this.expectUntranslated = expectUntranslated;
        }

        public void setExpectedContents(final String[] expectedContents) {
            this.expectedContents = expectedContents;
        }

        public void setMatchState(final ContentState matchState) {
            this.matchState = matchState;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof CopyTransServiceImplParameterizedTest.CopyTransExecution))
                return false;
            final CopyTransExecution other = (CopyTransExecution) o;
            if (!other.canEqual((Object) this))
                return false;
            final Object this$contextMismatchAction =
                    this.getContextMismatchAction();
            final Object other$contextMismatchAction =
                    other.getContextMismatchAction();
            if (this$contextMismatchAction == null
                    ? other$contextMismatchAction != null
                    : !this$contextMismatchAction
                            .equals(other$contextMismatchAction))
                return false;
            final Object this$projectMismatchAction =
                    this.getProjectMismatchAction();
            final Object other$projectMismatchAction =
                    other.getProjectMismatchAction();
            if (this$projectMismatchAction == null
                    ? other$projectMismatchAction != null
                    : !this$projectMismatchAction
                            .equals(other$projectMismatchAction))
                return false;
            final Object this$documentMismatchAction =
                    this.getDocumentMismatchAction();
            final Object other$documentMismatchAction =
                    other.getDocumentMismatchAction();
            if (this$documentMismatchAction == null
                    ? other$documentMismatchAction != null
                    : !this$documentMismatchAction
                            .equals(other$documentMismatchAction))
                return false;
            final Object this$contextMatches = this.getContextMatches();
            final Object other$contextMatches = other.getContextMatches();
            if (this$contextMatches == null ? other$contextMatches != null
                    : !this$contextMatches.equals(other$contextMatches))
                return false;
            final Object this$projectMatches = this.getProjectMatches();
            final Object other$projectMatches = other.getProjectMatches();
            if (this$projectMatches == null ? other$projectMatches != null
                    : !this$projectMatches.equals(other$projectMatches))
                return false;
            final Object this$documentMatches = this.getDocumentMatches();
            final Object other$documentMatches = other.getDocumentMatches();
            if (this$documentMatches == null ? other$documentMatches != null
                    : !this$documentMatches.equals(other$documentMatches))
                return false;
            final Object this$requireTranslationReview =
                    this.getRequireTranslationReview();
            final Object other$requireTranslationReview =
                    other.getRequireTranslationReview();
            if (this$requireTranslationReview == null
                    ? other$requireTranslationReview != null
                    : !this$requireTranslationReview
                            .equals(other$requireTranslationReview))
                return false;
            final Object this$expectedTranslationState =
                    this.getExpectedTranslationState();
            final Object other$expectedTranslationState =
                    other.getExpectedTranslationState();
            if (this$expectedTranslationState == null
                    ? other$expectedTranslationState != null
                    : !this$expectedTranslationState
                            .equals(other$expectedTranslationState))
                return false;
            if (this.isExpectUntranslated() != other.isExpectUntranslated())
                return false;
            if (!java.util.Arrays.deepEquals(this.getExpectedContents(),
                    other.getExpectedContents()))
                return false;
            final Object this$matchState = this.getMatchState();
            final Object other$matchState = other.getMatchState();
            if (this$matchState == null ? other$matchState != null
                    : !this$matchState.equals(other$matchState))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof CopyTransServiceImplParameterizedTest.CopyTransExecution;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $contextMismatchAction =
                    this.getContextMismatchAction();
            result = result * PRIME + ($contextMismatchAction == null ? 43
                    : $contextMismatchAction.hashCode());
            final Object $projectMismatchAction =
                    this.getProjectMismatchAction();
            result = result * PRIME + ($projectMismatchAction == null ? 43
                    : $projectMismatchAction.hashCode());
            final Object $documentMismatchAction =
                    this.getDocumentMismatchAction();
            result = result * PRIME + ($documentMismatchAction == null ? 43
                    : $documentMismatchAction.hashCode());
            final Object $contextMatches = this.getContextMatches();
            result = result * PRIME + ($contextMatches == null ? 43
                    : $contextMatches.hashCode());
            final Object $projectMatches = this.getProjectMatches();
            result = result * PRIME + ($projectMatches == null ? 43
                    : $projectMatches.hashCode());
            final Object $documentMatches = this.getDocumentMatches();
            result = result * PRIME + ($documentMatches == null ? 43
                    : $documentMatches.hashCode());
            final Object $requireTranslationReview =
                    this.getRequireTranslationReview();
            result = result * PRIME + ($requireTranslationReview == null ? 43
                    : $requireTranslationReview.hashCode());
            final Object $expectedTranslationState =
                    this.getExpectedTranslationState();
            result = result * PRIME + ($expectedTranslationState == null ? 43
                    : $expectedTranslationState.hashCode());
            result = result * PRIME + (this.isExpectUntranslated() ? 79 : 97);
            result = result * PRIME
                    + java.util.Arrays.deepHashCode(this.getExpectedContents());
            final Object $matchState = this.getMatchState();
            result = result * PRIME
                    + ($matchState == null ? 43 : $matchState.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "CopyTransServiceImplParameterizedTest.CopyTransExecution(contextMismatchAction="
                    + this.getContextMismatchAction()
                    + ", projectMismatchAction="
                    + this.getProjectMismatchAction()
                    + ", documentMismatchAction="
                    + this.getDocumentMismatchAction() + ", contextMatches="
                    + this.getContextMatches() + ", projectMatches="
                    + this.getProjectMatches() + ", documentMatches="
                    + this.getDocumentMatches() + ", requireTranslationReview="
                    + this.getRequireTranslationReview()
                    + ", expectedTranslationState="
                    + this.getExpectedTranslationState()
                    + ", expectUntranslated=" + this.isExpectUntranslated()
                    + ", expectedContents="
                    + java.util.Arrays.deepToString(this.getExpectedContents())
                    + ", matchState=" + this.getMatchState() + ")";
        }
    }
}
