/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.seam.core.Events;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.jboss.seam.util.Work;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.async.ContainsAsyncMethods;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.common.util.ContentStateUtil;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocumentUploadedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.exception.ZanataServiceException;
import org.zanata.i18n.Messages;
import org.zanata.lock.Lock;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.type.EntityType;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.LockManagerService;
import org.zanata.service.TranslationMergeService;
import org.zanata.service.TranslationService;
import org.zanata.service.ValidationService;
import javax.enterprise.event.Event;
import org.zanata.util.ShortString;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.ValidationAction;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Named("translationServiceImpl")
@javax.enterprise.context.Dependent
@Transactional
@ContainsAsyncMethods
@Slf4j
public class TranslationServiceImpl implements TranslationService {

    @Inject
    private EntityManager entityManager;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private DocumentDAO documentDAO;

    @Inject
    private TextFlowDAO textFlowDAO;

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;

    @Inject
    private ResourceUtils resourceUtils;

    @Inject
    private LocaleService localeServiceImpl;

    @Inject
    private LockManagerService lockManagerServiceImpl;

    @Inject
    private ValidationService validationServiceImpl;

    @Inject(value = ZanataJpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION,
            required = false)
    private HAccount authenticatedAccount;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private TranslationMergeServiceFactory translationMergeServiceFactory;

    @Inject
    private Messages msgs;

    @Inject
    private Event<DocumentUploadedEvent> documentUploadedEvent;

    @Inject
    private Event<TextFlowTargetStateEvent> textFlowTargetStateEvent;

    @Override
    public List<TranslationResult> translate(LocaleId localeId,
            List<TransUnitUpdateRequest> translationRequests) {
        return translate(localeId, translationRequests, true);
    }

    /**
     * This is used when reverting translation
     *
     * @param localeId
     * @param translationRequests
     * @return
     */
    private List<TranslationResult>
            translateWithoutValidating(LocaleId localeId,
                    List<TransUnitUpdateRequest> translationRequests) {
        return translate(localeId, translationRequests, false);
    }

    private List<TranslationResult> translate(LocaleId localeId,
            List<TransUnitUpdateRequest> translationRequests,
            boolean runValidation) {
        List<TranslationResult> results = new ArrayList<TranslationResult>();

        // avoid locale check if there is nothing to translate
        if (translationRequests.isEmpty()) {
            return results;
        }

        // single locale check - assumes update requests are all from the same
        // project-iteration
        HTextFlow sampleHTextFlow =
                entityManager.find(HTextFlow.class, translationRequests.get(0)
                        .getTransUnitId().getValue());
        HProjectIteration projectIteration =
                sampleHTextFlow.getDocument().getProjectIteration();
        HLocale hLocale = validateLocale(localeId, projectIteration);

        // single permission check - assumes update requests are all from same
        // project
        validateReviewPermissionIfApplicable(translationRequests,
                projectIteration, hLocale);

        for (TransUnitUpdateRequest request : translationRequests) {
            HTextFlow hTextFlow =
                    entityManager.find(HTextFlow.class, request
                            .getTransUnitId().getValue());

            TranslationResultImpl result = new TranslationResultImpl();

            if (runValidation) {
                String validationMessage =
                        validateTranslations(request.getNewContentState(),
                                projectIteration, request.getTransUnitId()
                                        .toString(), hTextFlow.getContents(),
                                request.getNewContents());

                if (!StringUtils.isEmpty(validationMessage)) {
                    log.warn(validationMessage);
                    result.isSuccess = false;
                    result.errorMessage = validationMessage;
                    results.add(result);
                    continue;
                }
            }

            HTextFlowTarget hTextFlowTarget =
                    textFlowTargetDAO.getOrCreateTarget(hTextFlow, hLocale);
            // if hTextFlowTarget is created, any further hibernate fetch will
            // trigger an implicit flush
            // (which will save this target even if it's not fully ready!!!)
            if (request.hasTargetComment()) {
                // FIXME this creates orphan comments, and replaces identical
                // comments with copies
                hTextFlowTarget.setComment(new HSimpleComment(request
                        .getTargetComment()));
            }

            result.baseVersion = hTextFlowTarget.getVersionNum();
            result.baseContentState = hTextFlowTarget.getState();

            if (request.getBaseTranslationVersion() == hTextFlowTarget
                    .getVersionNum()) {
                try {
                    int nPlurals = getNumPlurals(hLocale, hTextFlow);
                    result.targetChanged =
                            translate(hTextFlowTarget,
                                    request.getNewContents(),
                                    request.getNewContentState(),
                                    nPlurals,
                                    new TranslationDetails(request));
                    result.isSuccess = true;
                } catch (HibernateException e) {
                    result.isSuccess = false;
                    log.warn("HibernateException while translating");
                }
            } else {
                // concurrent edits not allowed
                String errorMessage =
                        "translation failed for textflow " + hTextFlow.getId()
                                + ": base versionNum + "
                                + request.getBaseTranslationVersion()
                                + " does not match current versionNum "
                                + hTextFlowTarget.getVersionNum();

                log.warn(errorMessage);
                result.errorMessage = errorMessage;
                result.isVersionNumConflict = true;
                result.isSuccess = false;
            }
            result.translatedTextFlowTarget = hTextFlowTarget;
            results.add(result);
        }

        return results;
    }

