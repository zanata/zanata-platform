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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.ApplicationConfiguration;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.ContainsAsyncMethods;
import org.zanata.common.ContentState;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocumentMilestoneEvent;
import org.zanata.events.DocumentStatisticUpdatedEvent;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.i18n.Messages;
import org.zanata.lock.Lock;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.WebHook;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.service.LockManagerService;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.model.statistic.WordStatistic;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.zanata.util.Event;

import javax.enterprise.event.Observes;

/**
 * Default implementation of the {@link DocumentService} business service
 * interface.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("documentServiceImpl")
@Scope(ScopeType.STATELESS)
@ContainsAsyncMethods
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    @In(required = false)
    private ZanataIdentity identity;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private DocumentDAO documentDAO;

    @In
    private LocaleService localeServiceImpl;

    @In
    private CopyTransService copyTransServiceImpl;

    @In
    private LockManagerService lockManagerServiceImpl;

    @In
    private VersionStateCache versionStateCacheImpl;

    @In
    private TranslationStateCache translationStateCacheImpl;

    @In
    private ResourceUtils resourceUtils;

    @In
    private ApplicationConfiguration applicationConfiguration;

    @In(value = JpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION,
            required = false)
    private HAccount authenticatedAccount;

    @In
    private Messages msgs;

    @In("event")
    private Event<DocumentUploadedEvent> documentUploadedEvent;

    @Override
    @Transactional
    public HDocument saveDocument(String projectSlug, String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans,
            boolean lock) {
        Lock docLock = null;
        if (lock) {
            // Lock this document for push
            docLock =
                    new Lock(projectSlug, iterationSlug, sourceDoc.getName(),
                            "push");
            lockManagerServiceImpl.attain(docLock);
        }

        try {
            return this.saveDocument(projectSlug, iterationSlug, sourceDoc,
                    extensions, copyTrans);
        } finally {
            if (lock) {
                lockManagerServiceImpl.release(docLock);
            }
        }
    }

    @Override
    @Async
    @Transactional
    public Future<HDocument> saveDocumentAsync(String projectSlug,
            String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans,
            boolean lock, AsyncTaskHandle<HDocument> handle) {
        // TODO Use the pased in handle
        return AsyncTaskResult.taskResult(saveDocument(projectSlug,
                iterationSlug, sourceDoc, extensions, copyTrans, lock));
    }

    @Override
    @Transactional
    public HDocument saveDocument(String projectSlug, String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans) {
        // Only active iterations allow the addition of a document
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);

        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");

        String docId = sourceDoc.getName();

        HDocument document =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        HLocale hLocale =
                this.localeServiceImpl
                        .validateSourceLocale(sourceDoc.getLang());

        boolean changed = false;
        int nextDocRev;
        if (document == null) { // must be a create operation
            nextDocRev = 1;
            changed = true;
            // TODO check that entity name matches id parameter
            document =
                    new HDocument(sourceDoc.getName(),
                            sourceDoc.getContentType(), hLocale);
            document.setProjectIteration(hProjectIteration);
            hProjectIteration.getDocuments().put(docId, document);
            document = documentDAO.makePersistent(document);
        } else if (document.isObsolete()) { // must also be a create operation
            nextDocRev = document.getRevision() + 1;
            changed = true;
            document.setObsolete(false);
            // not sure if this is needed
            hProjectIteration.getDocuments().put(docId, document);
        } else { // must be an update operation
            nextDocRev = document.getRevision() + 1;
        }

        changed |=
                resourceUtils.transferFromResource(sourceDoc, document,
                        extensions, hLocale, nextDocRev);
        documentDAO.flush();

        long actorId = authenticatedAccount.getPerson().getId();
        if (changed) {
            documentUploadedEvent.fireAfterSuccess(new DocumentUploadedEvent(
                    actorId, document.getId(), true, hLocale.getLocaleId()));
            clearStatsCacheForUpdatedDocument(document);
        }

        if (copyTrans && nextDocRev == 1) {
            copyTranslations(document);
        }

        return document;
    }

    @Override
    @Transactional
    public void makeObsolete(HDocument document) {
        // Simply make it obsolete. This method is here in case this logic is
        // expanded.
        document.setObsolete(true);
        documentDAO.makePersistent(document);
        documentDAO.flush();
        clearStatsCacheForUpdatedDocument(document);
    }

    @Observer(DocumentStatisticUpdatedEvent.EVENT_NAME)
    public void documentStatisticUpdated(@Observes DocumentStatisticUpdatedEvent event) {
        processWebHookDocumentMilestoneEvent(event,
                ContentState.TRANSLATED_STATES,
                msgs.format("jsf.webhook.response.state", DOC_EVENT_MILESTONE,
                        ContentState.Translated), DOC_EVENT_MILESTONE);

        processWebHookDocumentMilestoneEvent(event,
                Lists.newArrayList(ContentState.Approved),
                msgs.format("jsf.webhook.response.state", DOC_EVENT_MILESTONE,
                        ContentState.Approved), DOC_EVENT_MILESTONE);
    }

    private void processWebHookDocumentMilestoneEvent(
            DocumentStatisticUpdatedEvent event,
            Collection<ContentState> contentStates, String message,
            int percentMilestone) {

        boolean shouldPublish =
                hasContentStateReachedMilestone(event.getOldStats(),
                        event.getNewStats(), contentStates, percentMilestone);

        if (shouldPublish) {
            HProjectIteration version =
                    projectIterationDAO.findById(event.getProjectIterationId());
            HProject project = version.getProject();

            if (!project.getWebHooks().isEmpty()) {
                HDocument document = documentDAO.getById(event.getDocumentId());
                DocumentMilestoneEvent milestoneEvent =
                        new DocumentMilestoneEvent(project.getSlug(),
                                version.getSlug(), document.getDocId(),
                                event.getLocaleId(), message);
                for (WebHook webHook : project.getWebHooks()) {
                    publishDocumentMilestoneEvent(webHook, milestoneEvent);
                }
            }
        }
    }

    public void publishDocumentMilestoneEvent(WebHook webHook,
            DocumentMilestoneEvent milestoneEvent) {
        WebHooksPublisher.publish(webHook.getUrl(), milestoneEvent);
        log.info("firing webhook: {}:{}:{}:{}",
                webHook.getUrl(), milestoneEvent.getProject(),
                milestoneEvent.getVersion(), milestoneEvent.getDocId());
    }

    /**
     * Check if contentStates in statistic has reached given
     * milestone(percentage) and not equals to contentStates in previous
     * statistic
     *
     * @param oldStats
     * @param newStats
     * @param contentStates
     * @param percentMilestone
     */
    private boolean hasContentStateReachedMilestone(WordStatistic oldStats,
            WordStatistic newStats, Collection<ContentState> contentStates,
            int percentMilestone) {
        int oldStateCount = 0, newStateCount = 0;
        double percent = 0;

        for (ContentState contentState : contentStates) {
            oldStateCount += oldStats.get(contentState);
            newStateCount += newStats.get(contentState);
            percent += newStats.getPercentage(contentState);
        }

        return oldStateCount != newStateCount &&
                Double.compare(percent, percentMilestone) == 0;
    }

    /**
     * Invoke the copy trans function for a document.
     *
     * @param document
     *            The document to copy translations into.
     */
    private void copyTranslations(HDocument document) {
        if (applicationConfiguration.isCopyTransEnabled()) {
            copyTransServiceImpl.copyTransForDocument(document, null);
        }
    }

    private void clearStatsCacheForUpdatedDocument(HDocument document) {
        versionStateCacheImpl.clearVersionStatsCache(document
                .getProjectIteration()
                .getId());
        translationStateCacheImpl.clearDocumentStatistics(document.getId());
    }

    @VisibleForTesting
    public void init(ProjectIterationDAO projectIterationDAO,
            DocumentDAO documentDAO, Messages msgs) {
        this.projectIterationDAO = projectIterationDAO;
        this.documentDAO = documentDAO;
        this.msgs = msgs;
    }
}
