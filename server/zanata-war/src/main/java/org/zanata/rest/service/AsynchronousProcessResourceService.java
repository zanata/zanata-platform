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

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.jboss.resteasy.spi.NotFoundException;
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
    private ResourceUtils resourceUtils;
    @Inject
    private ZanataIdentity identity;

    @Override
    public ProcessStatus startSourceDocCreation(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans) {
        HProjectIteration hProjectIteration =
                retrieveAndCheckIteration(projectSlug, iterationSlug, true);
        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");
        resourceUtils.validateExtensions(extensions); // gettext, comment
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
        AsyncTaskHandle<HDocument> handle = new AsyncTaskHandle<HDocument>();
        Serializable taskId = asyncTaskHandleManager.registerTaskHandle(handle);
        documentServiceImpl.saveDocumentAsync(projectSlug, iterationSlug,
                resource, extensions, copytrans, true, handle);
        return getProcessStatus(taskId.toString()); // TODO Change to return 202
        // Accepted,
        // with a url to get the
        // progress
    }

    @Override
    public ProcessStatus startSourceDocCreationOrUpdate(final String idNoSlash,
            final String projectSlug, final String iterationSlug,
            final Resource resource, final Set<String> extensions,
            final boolean copytrans) {
        HProjectIteration hProjectIteration =
                retrieveAndCheckIteration(projectSlug, iterationSlug, true);
        resourceUtils.validateExtensions(extensions); // gettext, comment
        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");
        String name = "SourceDocCreationOrUpdate: " + projectSlug + "-"
                + iterationSlug + "-" + idNoSlash;
        AsyncTaskHandle<HDocument> handle = new AsyncTaskHandle<HDocument>();
        Serializable taskId = asyncTaskHandleManager.registerTaskHandle(handle);
        documentServiceImpl.saveDocumentAsync(projectSlug, iterationSlug,
                resource, extensions, copytrans, true, handle);
        return getProcessStatus(taskId.toString()); // TODO Change to return 202
        // Accepted,
        // with a url to get the
        // progress
    }

    /**
     * @param idNoSlash
     * @param projectSlug
     * @param iterationSlug
     * @param locale
     * @param translatedDoc
     * @param extensions
     * @param merge
     * @param assignCreditToUploader
     * @return
     */
    @Override
    public ProcessStatus startTranslatedDocCreationOrUpdate(
            final String idNoSlash, final String projectSlug,
            final String iterationSlug, final LocaleId locale,
            final TranslationsResource translatedDoc,
            final Set<String> extensions, final String merge,
            final boolean assignCreditToUploader) {
        // check security (cannot be on @Restrict as it refers to method
        // parameters)
        identity.checkPermission("modify-translation",
                this.localeServiceImpl.getByLocaleId(locale),
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
        AsyncTaskHandle<HDocument> handle = new AsyncTaskHandle<HDocument>();
        Serializable taskId = asyncTaskHandleManager.registerTaskHandle(handle);
        translationServiceImpl.translateAllInDocAsync(projectSlug,
                iterationSlug, id, locale, translatedDoc, extensions,
                finalMergeType, assignCreditToUploader, true, handle,
                TranslationSourceType.API_UPLOAD);
        return this.getProcessStatus(taskId.toString());
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) {
        AsyncTaskHandle handle =
                asyncTaskHandleManager.getHandleByKey(processId);
        if (handle == null) {
            throw new NotFoundException(
                    "A process was not found for id " + processId);
        }
        ProcessStatus status = new ProcessStatus();
        status.setStatusCode(handle.isDone() ? ProcessStatusCode.Finished
                : ProcessStatusCode.Running);
        int perComplete = 100;
        if (handle.getMaxProgress() > 0) {
            perComplete = (handle.getCurrentProgress() * 100
                    / handle.getMaxProgress());
        }
        status.setPercentageComplete(perComplete);
        status.setUrl("" + processId);
        if (handle.isDone()) {
            Object result = null;
            try {
                result = handle.getResult();
            } catch (InterruptedException e) {
                // The process was forcefully cancelled
                status.setStatusCode(ProcessStatusCode.Failed);
                status.setMessages(Lists.newArrayList(e.getMessage()));
            } catch (ExecutionException e) {
                // Exception thrown while running the task
                status.setStatusCode(ProcessStatusCode.Failed);
                status.setMessages(
                        Lists.newArrayList(e.getCause().getMessage()));
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