    private void validateReviewPermissionIfApplicable(
            List<TransUnitUpdateRequest> translationRequests,
            HProjectIteration projectIteration, HLocale hLocale) {
        Optional<TransUnitUpdateRequest> hasReviewRequest =
                Iterables.tryFind(translationRequests,
                        new Predicate<TransUnitUpdateRequest>() {
                            @Override
                            public boolean apply(TransUnitUpdateRequest input) {
                                return isReviewState(input.getNewContentState());
                            }
                        });
        if (hasReviewRequest.isPresent()) {
            identity.checkPermission("translation-review",
                    projectIteration.getProject(), hLocale);
        }
    }

    private static boolean isReviewState(ContentState contentState) {
        return contentState == ContentState.Approved
                || contentState == ContentState.Rejected;
    }

    /**
     * Generate a {@link HLocale} for the given localeId and check that
     * translations for this locale are permitted.
     *
     * @param localeId
     * @param projectIteration
     * @return the valid hLocale
     * @throws ZanataServiceException
     *             if the locale is not enabled for the project-iteration or
     *             server
     */
    private HLocale validateLocale(LocaleId localeId,
            HProjectIteration projectIteration) throws ZanataServiceException {
        String projectSlug = projectIteration.getProject().getSlug();
        return localeServiceImpl.validateLocaleByProjectIteration(localeId,
                projectSlug, projectIteration.getSlug());
    }

    /**
     * Sends out an event to signal that a Text Flow target has been translated
     */
    private void signalPostTranslateEvent(Long actorId,
            HTextFlowTarget hTextFlowTarget, ContentState oldState) {
        if (Events.exists()) {
            HTextFlow textFlow = hTextFlowTarget.getTextFlow();
            Long documentId = textFlow.getDocument().getId();
            Long versionId =
                    textFlow.getDocument().getProjectIteration().getId();
            // TODO remove hasError from DocumentStatus, so that we can pass
            // everything else directly to cache
            // DocumentStatus docStatus = new DocumentStatus(
            // new DocumentId(document.getId(), document.getDocId()), hasError,
            // hTextFlowTarget.getLastChanged(),
            // hTextFlowTarget.getLastModifiedBy().getAccount().getUsername());
            textFlowTargetStateEvent.fireAfterSuccess(
                    new TextFlowTargetStateEvent(actorId, versionId,
                            documentId, textFlow.getId(), hTextFlowTarget
                            .getLocale().getLocaleId(), hTextFlowTarget
                            .getId(), hTextFlowTarget.getState(),
                            oldState));
        }
    }

