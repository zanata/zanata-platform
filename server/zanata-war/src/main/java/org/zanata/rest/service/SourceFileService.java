/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import java.io.InputStream;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;

import lombok.extern.slf4j.Slf4j;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.JobStatus;
import org.zanata.security.ZanataIdentity;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Slf4j
@Transactional
public class SourceFileService implements SourceFileResource {

    // NB we add 20% fudge factor to allow for processing time
    // after final buffer is read from the file stream.
    private static final double FUDGE_FACTOR = 1.2;

    //    @Inject
//    private DocumentDAO documentDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
//    @Inject
//    private FilePersistService filePersistService;
    @Inject
    private LegacyFileMapper legacyFileMapper;
//    @Inject
//    private ResourceUtils resourceUtils;
//    @Inject
//    private SourceDocumentUpload sourceUploader;
    @Inject
    private ProjectUtil projectUtil;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private JobStatusService jobStatusService;
    @Inject
    private AsyncTaskHandleManager asyncTaskHandleManager;

    // POST
    @Override
    public Response uploadSourceFile(
            String projectSlug,
            String versionSlug,
            String clientDocId,
            InputStream fileStream,
            long size,
            @Nullable ProjectType clientProjectType) {
        HProjectIteration hProjectIteration =
                projectUtil.retrieveAndCheckIteration(projectSlug, versionSlug, true);
        // Check permission
        identity.checkPermission(hProjectIteration, "import-template");

        // TODO should we have PUT and POST, where POST rejects existing (non-obsolete) documents?
        // (see AsynchronousProcessResourceService.startSourceDocCreation)

        HProjectIteration iteration =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        if (iteration == null) {
            throw new WebApplicationException("Unknown project or version", 404);
        }
        ProjectType serverProjectType = iteration.resolveProjectType();
        String serverDocId = legacyFileMapper
                .getServerDocId(serverProjectType, clientDocId, clientProjectType);

        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, versionSlug, serverDocId);

        // FIXME
        AsyncTaskHandle<HDocument> handle = AsyncTaskHandle.withGeneratedKey(identity.getAccountUsername());
        handle.setMaxProgress((long) (size * FUDGE_FACTOR));
        asyncTaskHandleManager.registerTaskHandle(handle);

        // see the following:
        // DocumentServiceImpl.saveDocument
        // FileSystemPersistService
        // SourceDocumentUpload

//        documentServiceImpl.saveDocumentAsync(projectSlug, versionSlug,
//                resource, extensions, copytrans, true, handle);
//        return sourceUploader.tryUploadSourceFile(id, fileStream);

        JobStatus jobStatus = jobStatusService.getJobStatus(handle.getKey().toString());
        return Response.status(Response.Status.ACCEPTED).entity(jobStatus).build();
    }

    @Override
    public Response downloadSourceFile(
            String projectSlug,
            String versionSlug,
            String docId,
            String projectType) {
        // FIXME
        return null;
    }

}
