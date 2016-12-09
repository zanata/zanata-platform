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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
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
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.zanata.rest.dto.ProcessStatus.ProcessStatusCode;

/**
 * Default server-side implementation of the Asynchronous RunnableProcess
 * Resource.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequestScoped
@Named("asynchronousProcessResourceService")
@Path(AsynchronousProcessResource.SERVICE_PATH)
@Transactional
@Slf4j
@Deprecated
public class AsynchronousProcessResourceService implements
        AsynchronousProcessResource {
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
    private ProjectUtil projectUtil;

    @Inject
    private ResourceUtils resourceUtils;

    @Inject
    private ZanataIdentity identity;

    // POST (fail if document already exists) - not currently used by the Java client
    @Override
    // TODO docId (idNoSlash) should not be part of the URL for POST - should use resource.name
    public ProcessStatus startSourceDocCreation(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans) {
        return startSourceDocCreationOrUpdate(idNoSlash, projectSlug, iterationSlug, resource, extensions, copytrans, true);
    }

    // PUT
    // TODO it should, when the doc already exists
    @Override
    public ProcessStatus startSourceDocCreationOrUpdate(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans) {
        return startSourceDocCreationOrUpdate(idNoSlash, projectSlug, iterationSlug, resource, extensions, copytrans, false);
    }

    private ProcessStatus startSourceDocCreationOrUpdate(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans, final boolean httpPost) {
        // The client really ought to provide clean docIds, but we strip
        // leading '/' chars to make the comparison as lenient as possible
        // before throwing an error.
        String normalisedDocId = stripStart(URIHelper.convertFromDocumentURIId(idNoSlash), "/");
        String normalisedResourceName = stripStart(resource.getName(), "/");

        // We used to use resource.getName() without checking (ie no
        // BAD_REQUEST), but if these strings don't match, we don't really
        // know what to do.
        if (!normalisedResourceName.equals(normalisedDocId)) {
            String message = "ID contained in Resource ("
                    + normalisedResourceName + ") does not match decoded docId ("
                    + normalisedDocId + ") from URL";
            log.error(message);
            throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
        }

        if (copytrans) {
            log.warn("Ignoring copyTrans request. Async-aware clients should call CopyTransResource.startCopyTrans()");
        }

        HProjectIteration hProjectIteration =
                projectUtil.retrieveAndCheckIteration(projectSlug, iterationSlug, true);

        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");

        ResourceUtils.validateExtensions(extensions); // gettext, comment

        if (httpPost) {
            HDocument document =
                    documentDAO.getByDocIdAndIteration(hProjectIteration,
                            normalisedResourceName);
            // already existing non-obsolete document.
            if (document != null) {
                if (!document.isObsolete()) {
                    // updates must happen through PUT on the actual resource
                    ProcessStatus status = new ProcessStatus();
                    status.setStatusCode(ProcessStatusCode.Failed);
                    status.getMessages().add(
                            "A document with name " + normalisedResourceName
                                    + " already exists.");
                    return status;
                }
            }
        }

//        String name = "SourceDocCreation: "+projectSlug+"-"+iterationSlug+"-"+idNoSlash;
        AsyncTaskHandle<HDocument> handle = AsyncTaskHandle.withGeneratedKey(identity.getAccountUsername());
        asyncTaskHandleManager.registerTaskHandle(handle);
        documentServiceImpl.saveDocumentWithLockAsync(projectSlug, iterationSlug,
                resource, extensions, false, handle);

        // TODO Change to return 202 Accepted, with a url to get the progress
        return getProcessStatus(handle.getKey().toString());
    }

    public ProcessStatus startTranslatedDocCreationOrUpdate(
            final String idNoSlash, final String projectSlug,
            final String iterationSlug, final LocaleId locale,
            final TranslationsResource translatedDoc,
            final Set<String> extensions, final String merge,
            final boolean assignCreditToUploader) {
        // check security (cannot be on @Restrict as it refers to method
        // parameters)
        identity.checkPermission("modify-translation", this.localeServiceImpl
                .getByLocaleId(locale),
                this.getSecuredIteration(projectSlug, iterationSlug)
                        .getProject());

        MergeType mergeType;
        try {
            mergeType = MergeType.valueOf(merge.toUpperCase());
        } catch (Exception e) {
            ProcessStatus status = new ProcessStatus();
            status.setStatusCode(ProcessStatusCode.Failed);
            status.getMessages().add("bad merge type " + merge);
            return status;
        }

        final String id = URIHelper.convertFromDocumentURIId(idNoSlash);
        final MergeType finalMergeType = mergeType;

        AsyncTaskHandle<HDocument> handle = AsyncTaskHandle.withGeneratedKey(identity.getAccountUsername());
        asyncTaskHandleManager.registerTaskHandle(handle);
        translationServiceImpl.translateAllInDocWithLockAsync(projectSlug,
                iterationSlug, id, locale, translatedDoc, extensions,
                finalMergeType, assignCreditToUploader, handle,
                TranslationSourceType.API_UPLOAD);

        return this.getProcessStatus(handle.getKey().toString());
    }

    /**
     * @see JobStatusService#getJobStatus(String)
     * @param processId
     *            The process Id (as returned by one of the endpoints that
     *            starts an async process).
     * @return
     */
    @Override
    public ProcessStatus getProcessStatus(String processId) {
        AsyncTaskHandle<?> handle =
                asyncTaskHandleManager.getHandleByKey(processId);

        if (handle == null) {
            throw new NotFoundException("A process was not found for id "
                    + processId);
        }

        // FIXME check current user against project users (or admin)

        ProcessStatus status = new ProcessStatus();
        status.setStatusCode(handle.isDone() ? ProcessStatusCode.Finished
                : ProcessStatusCode.Running);
        status.setPercentageComplete((int) handle.getPercentComplete());
        status.setUrl("" + processId);

        if (handle.isDone()) {
            Object result = null;
            try {
                result = handle.getResult();
            } catch (InterruptedException e) {
                log.debug("async task interrupted", e);
                // The process was forcefully cancelled
                status.setStatusCode(ProcessStatusCode.Failed);
                status.setMessages(Lists.newArrayList(e.getMessage()));
            } catch (ExecutionException e) {
                log.debug("async task failed", e);
                // Exception thrown while running the task
                status.setStatusCode(ProcessStatusCode.Failed);
                status.setMessages(Lists
                        .newArrayList(e.getCause().getMessage()));
            }

            // TODO Need to find a generic way of returning all object types.
            // Since the only current
            // scenario involves lists of strings, hardcoding to that
            if (result != null && result instanceof List) {
                status.getMessages().addAll((List) result);
            }
        }

        return status;
    }

    public HProjectIteration getSecuredIteration(String projectSlug,
            String iterationSlug) {
        return projectUtil.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
    }
}