    public class TranslationDetails {
        private final String revisionComment;
        private final EntityType copiedEntityType;
        private final TranslationSourceType sourceType;
        private final Long copiedEntityId;

        public TranslationDetails(String revisionComment,
            EntityType copiedEntityType, TranslationSourceType sourceType,
            Long copiedEntityId) {
            this.revisionComment = revisionComment;
            this.sourceType = sourceType;
            this.copiedEntityType = copiedEntityType;
            this.copiedEntityId = copiedEntityId;
        }

        public TranslationDetails(TransUnitUpdateRequest request) {
            this.revisionComment = request.getRevisionComment();
            this.copiedEntityId = request.getCopiedEntityId();
            copiedEntityType =
                    StringUtils.isEmpty(request.getCopiedEntityType()) ? null
                            : EntityType.getValueOf(request
                                    .getCopiedEntityType());
            sourceType =
                    StringUtils.isEmpty(request.getSourceType()) ? null
                            : TranslationSourceType.getValueOf(request
                                    .getSourceType());
        }

        public String getRevisionComment() {
            return revisionComment;
        }

        public EntityType getCopiedEntityType() {
            return copiedEntityType;
        }

        public TranslationSourceType getSourceType() {
            return sourceType;
        }

        public Long getCopiedEntityId() {
            return copiedEntityId;
        }
    }

    private boolean translate(@Nonnull HTextFlowTarget hTextFlowTarget,
            @Nonnull List<String> contentsToSave, ContentState requestedState,
            int nPlurals, TranslationDetails details) {
        boolean targetChanged = false;
        ContentState currentState = hTextFlowTarget.getState();
        targetChanged |= setContentIfChanged(hTextFlowTarget, contentsToSave);
        targetChanged |=
                setContentStateIfChanged(requestedState, hTextFlowTarget,
                        nPlurals);

        if (targetChanged || hTextFlowTarget.getVersionNum() == 0) {
            HTextFlow textFlow = hTextFlowTarget.getTextFlow();
            hTextFlowTarget.setVersionNum(hTextFlowTarget.getVersionNum() + 1);
            hTextFlowTarget.setTextFlowRevision(textFlow.getRevision());
            hTextFlowTarget.setLastModifiedBy(authenticatedAccount.getPerson());

            hTextFlowTarget.setRevisionComment(details.getRevisionComment());
            hTextFlowTarget.setCopiedEntityType(details.getCopiedEntityType());
            hTextFlowTarget.setSourceType(details.getSourceType());
            hTextFlowTarget.setCopiedEntityId(details.getCopiedEntityId());

            log.debug("last modified by :{}", authenticatedAccount.getPerson()
                    .getName());
        }

        // save the target histories
        entityManager.flush();

        // fire event after flush
        if (targetChanged || hTextFlowTarget.getVersionNum() == 0) {
            this.signalPostTranslateEvent(authenticatedAccount.getPerson()
                    .getId(), hTextFlowTarget, currentState);
        }

        return targetChanged;
    }

