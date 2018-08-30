/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.editor.service;

import static org.zanata.async.AsyncTaskKey.joinFields;

import java.io.Serializable;
import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.AsyncTaskKey;
import org.zanata.async.GenericAsyncTaskKey;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.LocaleId;
import org.zanata.i18n.Messages;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeCancelRequest;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class TransMemoryMergeManager implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(TransMemoryMergeManager.class);
    private static final long serialVersionUID = 1364316697376958035L;
    private static final String KEY_NAME = "TMMergeForVerKey";

    private final AsyncTaskHandleManager asyncTaskHandleManager;

    private final TransMemoryMergeService transMemoryMergeService;

    private final ZanataIdentity identity;
    @Inject
    private Messages messages;

    @Inject
    public TransMemoryMergeManager(
            AsyncTaskHandleManager asyncTaskHandleManager,
            TransMemoryMergeService transMemoryMergeService,
            ZanataIdentity identity) {
        this.asyncTaskHandleManager = asyncTaskHandleManager;
        this.transMemoryMergeService = transMemoryMergeService;
        this.identity = identity;
    }

    /**
     * start an async TM merge operation for given request.
     *
     * @param request
     *            REST request for TM merge
     * @return number of untranslated text flows for given document and locale
     * @throws UnsupportedOperationException
     *             if there is already a task running
     */
    public boolean startTransMemoryMerge(TransMemoryMergeRequest request) {
        TMMergeForDocTaskKey key =
                new TMMergeForDocTaskKey(
                        request.documentId, request.localeId);
        AsyncTaskHandle<?> handleByKey =
                asyncTaskHandleManager.getHandleByKey(key);
        if (AsyncTaskHandle.taskIsNotRunning(handleByKey)) {
            TransMemoryMergeTaskHandle handle = new TransMemoryMergeTaskHandle();
            handle.setTriggeredBy(identity.getAccountUsername());
            handle.setTaskName(messages.format("jsf.tasks.translationMemoryMerge",
                    request.projectIterationId.getProjectSlug(),
                    request.projectIterationId.getIterationSlug()));
            asyncTaskHandleManager.registerTaskHandle(handle, key);
            transMemoryMergeService.executeMergeAsync(request, handle);
            return true;
        }
        return false;
    }

    public boolean cancelTransMemoryMerge(TransMemoryMergeCancelRequest request) {
        TMMergeForDocTaskKey key =
                new TMMergeForDocTaskKey(
                        request.documentId, request.localeId);
        AsyncTaskHandle<?> handleByKey =
                asyncTaskHandleManager.getHandleByKey(key);
        if (AsyncTaskHandle.taskIsNotRunning(handleByKey)) {
            return false;
        }
        TransMemoryMergeTaskHandle handle =
                (TransMemoryMergeTaskHandle) handleByKey;
        String triggeredBy = handle.getTriggeredBy();
        if (Objects.equals(identity.getAccountUsername(), triggeredBy)) {
            handle.cancel(true);
            handle.setCancelledTime(System.currentTimeMillis());
            handle.setCancelledBy(identity.getAccountUsername());
            log.info("task: {} cancelled by its creator", handle);
            return true;
        } else {
            log.warn("{} is attempting to cancel {}", identity.getAccountUsername(), handle);
        }
        return false;
    }

    public AsyncTaskHandle<Void> start(Long versionId, VersionTMMerge mergeRequest) {
        AsyncTaskKey key = makeKey(versionId, mergeRequest.getLocaleId());
        MergeTranslationsTaskHandle handleByKey =
                (MergeTranslationsTaskHandle) asyncTaskHandleManager.getHandleByKey(key);
        if (AsyncTaskHandle.taskIsNotRunning(handleByKey)) {
            handleByKey = new MergeTranslationsTaskHandle(key);
            handleByKey.setTaskName(
                    messages.format("jsf.tasks.TMMerge", mergeRequest.getLocaleId()));
            handleByKey.setTriggeredBy(identity.getAccountUsername());
            asyncTaskHandleManager.registerTaskHandle(handleByKey, key);
            transMemoryMergeService.startMergeTranslations(versionId,
                    mergeRequest, handleByKey);
        } else {
            log.warn(
                    "there is already a task running for version id {} and locale {}",
                    versionId, mergeRequest.getLocaleId());
            throw new UnsupportedOperationException("there is already a task running for version and locale");
        }
        return handleByKey;
    }

    @SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super class equals method is sufficient")
    static class TMMergeForDocTaskKey extends
            GenericAsyncTaskKey {

        private static final long serialVersionUID = -7210004008208642L;
        private static final String KEY_NAME = "TMMergeForDocKey";
        private final DocumentId documentId;
        private final LocaleId localeId;

        TMMergeForDocTaskKey(DocumentId documentId, LocaleId localeId) {
            // here we use numeric id to form the string id because it doesn't require URL encoding
            super(joinFields(KEY_NAME, documentId.getId(), localeId));
            this.documentId = documentId;
            this.localeId = localeId;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("documentId", documentId)
                    .add("localeId", localeId)
                    .toString();
        }
    }

    @VisibleForTesting
    protected static AsyncTaskKey makeKey(Long versionId, LocaleId localeId) {
        return new GenericAsyncTaskKey(joinFields(KEY_NAME, versionId, localeId));
    }

    @VisibleForTesting
    protected void setMessages(Messages msgs) {
        messages = msgs;
    }
}
