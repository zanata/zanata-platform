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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        LocaleServiceImpl.class,
        TranslationMemoryServiceImpl.class,
        VersionStateCacheImpl.class,
        TranslationStateCacheImpl.class,
        ValidationServiceImpl.class
})
public class CopyTransServiceImplTest extends ZanataDbunitJpaTest {

    @Inject ProjectIterationDAO iterationDAO;
    @Inject LocaleDAO localeDAO;
    @Inject ProjectDAO projectDAO;
    @Inject DocumentDAO documentDAO;
    @Inject CopyTransServiceImpl copyTransService;

    @Produces @Mock IServiceLocator serviceLocator;
    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;

    @Produces @Mock
    private CacheLoader<DocumentLocaleKey, WordStatistic>
            documentStatisticLoader;

    @Produces @Mock
    private CacheLoader<DocumentLocaleKey, DocumentStatus> docStatusLoader;

    @Produces @Mock
    private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

    @Produces @Mock
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

    @Produces @Zanata
    protected CacheContainer getCacheContainer() {
        return new InfinispanTestCacheContainer();
    }

    @Produces @Authenticated
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
        CopyTransExecution execution =
                new CopyTransExecution(IGNORE, IGNORE, IGNORE, true, true,
                        true, true, Approved).expectUntranslated();
//        testCopyTrans(execution);
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
                new CopyTransExecution(IGNORE, IGNORE, IGNORE, true, true,
                        true, true, Approved).expectTransState(Approved);
//        testCopyTrans(execution);
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
