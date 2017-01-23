/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.file;

import static org.zanata.file.DocumentUploadUtil.getInputStream;
import static org.zanata.file.DocumentUploadUtil.isSinglePart;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.google.common.collect.Sets;
import org.apache.commons.io.FilenameUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.DocumentUploadDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.exception.VirusDetectedException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.VirusScanner;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
//TODO damason: add thorough unit testing

@Dependent
@Named("sourceDocumentUploader")
public class SourceDocumentUpload {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SourceDocumentUpload.class);

    private static final HLocale NULL_LOCALE = null;
    @Inject
    private DocumentUploadUtil util;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private TranslationFileService translationFileServiceImpl;
    @Inject
    private VirusScanner virusScanner;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private DocumentUploadDAO documentUploadDAO;
    @Inject
    private DocumentService documentServiceImpl;

    public Response tryUploadSourceFileWithoutHash(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) {
        try {
            failIfSourceUploadNotValid(id, uploadForm);
        } catch (ChunkUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        }
        return tryValidatedUploadSourceFile(id, uploadForm);
    }

    public Response tryUploadSourceFile(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) {
        try {
            failIfSourceUploadNotValid(id, uploadForm);
            util.failIfHashNotPresent(uploadForm);
        } catch (ChunkUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        }
        return tryValidatedUploadSourceFile(id, uploadForm);
    }

    public Response tryValidatedUploadSourceFile(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) {
        try {
            Optional<File> tempFile;
            int totalChunks;
            if (!uploadForm.getLast()) {
                HDocumentUpload upload =
                        util.saveUploadPart(id, NULL_LOCALE, uploadForm);
                totalChunks = upload.getParts().size();
                return Response.status(Status.ACCEPTED)
                        .entity(new ChunkUploadResponse(upload.getId(),
                                totalChunks, true,
                                "Chunk accepted, awaiting remaining chunks."))
                        .build();
            }
            if (isSinglePart(uploadForm)) {
                totalChunks = 1;
                tempFile = Optional.<File> absent();
            } else {
                HDocumentUpload previousParts =
                        documentUploadDAO.findById(uploadForm.getUploadId());
                totalChunks = previousParts.getParts().size();
                totalChunks++; // add final part
                tempFile =
                        Optional.of(util.combineToTempFileAndDeleteUploadRecord(
                                previousParts, uploadForm));
            }
            HProjectIteration version = projectIterationDAO
                    .getBySlug(id.getProjectSlug(), id.getVersionSlug());
            if (version == null) {
                throw new ZanataServiceException("Project version not found: "
                        + id.getProjectSlug() + " " + id.getVersionSlug());
            }
            if (version.getProjectType() == ProjectType.File) {
                if (!tempFile.isPresent()) {
                    tempFile = Optional
                            .of(util.persistTempFileFromUpload(uploadForm));
                }
                processAdapterFile(tempFile.get(), id, uploadForm);
            } else if (DocumentType.getByName(
                    uploadForm.getFileType()) == DocumentType.GETTEXT) {
                InputStream potStream = getInputStream(tempFile, uploadForm);
                parsePotFile(potStream, id);
            } else {
                throw new ZanataServiceException(
                        "Unsupported source file: " + id.getDocId());
            }
            if (tempFile.isPresent()) {
                tempFile.get().delete();
            }
            return sourceUploadSuccessResponse(util.isNewDocument(id),
                    totalChunks);
        } catch (ChunkUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        } catch (FileNotFoundException e) {
            log.error("failed to create input stream from temp file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        }
    }

    private void failIfSourceUploadNotValid(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) throws ChunkUploadException {
        util.failIfUploadNotValid(id, uploadForm);
        failIfSourceUploadNotAllowed(id);
        failIfFileTypeNotValid(uploadForm);
    }

    private void failIfSourceUploadNotAllowed(GlobalDocumentId id)
            throws ChunkUploadException {
        if (!isDocumentUploadAllowed(id)) {
            throw new ChunkUploadException(Status.FORBIDDEN,
                    "You do not have permission to upload source documents to project-version \""
                            + id.getProjectSlug() + ":" + id.getVersionSlug()
                            + "\".");
        }
    }

    private boolean isDocumentUploadAllowed(GlobalDocumentId id) {
        HProjectIteration projectIteration = projectIterationDAO
                .getBySlug(id.getProjectSlug(), id.getVersionSlug());
        return projectIteration.getStatus() == EntityStatus.ACTIVE
                && projectIteration.getProject()
                        .getStatus() == EntityStatus.ACTIVE
                && identity != null && identity.hasPermissionWithAnyTargets(
                        "import-template", projectIteration);
    }

    private void failIfFileTypeNotValid(DocumentFileUploadForm uploadForm)
            throws ChunkUploadException {
        DocumentType type = DocumentType.getByName(uploadForm.getFileType());
        if (!isSourceDocumentType(type)) {
            throw new ChunkUploadException(Status.BAD_REQUEST, "The type \""
                    + uploadForm.getFileType()
                    + "\" specified in form parameter \'type\' is not valid for a source file on this server.");
        }
    }

    private boolean isSourceDocumentType(DocumentType type) {
        return isPotType(type) || isAdapterType(type);
    }

    private boolean isPotType(DocumentType type) {
        return type == DocumentType.GETTEXT;
    }

    private boolean isAdapterType(DocumentType type) {
        return translationFileServiceImpl.hasAdapterFor(type);
    }

    private static Response sourceUploadSuccessResponse(boolean isNewDocument,
            int acceptedChunks) {
        Response response;
        ChunkUploadResponse uploadResponse = new ChunkUploadResponse();
        uploadResponse.setAcceptedChunks(acceptedChunks);
        uploadResponse.setExpectingMore(false);
        if (isNewDocument) {
            uploadResponse.setSuccessMessage(
                    "Upload of new source document successful.");
            response = Response.status(Status.CREATED).entity(uploadResponse)
                    .build();
        } else {
            uploadResponse.setSuccessMessage(
                    "Upload of new version of source document successful.");
            response =
                    Response.status(Status.OK).entity(uploadResponse).build();
        }
        return response;
    }

    private void processAdapterFile(@Nonnull File tempFile, GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) {
        String name = id.getProjectSlug() + ":" + id.getVersionSlug() + ":"
                + id.getDocId();
        try {
            virusScanner.scan(tempFile, name);
        } catch (VirusDetectedException e) {
            log.warn("File failed virus scan: {}", e.getMessage());
            throw new ChunkUploadException(Status.BAD_REQUEST,
                    "Uploaded file did not pass virus scan");
        }
        HDocument document;
        Optional<String> params;
        params = Optional.fromNullable(
                Strings.emptyToNull(uploadForm.getAdapterParams()));
        if (!params.isPresent()) {
            params = documentDAO.getAdapterParams(id.getProjectSlug(),
                    id.getVersionSlug(), id.getDocId());
        }
        try {
            Optional<String> docType =
                    Optional.fromNullable(uploadForm.getFileType());
            Resource doc =
                    translationFileServiceImpl.parseUpdatedAdapterDocumentFile(
                            tempFile.toURI(), id.getDocId(),
                            uploadForm.getFileType(), params, docType);
            doc.setLang(LocaleId.EN_US);
            // TODO Copy Trans values
            document = documentServiceImpl.saveDocument(id.getProjectSlug(),
                    id.getVersionSlug(), doc,
                    Sets.newHashSet(PotEntryHeader.ID, SimpleComment.ID),
                    false);
        } catch (SecurityException e) {
            throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e);
        } catch (ZanataServiceException e) {
            throw new ChunkUploadException(Status.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e);
        }
        String contentHash = uploadForm.getHash();
        DocumentType documentType =
                DocumentType.getByName(uploadForm.getFileType());
        persistRawDocument(document, tempFile, contentHash, documentType,
                params);
        translationFileServiceImpl.removeTempFile(tempFile);
    }

    private void persistRawDocument(HDocument document, File rawFile,
            String contentHash, DocumentType documentType,
            Optional<String> params) {
        HRawDocument rawDocument = new HRawDocument();
        rawDocument.setDocument(document);
        rawDocument.setContentHash(contentHash);
        rawDocument.setType(documentType);
        rawDocument.setUploadedBy(identity.getCredentials().getUsername());
        filePersistService.persistRawDocumentContentFromFile(rawDocument,
                rawFile, FilenameUtils.getExtension(rawFile.getName()));
        if (params.isPresent()) {
            rawDocument.setAdapterParameters(params.get());
        }
        documentDAO.addRawDocument(document, rawDocument);
        documentDAO.flush();
    }

    /**
     * This method should only process gettext project type
     *
     * @param potStream
     * @param id
     */
    private void parsePotFile(InputStream potStream, GlobalDocumentId id) {
        // remove .pot extension from docId as per zanata-cli
        String docIdWithoutExtension =
                FilenameUtils.removeExtension(id.getDocId());
        Resource doc = translationFileServiceImpl.parseUpdatedPotFile(potStream,
                docIdWithoutExtension, ".pot", useOfflinePo(id));
        doc.setLang(LocaleId.EN_US);
        // TODO Copy Trans values
        StringSet extensions = new StringSet(ExtensionType.GetText.toString());
        extensions.add(SimpleComment.ID);
        documentServiceImpl.saveDocument(id.getProjectSlug(),
                id.getVersionSlug(), doc, extensions, false);
    }

    private boolean useOfflinePo(GlobalDocumentId id) {
        return !util.isNewDocument(id)
                && !translationFileServiceImpl.isPoDocument(id.getProjectSlug(),
                        id.getVersionSlug(), id.getDocId());
    }
}
