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

import com.google.common.cache.CacheLoader;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.infinispan.manager.CacheContainer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cache.InfinispanTestCacheContainer;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.VersionLocaleKey;
import org.zanata.test.CdiUnitRunner;
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
import java.util.Map;
import static org.zanata.common.ContentState.Approved;
import static org.zanata.common.ContentState.New;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.IGNORE;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class, VersionStateCacheImpl.class,
        TranslationStateCacheImpl.class, ValidationServiceImpl.class })
public class CopyTransServiceImplTest extends ZanataDbunitJpaTest {

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
    private UrlUtil urlUtil;
    @Produces
    @Mock
    IServiceLocator serviceLocator;
    @Produces
    @Mock
    @FullText
    FullTextEntityManager fullTextEntityManager;
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

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    @Produces
    protected EntityManagerFactory getEmf() {
        return super.getEmf();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
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

    @Test
    @InRequestScope
    public void ignoreTranslationsFromObsoleteProjectAndVersion()
            throws Exception {
        // Make versions and projects obsolete
        HProjectIteration version =
                iterationDAO.getBySlug("same-project", "same-version");
        assert version != null;
        version.setStatus(EntityStatus.OBSOLETE);
        iterationDAO.makePersistent(version);
        HProject project = projectDAO.getBySlug("different-project");
        assert project != null;
        project.setStatus(EntityStatus.OBSOLETE);
        projectDAO.makePersistent(project);
        // Run the copy trans scenario (very liberal, but nothing should be
        // translated)
        CopyTransExecution execution = new CopyTransExecution(IGNORE, IGNORE,
                IGNORE, true, true, true, true, Approved).expectUntranslated();
        // testCopyTrans(execution);
    }

    @Test
    @InRequestScope
    public void reuseTranslationsFromObsoleteDocuments() throws Exception {
        // Make all documents obsolete
        HProjectIteration version =
                iterationDAO.getBySlug("same-project", "same-version");
        assert version != null;
        for (HDocument doc : version.getDocuments().values()) {
            doc.setObsolete(true);
            documentDAO.makePersistent(doc);
        }
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
                new CopyTransExecution(IGNORE, IGNORE, IGNORE, true, true, true,
                        true, Approved).expectTransState(Approved);
        // testCopyTrans(execution);
    }

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

        public ConditionRuleAction getContextMismatchAction() {
            return this.contextMismatchAction;
        }

        public ConditionRuleAction getProjectMismatchAction() {
            return this.projectMismatchAction;
        }

        public ConditionRuleAction getDocumentMismatchAction() {
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
                final ConditionRuleAction contextMismatchAction) {
            this.contextMismatchAction = contextMismatchAction;
        }

        public void setProjectMismatchAction(
                final ConditionRuleAction projectMismatchAction) {
            this.projectMismatchAction = projectMismatchAction;
        }

        public void setDocumentMismatchAction(
                final ConditionRuleAction documentMismatchAction) {
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
            if (!(o instanceof CopyTransServiceImplTest.CopyTransExecution))
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
            return other instanceof CopyTransServiceImplTest.CopyTransExecution;
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
            return "CopyTransServiceImplTest.CopyTransExecution(contextMismatchAction="
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