    /**
     * @return true if the content was changed, false otherwise
     */
    private boolean setContentIfChanged(
            @Nonnull HTextFlowTarget hTextFlowTarget,
            @Nonnull List<String> contentsToSave) {
        if (!contentsToSave.equals(hTextFlowTarget.getContents())) {
            hTextFlowTarget.setContents(contentsToSave);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check that requestedState is valid for the given content, adjust if
     * necessary and set the new state if it has changed.
     *
     * @return true if the content state or contents list were updated, false
     *         otherwise
     * @see #adjustContentsAndState(org.zanata.model.HTextFlowTarget, int,
     *      java.util.List)
     */
    private boolean setContentStateIfChanged(
            @Nonnull ContentState requestedState,
            @Nonnull HTextFlowTarget target, int nPlurals) {
        boolean changed = false;
        ContentState previousState = target.getState();
        target.setState(requestedState);
        ArrayList<String> warnings = new ArrayList<String>();
        changed |= adjustContentsAndState(target, nPlurals, warnings);
        for (String warning : warnings) {
            log.warn(warning);
        }

        if (isReviewState(target.getState())) {
            // reviewer saved it
            target.setReviewer(authenticatedAccount.getPerson());
            if (previousState == ContentState.New) {
                target.setTranslator(authenticatedAccount.getPerson());
            }
        } else {
            target.setTranslator(authenticatedAccount.getPerson());
        }

        if (target.getState() != previousState) {
            changed = true;
        }
        return changed;
    }

    /**
     * Checks target state against its contents. If necessary, modifies target
     * state and generates a warning
     *
     * @param target
     *            HTextFlowTarget to check/modify
     * @param nPlurals
     *            number of plurals for this locale for this message: use 1 if
     *            message does not support plurals
     * @param warnings
     *            a warning string will be added if state is adjusted
     * @return true if and only if some state was changed
     */
    private static boolean adjustContentsAndState(
            @Nonnull HTextFlowTarget target, int nPlurals,
            @Nonnull List<String> warnings) {
        ContentState oldState = target.getState();
        String resId = target.getTextFlow().getResId();
        boolean contentsChanged =
                ensureContentsSize(target, nPlurals, resId, warnings);

        List<String> contents = target.getContents();
        target.setState(ContentStateUtil.determineState(oldState, contents,
                resId, warnings));
        boolean stateChanged = (oldState != target.getState());
        return contentsChanged || stateChanged;
    }

    /**
     * Ensures that target.contents has exactly legalSize elements
     *
     * @param target
     *            HTextFlowTarget to check/modify
     * @param legalSize
     *            required number of contents
     * @param resId
     *            ID of target
     * @param warnings
     *            if elements were added or removed
     * @return
     */
    private static boolean ensureContentsSize(HTextFlowTarget target,
            int legalSize, String resId, @Nonnull List<String> warnings) {
        int contentsSize = target.getContents().size();
        if (contentsSize < legalSize) {
            String warning =
                    "Should have "
                            + legalSize
                            + " contents; adding empty strings: TextFlowTarget "
                            + resId + " with contents: " + target.getContents();
            warnings.add(warning);
            List<String> newContents = new ArrayList<String>(legalSize);
            newContents.addAll(target.getContents());
            while (newContents.size() < legalSize) {
                newContents.add("");
            }
            target.setContents(newContents);
            return true;
        } else if (contentsSize > legalSize) {
            String warning =
                    "Should have "
                            + legalSize
                            + " contents; discarding extra strings: TextFlowTarget "
                            + resId + " with contents: " + target.getContents();
            warnings.add(warning);
            List<String> newContents = new ArrayList<String>(legalSize);
            for (int i = 0; i < contentsSize; i++) {
                String content = target.getContents().get(i);
                newContents.add(content);
            }
            target.setContents(newContents);
            return true;
        }
        return false;
    }

    @Override
    // This will not run in a transaction. Instead, transactions are controlled
    // within the method itself.
    @Transactional(TransactionPropagationType.NEVER)
    @Async
    public
    Future<List<String>> translateAllInDocAsync(String projectSlug,
            String iterationSlug, String docId, LocaleId locale,
            TranslationsResource translations, Set<String> extensions,
            MergeType mergeType, boolean assignCreditToUploader, boolean lock,
            AsyncTaskHandle handle, TranslationSourceType translationSourceType) {
        // Lock this document for push
        Lock transLock = null;
        if (lock) {
            transLock =
                    new Lock(projectSlug, iterationSlug, docId, locale, "push");
            lockManagerServiceImpl.attain(transLock);
        }

        List<String> messages = Lists.newArrayList();
        try {
            messages =
                    this.translateAllInDoc(projectSlug, iterationSlug, docId,
                            locale, translations, extensions, mergeType,
                            assignCreditToUploader, handle, translationSourceType);
        } finally {
            if (lock) {
                lockManagerServiceImpl.release(transLock);
            }
        }
        return AsyncTaskResult.taskResult(messages);
    }

    /**
     * Run enforced validation check(Error) if target has changed and
     * translation saving as 'Translated' or 'Approved'
     *
     * @param newState
     * @param projectVersion
     * @param targetId
     * @param sources
     * @param translations
     * @return error messages
     */
    private String validateTranslations(ContentState newState,
            HProjectIteration projectVersion, String targetId,
            List<String> sources, List<String> translations) {
        String message = null;
        if (newState.isTranslated()) {
            List<String> validationMessages =
                    validationServiceImpl.validateWithServerRules(
                            projectVersion, sources, translations,
                            ValidationAction.State.Error);

            if (!validationMessages.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String validationMessage : validationMessages) {
                    sb.append(validationMessage).append("\n");
                }
                message =
                        msgs.format("jsf.TranslationContainsError",
                                ShortString.shorten(translations.get(0)),
                                sb.toString());
            }
        }
        return message;
    }

