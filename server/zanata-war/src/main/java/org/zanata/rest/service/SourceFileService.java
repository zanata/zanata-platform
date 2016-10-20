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

import static org.zanata.rest.service.FileResource.FILETYPE_POT;
import static org.zanata.rest.service.FileResource.FILETYPE_RAW_SOURCE_DOCUMENT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.RawDocumentContentAccessException;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.resource.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Slf4j
@Transactional
public class SourceFileService implements SourceFileResource {

    /**
     * See also {@link TranslatedFileResourceService#FILE_TYPE_OFFLINE_PO}
     */
    private static final String FILE_TYPE_OFFLINE_PO_TEMPLATE = "offlinepot";

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private LegacyFileMapper legacyFileMapper;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private SourceDocumentUpload sourceUploader;

    @Override
    public Response uploadSourceFile(
            String projectSlug,
            String iterationSlug,
            String docId,
            InputStream fileStream,
            @Nullable ProjectType projectType) {
        String suffix = legacyFileMapper.getFilenameSuffix(projectSlug, iterationSlug, projectType, true);
        String actualDocId = StringUtils.removeEnd(docId, suffix);
//        String actualDocId = legacyFileMapper.getServerDocId(projectSlug, iterationSlug, docId, projectType);
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, actualDocId);
        // FIXME
        return null;
//        return sourceUploader.tryUploadSourceFile(id, fileStream);
    }


    @Override
    public Response downloadSourceFile(
            String projectSlug,
            String iterationSlug,
            String fileType,
            String docId,
            String projectType) {
        // TODO scan (again) for virus
        HDocument document =
                documentDAO.getByProjectIterationAndDocId(projectSlug,
                        iterationSlug, docId);
        if (document == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (FILETYPE_RAW_SOURCE_DOCUMENT.equals(fileType)) {
            if (document.getRawDocument() == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            InputStream fileContents;
            try {
                fileContents =
                        filePersistService
                                .getRawDocumentContentAsStream(document
                                        .getRawDocument());
            } catch (RawDocumentContentAccessException e) {
                log.error(e.toString(), e);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
                        .build();
            }
            StreamingOutput output =
                    new InputStreamStreamingOutput(fileContents);
            return Response
                    .ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"" + document.getName()
                                    + "\"").entity(output).build();
        } else if (FILETYPE_POT.equals(fileType)
                || FILE_TYPE_OFFLINE_PO_TEMPLATE.equals(fileType)) {
            // Note: could give 404 or unsupported media type for "pot" in
            // non-po projects,
            // and suggest using offlinepo
            Resource res = resourceUtils.buildResource(document);
            StreamingOutput output =
                    new POTStreamingOutput(res,
                            FILE_TYPE_OFFLINE_PO_TEMPLATE.equals(fileType));
            return Response
                    .ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"" + document.getName()
                                    + ".pot\"").type(MediaType.TEXT_PLAIN)
                    .entity(output).build();
        } else {
            return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
    }

    private class InputStreamStreamingOutput implements StreamingOutput {
        private InputStream input;

        InputStreamStreamingOutput(InputStream input) {
            this.input = input;
        }

        @Override
        public void write(OutputStream output) throws IOException,
                WebApplicationException {
            byte[] buffer = new byte[4096]; // To hold file contents
            int bytesRead; // How many bytes in buffer

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    private class POTStreamingOutput implements StreamingOutput {
        private Resource resource;
        private boolean offlinePot;

        /**
         * @param offlinePot
         *            true if text flow id should be inserted into msgctxt to
         *            allow reverse mapping
         */
        POTStreamingOutput(Resource resource, boolean offlinePot) {
            this.resource = resource;
            this.offlinePot = offlinePot;
        }

        @Override
        public void write(OutputStream output) throws IOException,
                WebApplicationException {
            PoWriter2 writer = new PoWriter2(false, offlinePot);
            writer.writePot(output, "UTF-8", resource);
        }
    }

}
