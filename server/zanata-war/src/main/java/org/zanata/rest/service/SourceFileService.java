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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.AsyncTaskResult;
import org.zanata.common.FileTypeName;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.DocumentPersistService;
import org.zanata.file.DocumentUploadUtil;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.SourceDocumentUpload;

import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.JobStatus;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.web.HttpHeaders;

import static java.io.File.createTempFile;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.zanata.async.AsyncUtil.onFailure;
import static org.zanata.util.FileUtil.tryDeleteFile;
import static org.zanata.util.JavaslangNext.TODO;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Transactional
public class SourceFileService implements SourceFileResource {

    public SourceFileService() {
    }

    @PostConstruct
    public void postConstruct() {
    }

    private static final Logger log = LoggerFactory.getLogger(SourceFileService.class);

    // NB we add 20% fudge factor to allow for processing time
    // after final buffer is read from the file stream.
    private static final double FUDGE_FACTOR = 1.2;

    @Inject
    private ProjectIterationDAO projectIterationDAO;
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
    @Inject
    private SourceDocumentUpload sourceUploader;
    @Inject
    private DocumentUploadUtil uploadUtil;
    @Inject
    private DocumentPersistService documentPersistService;

    // PUT
    @Override
    public Response uploadSourceFile(
            String projectSlug,
            String versionSlug,
            String clientDocId,
            LocaleId lang,
            InputStream fileStream,
            @Nullable Long clientSize,
            @Nullable ProjectType clientProjectType) {
        if (clientDocId.startsWith("/")) {
            throw new WebApplicationException("docId must not start with \"/\"", Response.Status.BAD_REQUEST);
        }

        // may throw 404, 403
        HProjectIteration iteration =
                projectUtil.retrieveAndCheckIteration(projectSlug, versionSlug, true);
        // Check permission
        identity.checkPermission(iteration, "import-template");

        ProjectType serverProjectType = iteration.resolveProjectType();
        String serverDocId = legacyFileMapper
                .getServerDocId(serverProjectType, clientDocId, clientProjectType);

        try {
            AsyncTaskResult<HDocument> futureDoc = null;
            File tmpFile = createTempFile("zanata-upload", ".tmp");
            try {
                // stream to tmpFile
                // this could take a while...
                if (clientSize == null) {
                    log.info("streaming uploaded file (unknown size) to temp file {}",
                            tmpFile);
                } else if (clientSize > 10_000) {
                    log.info("streaming uploaded file (expecting {} bytes) to temp file {}", clientSize,
                            tmpFile);
                }
                // else: no log message for files < 10KB
                copyInputStreamToFile(fileStream, tmpFile);
                if (clientSize != null && clientSize != tmpFile.length()) {
                    String msg = String.format(
                            "expected %d bytes but got %d bytes",
                            clientSize, tmpFile.length());
                    log.warn(msg);
                    throw new WebApplicationException(msg, Response.Status.BAD_REQUEST);
                }
                GlobalDocumentId id =
                        new GlobalDocumentId(projectSlug, versionSlug, serverDocId);

                AsyncTaskHandle<HDocument> handle = AsyncTaskHandle.withGeneratedKey(identity.getAccountUsername());
                handle.setMaxProgress((long) (tmpFile.length() * FUDGE_FACTOR));
                asyncTaskHandleManager.registerTaskHandle(handle);

                FileTypeName fileType = TODO("add FileTypeName parameter");
                futureDoc = sourceUploader.processUploadedTempFile(id, lang, tmpFile,
                        fileType, handle);
                // delete tmpFile upon async failure
                onFailure(futureDoc, e -> tryDeleteFile(tmpFile));

                JobStatus jobStatus = jobStatusService.getJobStatus(handle.getKey().toString());
                return Response.status(Response.Status.ACCEPTED).entity(jobStatus).build();
            } finally {
                //noinspection ConstantConditions
                if (futureDoc == null) {
                    // AsyncMethodInterceptor never completed for
                    // sourceUploader.processUploadedTempFile() (due to an
                    // exception) so delete tmpFile now.
                    tryDeleteFile(tmpFile);
                }
            }
        } catch (IOException e) {
            // The RuntimeException will be mapped to a 500 error
            // (internal server error). The I/O exception may be caused by
            // a client disconnect, which means error 500 could be wrong, but in
            // that case, the client won't see the response...
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response downloadSourceFile(String projectSlug, String versionSlug,
            String docId, String projectType) {
        GlobalDocumentId documentId =
                new GlobalDocumentId(projectSlug, versionSlug, docId);
        DocumentPersistService.PersistedDocumentInfo docInfo =
                documentPersistService.getSourceDocumentForStreaming(documentId);
        if (docInfo != null) {
            Response.ResponseBuilder response = Response
                    .ok(new FileService.InputStreamStreamingOutput(
                            docInfo.inputStream))
                    .header("Content-Disposition",
                            HttpHeaders.getContentDisposition(docInfo.basename,
                                    false));
            if (docInfo.size != null) {
                response.header("Content-Length", docInfo.size);
            }
            return response.build();
        } else {
            return Response.status(404).build();
        }
    }

}