    @Override
    public List<String> translateAllInDoc(final String projectSlug,
            final String iterationSlug, final String docId,
            final LocaleId locale, final TranslationsResource translations,
            final Set<String> extensions, final MergeType mergeType,
            final boolean assignCreditToUploader,
            final TranslationSourceType translationSourceType) {
        return translateAllInDoc(projectSlug, iterationSlug, docId, locale,
                translations, extensions, mergeType, assignCreditToUploader,
                null, translationSourceType);
    }

    @Override
    public List<String> translateAllInDoc(final String projectSlug,
            final String iterationSlug, final String docId,
            final LocaleId locale, final TranslationsResource translations,
            final Set<String> extensions, final MergeType mergeType,
            final boolean assignCreditToUploader, AsyncTaskHandle handle,
            final TranslationSourceType translationSourceType) {
        final HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);

        if (hProjectIteration == null) {
            throw new ZanataServiceException("Version '" + iterationSlug
                    + "' for project '" + projectSlug + "' ");
        }

        if (mergeType == MergeType.IMPORT) {
            identity.checkPermission("import-translation", hProjectIteration);
        }

        ResourceUtils.validateExtensions(extensions);

        log.debug("pass evaluate");
        final HDocument document =
                documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
        if (document == null || document.isObsolete()) {
            throw new ZanataServiceException("A document was not found.", 404);
        }

        log.debug("start put translations entity:{}", translations);

        boolean changed = false;

        final HLocale hLocale =
                localeServiceImpl.validateLocaleByProjectIteration(locale,
                        projectSlug, iterationSlug);
        final Optional<AsyncTaskHandle> handleOp =
                Optional.fromNullable(handle);

        if (handleOp.isPresent()) {
            handleOp.get().setMaxProgress(
                    translations.getTextFlowTargets().size());
        }

        try {
            changed |= new Work<Boolean>() {
                @Override
                protected Boolean work() throws Exception {
                    // handle extensions
                    boolean changed =
                            resourceUtils
                                    .transferFromTranslationsResourceExtensions(
                                            translations.getExtensions(true),
                                            document, extensions, hLocale,
                                            mergeType);
                    return changed;
                }
            }.workInTransaction();
        } catch (Exception e) {
            log.error("exception in transferFromTranslationsResourceExtensions: {}", e.getMessage());
            throw new ZanataServiceException("Error during translation.", 500,
                    e);
        }

        // NB: removedTargets only applies for MergeType.IMPORT
        final Collection<HTextFlowTarget> removedTargets =
                new HashSet<HTextFlowTarget>();
        final List<String> warnings = new ArrayList<String>();

        if (mergeType == MergeType.IMPORT) {
            for (HTextFlow textFlow : document.getTextFlows()) {
                HTextFlowTarget hTarget =
                        textFlow.getTargets().get(hLocale.getId());
                if (hTarget != null) {
                    removedTargets.add(hTarget);
                }
            }
        }

        // Break the target into batches
        List<List<TextFlowTarget>> batches =
                Lists.partition(translations.getTextFlowTargets(), BATCH_SIZE);

