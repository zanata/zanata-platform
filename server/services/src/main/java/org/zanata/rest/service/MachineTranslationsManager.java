/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.service;

import static org.zanata.async.AsyncTaskKey.joinFields;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.GenericAsyncTaskKey;
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.MachineTranslationPrefill;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.MachineTranslationService;
import org.zanata.webtrans.shared.model.ProjectIterationId;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MachineTranslationsManager {
    private static final Logger log =
            LoggerFactory.getLogger(MachineTranslationsManager.class);

    @Inject
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private MachineTranslationService machineTranslationService;
    @Inject
    private Messages messages;

    /**
     * Fill a project version with translations from an MT source
     *
     * @param projectIteration version to fill
     * @param prefillRequest options for MT fill
     * @return async handle for process
     */
    public AsyncTaskHandle<Void> prefillVersionWithMachineTranslations(
            HProjectIteration projectIteration,
            MachineTranslationPrefill prefillRequest) {
        MachineTranslationPrefillTaskHandle taskHandle =
                createMTTaskHandle(projectIteration, prefillRequest);
        machineTranslationService.prefillProjectVersionWithMachineTranslation(
                projectIteration.getId(), prefillRequest, taskHandle);
        return taskHandle;
    }

    /**
     * Fill a single document with translations from an MT source
     *
     * @param documentId id of target document
     * @param projectIteration version to fill
     * @param prefillRequest options for MT fill
     * @return async handle for process
     */
    public AsyncTaskHandle<Void> prefillDocumentWithMachineTranslations(
            long documentId, HProjectIteration projectIteration,
            MachineTranslationPrefill prefillRequest) {
        MachineTranslationPrefillTaskHandle taskHandle =
                createMTTaskHandle(projectIteration, prefillRequest);
        machineTranslationService.prefillDocumentWithMachineTranslation(
                documentId, prefillRequest, taskHandle);
        return taskHandle;
    }

    /**
     * Create a Machine Translation task handle for the given version, failing
     * with an UnsupportedOperationException if one already exists

     * @param projectIteration version to create handle for
     * @param prefillRequest machine translation request for handle
     * @return async task handle
     * @throws UnsupportedOperationException if task exists
     */
    private MachineTranslationPrefillTaskHandle createMTTaskHandle(
            HProjectIteration projectIteration,
            MachineTranslationPrefill prefillRequest) {
        String projectSlug = projectIteration.getProject().getSlug();
        String versionSlug = projectIteration.getSlug();
        ProjectIterationId projectIterationId =
                new ProjectIterationId(projectSlug, versionSlug,
                        projectIteration.getProjectType());
        MachineTranslationsForVersionTaskKey taskKey =
                new MachineTranslationsForVersionTaskKey(projectIterationId);
        MachineTranslationPrefillTaskHandle taskHandle =
                (MachineTranslationPrefillTaskHandle) asyncTaskHandleManager.getHandleByKey(taskKey);

        if (AsyncTaskHandle.taskIsNotRunning(taskHandle)) {
            taskHandle = new MachineTranslationPrefillTaskHandle(taskKey);
            taskHandle.setTaskName(
                    messages.format("jsf.tasks.machineTranslation", projectSlug,
                            versionSlug, prefillRequest.getToLocale().getId()));
            taskHandle.setTriggeredBy(authenticatedAccount.getUsername());
            taskHandle.setTargetVersion(projectIterationId.toString());
            asyncTaskHandleManager.registerTaskHandle(taskHandle, taskKey);
        } else {
            log.warn("there is already a task running {}", taskKey);
            throw new UnsupportedOperationException("task already running");
        }

        return taskHandle;
    }

    @SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "super class equals method is sufficient")
    public static class MachineTranslationsForVersionTaskKey extends
            GenericAsyncTaskKey {

        private static final String KEY_NAME = "MachineTranslationsForVersionTaskKey";
        private static final long serialVersionUID = -6461799115582311574L;

        public MachineTranslationsForVersionTaskKey(ProjectIterationId projectIterationId) {
            super(joinFields(KEY_NAME, projectIterationId.toString()));
        }
    }
}
