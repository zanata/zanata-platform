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
package org.zanata.rest.service;

import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.TranslationSourceType;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.RestUtil;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;

import static org.zanata.rest.dto.ProcessStatus.ProcessStatusCode;

/**
 * Default server-side implementation of the Asynchronous RunnableProcess
 * Resource.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequestScoped
@Named("asynchronousProcessResourceService")
@Path(AsynchronousProcessResource.SERVICE_PATH)
@Transactional
public class AsynchronousProcessResourceService
        implements AsynchronousProcessResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(AsynchronousProcessResourceService.class);

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private DocumentService documentServiceImpl;
    @Inject
    private TranslationService translationServiceImpl;
    @Inject
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private ZanataIdentity identity;

    @Deprecated
    @Override
    public ProcessStatus startSourceDocCreation(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans) {
        HProjectIteration hProjectIteration =
                retrieveAndCheckIteration(projectSlug, iterationSlug, true);
        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");
        ResourceUtils.validateExtensions(extensions); // gettext, comment
        HDocument document = documentDAO
                .getByDocIdAndIteration(hProjectIteration, resource.getName());
        // already existing non-obsolete document.
        if (document != null) {
            if (!document.isObsolete()) {
                // updates must happen through PUT on the actual resource
                ProcessStatus status = new ProcessStatus();
                status.setStatusCode(ProcessStatusCode.Failed);
                status.getMessages().add("A document with name "
                        + resource.getName() + " already exists.");
                return status;
            }
        }
        String name = "SourceDocCreation: " + projectSlug + "-" + iterationSlug
                + "-" + idNoSlash;
        AsyncTaskHandle<HDocument> handle = new AsyncTaskHandle<>();
        String keyId = asyncTaskHandleManager.registerTaskHandle(handle);
        documentServiceImpl
                .saveDocumentAsync(projectSlug, iterationSlug,
                        resource, extensions, copytrans, true, handle);
        logWhenUploadComplete(handle, name, keyId);
        return getProcessStatus(keyId); // TODO Change to return 202
        // Accepted,
        // with a url to get the
        // progress
    }

    @Deprecated
    @Override
    public ProcessStatus startSourceDocCreationOrUpdate(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans) {
        String docId = RestUtil.convertFromDocumentURIId(idNoSlash);
        return startSourceDocCreationOrUpdateProcess(projectSlug,
                iterationSlug, resource, extensions, docId, copytrans);
    }

    @Override
    public ProcessStatus startSourceDocCreationOrUpdateWithDocId(
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, String docId) {
        boolean copyTrans = false;
        return startSourceDocCreationOrUpdateProcess(projectSlug,
                iterationSlug, resource, extensions, docId, copyTrans);
    }

    private ProcessStatus startSourceDocCreationOrUpdateProcess(
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, String docId, boolean copyTrans) {
        if (StringUtils.isBlank(docId)) {
            throw new BadRequestException("missing id");
        }
        HProjectIteration hProjectIteration =
                retrieveAndCheckIteration(projectSlug, iterationSlug, true);
        ResourceUtils.validateExtensions(extensions); // gettext, comment
        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");
        String name = "SourceDocCreationOrUpdate: " + projectSlug + "-"
                + iterationSlug + "-" + docId;
        AsyncTaskHandle<HDocument> handle = new AsyncTaskHandle<HDocument>();
        String keyId = asyncTaskHandleManager.registerTaskHandle(handle);
        documentServiceImpl
                .saveDocumentAsync(projectSlug, iterationSlug, resource,
                        extensions, copyTrans, true, handle);
        logWhenUploadComplete(handle, name, keyId);
        return getProcessStatus(keyId); // TODO Change to return 202
        // Accepted, with a url to get the progress
    }

    private <T> void logWhenUploadComplete(
            AsyncTaskHandle<T> taskHandle,
            final String taskName, final String taskId) {
        taskHandle.whenTaskComplete((result, throwable) -> {
            if (throwable != null) {
                log.warn("async upload failed. id={}, job={}", taskId, taskName,
                        throwable);
            } else {
                log.info("async upload complete. id={}, job={}, result={}",
                        taskId, taskName, result);
            }
        });
    }

    @Deprecated
    @Override
    public ProcessStatus startTranslatedDocCreationOrUpdate(
            final String idNoSlash, final String projectSlug,
            final String iterationSlug, final LocaleId locale,
            final TranslationsResource translatedDoc,
            final Set<String> extensions, final String merge,
            final boolean assignCreditToUploader) {
        final String id = RestUtil.convertFromDocumentURIId(idNoSlash);
        return startTranslatedDocCreationOrUpdateWithDocId(projectSlug,
                iterationSlug, locale, translatedDoc, id, extensions, merge,
                assignCreditToUploader);
    }

    @Override
    public ProcessStatus startTranslatedDocCreationOrUpdateWithDocId(
            String projectSlug, String iterationSlug, LocaleId locale,
            TranslationsResource translatedDoc, String docId,
            Set<String> extensions,
            String merge, boolean assignCreditToUploader) {
        // check security (cannot be on @Restrict as it refers to method
        // parameters)
        identity.checkPermission("modify-translation",
                this.localeServiceImpl.getByLocaleId(locale),
                this.getSecuredIteration(projectSlug, iterationSlug)
                        .getProject());
        if (StringUtils.isBlank(docId)) {
            throw new BadRequestException("missing docId");
        }
        MergeType mergeType;
        try {
            mergeType = MergeType.valueOf(merge.toUpperCase());
        } catch (Exception e) {
            ProcessStatus status = new ProcessStatus();
            status.setStatusCode(ProcessStatusCode.Failed);
            status.getMessages().add("bad merge type " + merge);
            return status;
        }
        final MergeType finalMergeType = mergeType;
        String taskName =
                "TranslatedDocUpload: " + projectSlug + "-" + iterationSlug +
                        "-" + docId;
        AsyncTaskHandle<HDocument> handle = new AsyncTaskHandle<>();
        String keyId = asyncTaskHandleManager.registerTaskHandle(handle);
        translationServiceImpl.translateAllInDocAsync(projectSlug,
                iterationSlug, docId, locale, translatedDoc, extensions,
                finalMergeType, assignCreditToUploader, true, handle,
                TranslationSourceType.API_UPLOAD);
        logWhenUploadComplete(handle, taskName, keyId);
        return this.getProcessStatus(keyId);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ProcessStatus getProcessStatus(String processId) {
        AsyncTaskHandle handle =
                asyncTaskHandleManager.getHandleByKeyId(processId);
        if (handle == null) {
            throw new NotFoundException(
                    "A process was not found for id " + processId);
        }
        return AsyncProcessService.handleToProcessStatus(handle, processId);
    }

    private HProjectIteration retrieveAndCheckIteration(String projectSlug,
            String iterationSlug, boolean writeOperation) {
        HProjectIteration hProjectIteration =
                projectIterationDAO.getBySlug(projectSlug, iterationSlug);
        HProject hProject = hProjectIteration == null ? null
                : hProjectIteration.getProject();
        if (hProjectIteration == null) {
            throw new NoSuchEntityException("Project Iteration \'" + projectSlug
                    + ":" + iterationSlug + "\' not found.");
        } else if (hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE)
                || hProject.getStatus().equals(EntityStatus.OBSOLETE)) {
            throw new NoSuchEntityException("Project Iteration \'" + projectSlug
                    + ":" + iterationSlug + "\' not found.");
        } else if (writeOperation) {
            if (hProjectIteration.getStatus().equals(EntityStatus.READONLY)
                    || hProject.getStatus().equals(EntityStatus.READONLY)) {
                throw new ReadOnlyEntityException(
                        "Project Iteration \'" + projectSlug + ":"
                                + iterationSlug + "\' is read-only.");
            } else {
                return hProjectIteration;
            }
        } else {
            return hProjectIteration;
        }
    }

    public HProjectIteration getSecuredIteration(String projectSlug,
            String iterationSlug) {
        return retrieveAndCheckIteration(projectSlug, iterationSlug, false);
    }
}