        for (final List<TextFlowTarget> batch : batches) {
            try {
                SaveBatchWork work = new SaveBatchWork();
                work.setExtensions(extensions);
                work.setWarnings(warnings);
                work.setLocale(hLocale);
                work.setDocument(document);
                work.setMergeType(mergeType);
                work.setRemovedTargets(removedTargets);
                work.setHandleOp(handleOp);
                work.setProjectIterationId(hProjectIteration.getId());
                work.setBatch(batch);
                work.setAssignCreditToUploader(assignCreditToUploader);
                work.setTranslationSourceType(translationSourceType);
                changed |= work.workInTransaction();
            } catch (Exception e) {
                log.error("exception in SaveBatchWork:{}", e.getMessage());
                throw new ZanataServiceException("Error during translation.",
                        500, e);
            }

        }

        if (changed || !removedTargets.isEmpty()) {
            try {
                new Work<Void>() {
                    @Override
                    protected Void work() throws Exception {
                        for (HTextFlowTarget target : removedTargets) {
                            target =
                                    textFlowTargetDAO.findById(target.getId(),
                                            true); // need to refresh from
                                                   // persistence
                            target.clear();
                        }
                        textFlowTargetDAO.flush();

                        documentDAO.flush();
                        return null;
                    }
                }.workInTransaction();

                Long actorId = authenticatedAccount.getPerson().getId();
                documentUploadedEvent.fire(new DocumentUploadedEvent(
                        actorId,
                        document.getId(), false,
                        hLocale.getLocaleId()));
            } catch (Exception e) {
                log.error("exception in removeTargets: {}", e.getMessage());
                throw new ZanataServiceException("Error during translation.",
                        500, e);
            }
        }

