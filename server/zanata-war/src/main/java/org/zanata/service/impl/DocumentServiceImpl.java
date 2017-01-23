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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.model.type.WebhookType;
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
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.CopyTransService;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.service.LockManagerService;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;
import com.google.common.annotations.VisibleForTesting;
import javax.enterprise.event.Event;
import org.zanata.util.UrlUtil;
import org.zanata.webhook.events.SourceDocumentChangedEvent;
import javax.enterprise.event.Observes;

/**
 * Default implementation of the {@link DocumentService} business service
 * interface.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("documentServiceImpl")
@RequestScoped
@Transactional
public class DocumentServiceImpl implements DocumentService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Inject
    private ZanataIdentity identity;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private CopyTransService copyTransServiceImpl;
    @Inject
    private LockManagerService lockManagerServiceImpl;
    @Inject
    private VersionStateCache versionStateCacheImpl;
    @Inject
    private TranslationStateCache translationStateCacheImpl;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Inject
    private UrlUtil urlUtil;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private WebhookServiceImpl webhookServiceImpl;
    @Inject
    private Messages msgs;
    @Inject
    private Event<DocumentUploadedEvent> documentUploadedEvent;

    @Override
    @Transactional
    public HDocument saveDocument(String projectSlug, String iterationSlug,
            Resource sourceDoc, Set<String> extensions, boolean copyTrans,
            boolean lock) {
        Lock docLock = null;
        if (lock) {
            // Lock this document for push
            docLock = new Lock(projectSlug, iterationSlug, sourceDoc.getName(),
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
            String iterationSlug, Resource sourceDoc, Set<String> extensions,
            boolean copyTrans, boolean lock,
            AsyncTaskHandle<HDocument> handle) {
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
        HLocale hLocale = this.localeServiceImpl
                .validateSourceLocale(sourceDoc.getLang());
        boolean changed = false;
        boolean isCreateOperation = false;
        int nextDocRev;
        if (document == null) {
            // must be a create operation
            nextDocRev = 1;
            changed = true;
            // TODO check that entity name matches id parameter
            document = new HDocument(sourceDoc.getName(),
                    sourceDoc.getContentType(), hLocale);
            document.setProjectIteration(hProjectIteration);
            hProjectIteration.getDocuments().put(docId, document);
            document = documentDAO.makePersistent(document);
            isCreateOperation = true;
        } else if (document.isObsolete()) {
            // must also be a create operation
            nextDocRev = document.getRevision() + 1;
            changed = true;
            document.setObsolete(false);
            // not sure if this is needed
            hProjectIteration.getDocuments().put(docId, document);
            isCreateOperation = true;
        } else {
            // must be an update operation
            nextDocRev = document.getRevision() + 1;
        }
        changed |= resourceUtils.transferFromResource(sourceDoc, document,
                extensions, hLocale, nextDocRev);
        documentDAO.flush();
        if (changed) {
            long actorId = authenticatedAccount.getPerson().getId();
            documentUploadedEvent.fire(new DocumentUploadedEvent(actorId,
                    document.getId(), true, hLocale.getLocaleId()));
            clearStatsCacheForUpdatedDocument(document);
            if (isCreateOperation) {
                webhookServiceImpl.processWebhookSourceDocumentChanged(
                        projectSlug, iterationSlug, document.getDocId(),
                        hProjectIteration.getProject().getWebHooks(),
                        SourceDocumentChangedEvent.ChangeType.ADD);
            }
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
        HProjectIteration version = document.getProjectIteration();
        HProject proj = version.getProject();
        webhookServiceImpl.processWebhookSourceDocumentChanged(proj.getSlug(),
                version.getSlug(), document.getDocId(), proj.getWebHooks(),
                SourceDocumentChangedEvent.ChangeType.REMOVE);
    }
    // TODO [CDI] simulate async event (e.g. this event was fired asyncly in
    // seam)

    public void documentStatisticUpdated(@Observes DocStatsEvent event) {
        HProjectIteration version =
                projectIterationDAO.findById(event.getProjectVersionId());
        HProject project = version.getProject();
        if (project.getWebHooks().isEmpty()) {
            return;
        }
        List<WebHook> docMilestoneWebHooks = project.getWebHooks().stream()
                .filter(webHook -> webHook.getTypes()
                        .contains(WebhookType.DocumentMilestoneEvent))
                .collect(Collectors.toList());
        if (docMilestoneWebHooks.isEmpty()) {
            return;
        }
        Long docId = event.getKey().getDocumentId();
        LocaleId localeId = event.getKey().getLocaleId();
        translationStateCacheImpl.clearDocumentStatistics(docId, localeId);
        WordStatistic stats = translationStateCacheImpl
                .getDocumentStatistics(docId, localeId);
        WordStatistic oldStats = StatisticsUtil.copyWordStatistic(stats);
        if (oldStats == null) {
            return;
        }
        for (Map.Entry<ContentState, Long> entry : event.getWordDeltasByState()
                .entrySet()) {
            int count = Math.toIntExact(entry.getValue());
            oldStats.decrement(entry.getKey(), count);
        }
        processWebHookDocumentMilestoneEvent(event.getKey(), stats, oldStats,
                version.getSlug(), project.getSlug(),
                ContentState.TRANSLATED_STATES,
                docMilestoneWebHooks, msgs.format("jsf.webhook.response.state",
                        DOC_EVENT_MILESTONE, ContentState.Translated),
                DOC_EVENT_MILESTONE);
        processWebHookDocumentMilestoneEvent(event.getKey(), stats, oldStats,
                version.getSlug(), project.getSlug(),
                Lists.newArrayList(ContentState.Approved),
                docMilestoneWebHooks, msgs.format("jsf.webhook.response.state",
                        DOC_EVENT_MILESTONE, ContentState.Approved),
                DOC_EVENT_MILESTONE);
    }

    private void processWebHookDocumentMilestoneEvent(DocumentLocaleKey key,
            WordStatistic stats, WordStatistic oldStats, String versionSlug,
            String projectSlug, Collection<ContentState> contentStates,
            List<WebHook> docMilestoneWebHooks, String message,
            int percentMilestone) {
        LocaleId localeId = key.getLocaleId();
        boolean shouldPublish = hasContentStateReachedMilestone(oldStats, stats,
                contentStates, percentMilestone);
        if (shouldPublish) {
            HDocument document = documentDAO.getById(key.getDocumentId());
            String editorUrl = urlUtil.fullEditorDocumentUrl(projectSlug,
                    versionSlug, localeId, LocaleId.EN_US, document.getDocId());
            webhookServiceImpl.processDocumentMilestone(projectSlug,
                    versionSlug, document.getDocId(), localeId, message,
                    editorUrl, docMilestoneWebHooks);
        }
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
        int oldStateCount = 0;
        int newStateCount = 0;
        double percent = 0;
        for (ContentState contentState : contentStates) {
            oldStateCount += oldStats.get(contentState);
            newStateCount += newStats.get(contentState);
            percent += newStats.getPercentage(contentState);
        }
        return oldStateCount != newStateCount
                && Double.compare(percent, percentMilestone) == 0;
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
        versionStateCacheImpl
                .clearVersionStatsCache(document.getProjectIteration().getId());
        translationStateCacheImpl.clearDocumentStatistics(document.getId());
    }

    @VisibleForTesting
    public void init(ProjectIterationDAO projectIterationDAO,
            DocumentDAO documentDAO,
            TranslationStateCache translationStateCacheImpl, UrlUtil urlUtil,
            ApplicationConfiguration applicationConfiguration, Messages msgs,
            WebhookServiceImpl webhookService) {
        this.projectIterationDAO = projectIterationDAO;
        this.documentDAO = documentDAO;
        this.translationStateCacheImpl = translationStateCacheImpl;
        this.urlUtil = urlUtil;
        this.applicationConfiguration = applicationConfiguration;
        this.msgs = msgs;
        this.webhookServiceImpl = webhookService;
    }
}