        return warnings;
    }

    private int getNumPlurals(HLocale hLocale, HTextFlow textFlow) {
        int nPlurals;
        if (!textFlow.isPlural()) {
            nPlurals = 1;
        } else {
            nPlurals =
                    resourceUtils
                            .getNumPlurals(textFlow.getDocument(), hLocale);
        }
        return nPlurals;
    }

    @Getter
    @Setter
    private final class SaveBatchWork extends Work<Boolean> {
        private Set<String> extensions;
        private List<String> warnings;
        private HLocale locale;
        private HDocument document;
        private MergeType mergeType;
        private Collection<HTextFlowTarget> removedTargets;
        private Optional<AsyncTaskHandle> handleOp;
        private Long projectIterationId;
        private List<TextFlowTarget> batch;
        private boolean assignCreditToUploader;
        private TranslationSourceType translationSourceType;

        @Override
        protected Boolean work() throws Exception {
            // we need to call clear at the beginning because text flow target
            // history rely on after commit callback.
            textFlowTargetDAO.clear();
            document = entityManager.find(HDocument.class, document.getId());
            boolean changed = false;

            // we need a fresh object in this session,
            // so that it can lazily load associated objects
            HProjectIteration iteration =
                    projectIterationDAO.findById(projectIterationId);
            Map<String, HTextFlow> resIdToTextFlowMap = textFlowDAO.getByDocumentAndResIds(document, Lists.transform(
                    batch, new Function<TextFlowTarget, String>() {

                        @Override
                        public String apply(TextFlowTarget input) {
                            return input.getResId();
                        }
                    }));
            final int numPlurals = resourceUtils
                    .getNumPlurals(document, locale);
            for (TextFlowTarget incomingTarget : batch) {
                String resId = incomingTarget.getResId();
                String sourceHash = incomingTarget.getSourceHash();
                HTextFlow textFlow = resIdToTextFlowMap.get(resId);
                if (textFlow == null) {
                    // return warning for unknown resId to caller
                    String warning =
                            "Could not find TextFlow for TextFlowTarget "
                                    + resId + " with contents: "
                                    + incomingTarget.getContents();
                    warnings.add(warning);
                    log.warn("skipping TextFlowTarget with unknown resId: {}",
                            resId);
                } else if (sourceHash != null
                        && !sourceHash.equals(textFlow.getContentHash())) {
                    String warning =
                            MessageFormat
                                    .format("TextFlowTarget {0} may be obsolete; "
                                            + "associated source hash: {1}; "
                                            + "expected hash is {2} for source: {3}",
                                            resId, sourceHash,
                                            textFlow.getContentHash(),
                                            textFlow.getContents());
                    warnings.add(warning);
                    log.warn(
                            "skipping TextFlowTarget {} with unknown sourceHash: {}",
                            resId, sourceHash);
                } else {
                    String validationMessage =
                            validateTranslations(incomingTarget.getState(),
                                    iteration,
                                    incomingTarget.getResId(),
                                    textFlow.getContents(),
                                    incomingTarget.getContents());

                    if (!StringUtils.isEmpty(validationMessage)) {
                        warnings.add(validationMessage);
                        log.warn(validationMessage);
                        continue;
                    }

                    int nPlurals = textFlow.isPlural() ? numPlurals : 1;
                    // we have eagerly loaded all targets upfront
                    HTextFlowTarget hTarget = textFlow.getTargets().get(locale.getId());
                    ContentState currentState = ContentState.New;
                    if (hTarget != null) {
                        currentState = hTarget.getState();
                    }

                    if (mergeType == MergeType.IMPORT) {
                        removedTargets.remove(hTarget);
                    }

                    TranslationMergeServiceFactory.MergeContext mergeContext =
                            new TranslationMergeServiceFactory.MergeContext(
                                    mergeType, textFlow, locale, hTarget,
                                    nPlurals);
                    TranslationMergeService mergeService =
                            translationMergeServiceFactory
                                    .getMergeService(mergeContext);

                    boolean targetChanged =
                            mergeService.merge(incomingTarget, hTarget,
                                    extensions);
                    if (hTarget == null) {
                        // in case hTarget was null, we need to
                        // retrieve it after merge
                        hTarget = textFlow.getTargets().get(locale.getId());
                    }
                    targetChanged |=
                            adjustContentsAndState(hTarget, nPlurals, warnings);
                    // update translation information if applicable
                    if (targetChanged) {
                        hTarget.setVersionNum(hTarget.getVersionNum() + 1);

                        changed = true;
                        Long actorId;
                        if (assignCreditToUploader){
                            HPerson hPerson = authenticatedAccount.getPerson();
                            hTarget.setTranslator(hPerson);
                            hTarget.setLastModifiedBy(hPerson);
                            actorId = hPerson.getId();
                        } else {
                            hTarget.setTranslator(null);
                            hTarget.setLastModifiedBy(authenticatedAccount.getPerson());
                            actorId = null;
                        }
                        hTarget.setSourceType(translationSourceType);
                        hTarget.setCopiedEntityId(null);
                        hTarget.setCopiedEntityId(null);
                        textFlowTargetDAO.makePersistent(hTarget);
                        signalPostTranslateEvent(actorId, hTarget, currentState);
                    }
                }

                if (handleOp.isPresent()) {
                    handleOp.get().increaseProgress(1);
                }
            }
            textFlowTargetDAO.flush();

            return changed;
        }

    }

    public static class TranslationResultImpl implements TranslationResult {
        private HTextFlowTarget translatedTextFlowTarget;
        private boolean isSuccess;
        private boolean targetChanged = false;
        private boolean isVersionNumConflict = false;
        private int baseVersion;
        private ContentState baseContentState;
        private String errorMessage;

        @Override
        public boolean isTranslationSuccessful() {
            return isSuccess;
        }

        @Override
        public boolean isVersionNumConflict() {
            return isVersionNumConflict;
        }

        @Override
        public boolean isTargetChanged() {
            return targetChanged;
        }

        @Override
        public HTextFlowTarget getTranslatedTextFlowTarget() {
            return translatedTextFlowTarget;
        }

        @Override
        public int getBaseVersionNum() {
            return baseVersion;
        }

        @Override
        public ContentState getBaseContentState() {
            return baseContentState;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

    }

    @Override
    public List<TranslationResult> revertTranslations(LocaleId localeId,
            List<TransUnitUpdateInfo> translationsToRevert) {
        List<TranslationResult> results = new ArrayList<TranslationResult>();
        List<TransUnitUpdateRequest> updateRequests =
                new ArrayList<TransUnitUpdateRequest>();
        if (!translationsToRevert.isEmpty()) {

            HTextFlow sampleHTextFlow =
                    entityManager.find(HTextFlow.class, translationsToRevert
                            .get(0).getTransUnit().getId().getValue());
            HLocale hLocale =
                    validateLocale(localeId, sampleHTextFlow.getDocument()
                            .getProjectIteration());
            for (TransUnitUpdateInfo info : translationsToRevert) {
                if (!info.isSuccess() || !info.isTargetChanged()) {
                    continue;
                }

                TransUnitId tuId = info.getTransUnit().getId();
                HTextFlow hTextFlow =
                        entityManager.find(HTextFlow.class, tuId.getValue());
                HTextFlowTarget hTextFlowTarget =
                        textFlowTargetDAO.getOrCreateTarget(hTextFlow, hLocale);

                // check that version has not advanced
                // TODO probably also want to check that source has not been
                // updated
                Integer versionNum = hTextFlowTarget.getVersionNum();
                log.debug(
                        "about to revert hTextFlowTarget version {} to TransUnit version {}",
                        versionNum, info.getTransUnit().getVerNum());
                if (versionNum.equals(info.getTransUnit().getVerNum())) {
                    // look up replaced version
                    HTextFlowTargetHistory oldTarget =
                            hTextFlowTarget.getHistory().get(
                                    info.getPreviousVersionNum());
                    if (oldTarget != null) {
                        // generate request
                        List<String> oldContents = oldTarget.getContents();
                        ContentState oldState = oldTarget.getState();
                        TransUnitUpdateRequest request =
                                new TransUnitUpdateRequest(tuId, oldContents,
                                        oldState, versionNum,
                                        oldTarget.getRevisionComment(),
                                        oldTarget.getCopiedEntityId(), oldTarget
                                                .getCopiedEntityType().getAbbr(),
                                        oldTarget.getSourceType().getAbbr());
                        // add to list
                        updateRequests.add(request);
                    } else {
                        log.info(
                                "got null previous target for tu with id {}, version {}. Assuming previous state is untranslated",
                                hTextFlow.getId(), info.getPreviousVersionNum());
                        List<String> emptyContents = Lists.newArrayList();
                        for (int i = 0; i < hTextFlowTarget.getContents()
                                .size(); i++) {
                            emptyContents.add("");
                        }
                        TransUnitUpdateRequest request =
                                new TransUnitUpdateRequest(tuId, emptyContents,
                                        ContentState.New, versionNum,
                                        TranslationSourceType.UNKNOWN.getAbbr());
                        updateRequests.add(request);
                    }
                } else {
                    log.info(
                            "attempt to revert target version {} for tu with id {}, but current version is {}. Not reverting.",
                            new Object[] { info.getTransUnit().getVerNum(),
                                    tuId, versionNum });
                    results.add(buildFailResult(hTextFlowTarget));
                }
            }
        }
        results.addAll(translateWithoutValidating(localeId, updateRequests));
        return results;
    }

    /**
     * @param hTextFlowTarget
     * @return
     */
    private TranslationResultImpl buildFailResult(
            HTextFlowTarget hTextFlowTarget) {
        TranslationResultImpl result = new TranslationResultImpl();
        result.baseVersion = hTextFlowTarget.getVersionNum();
        result.baseContentState = hTextFlowTarget.getState();
        result.isSuccess = false;
        result.isVersionNumConflict = false;
        result.translatedTextFlowTarget = hTextFlowTarget;
        result.errorMessage = null;
        return result;
    }
}
