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

import static java.util.Optional.ofNullable;
import static com.google.common.base.Strings.emptyToNull;
import static org.zanata.file.DocumentUploadUtil.getInputStream;
import static org.zanata.file.DocumentUploadUtil.isSinglePart;
import static org.zanata.util.FileUtil.tryDeleteFile;
import static org.zanata.util.JavaslangNext.TODO;
import static org.zanata.util.Optionals.optional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import javax.inject.Inject;
import javax.inject.Named;

import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.FileTypeName;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.DocumentUploadDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.DocumentUploadException;
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
import org.zanata.rest.service.AsynchronousProcessResourceService;
import org.zanata.rest.service.VirusScanner;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;

import org.zanata.util.FileUtil;

//TODO damason: add thorough unit testing
// This warning is probably a good idea, but it's making the code hard to read:
@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "Guava" })
@Slf4j
@Dependent
@Named("sourceDocumentUploader")
public class SourceDocumentUpload {

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


    /**
     * The tmpFile will be deleted unless an exception is thrown.
     * @param id
     * @param lang
     * @param tmpFile
     * @param fileTypeName
     * @param handle
     * @return
     */
    @Async
    public @Nonnull AsyncTaskResult<HDocument> processUploadedTempFile(
            GlobalDocumentId id, LocaleId lang, File tmpFile,
            FileTypeName fileTypeName, AsyncTaskHandle<HDocument> handle) {

        // see the following:
        /*
            tryUploadSourceFile: uses failIfSourceUploadNotValid, DocumentUploadUtil.failIfUploadNotValid
            ** then tryValidatedUploadSourceFile **

            also AsynchronousProcessResourceService.startSourceDocCreationOrUpdate
         */
        if (false) {
            tryUploadSourceFile(null, (DocumentFileUploadForm) null, null);
            tryValidatedUploadSourceFile(null, null, null);
            // alternatively
            new AsynchronousProcessResourceService().startSourceDocCreationOrUpdate(
                    "", "", "", null, null, false);
        }

        Optional<String> docType = TODO("determine doctype from headers");
//        a) processAdapterFile: uses DocumentServiceImpl.saveDocument and persistRawDocument
        HDocument document = processAdapterFile(tmpFile, id,
                Optional.empty(), docType, Optional.empty(), lang);
//        OR b) parsePotFile: uses DocumentServiceImpl.saveDocument
        tryDeleteFile(tmpFile);
        return AsyncTaskResult.taskResult(document);
    }

    public Response tryUploadSourceFile(GlobalDocumentId id,
            FileTypeName fileType, LocaleId sourceLocale) {
        try {
            failIfSourceUploadNotValid(id, fileType);
        } catch (DocumentUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        }

//        return tryValidatedUploadSourceFile(id, fileType, sourceLocale);
        return TODO();
    }

    public Response tryUploadSourceFileWithoutHash(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm, LocaleId sourceLocale) {
        try {
            failIfSourceUploadNotValid(id, uploadForm);
        } catch (DocumentUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        }

        return tryValidatedUploadSourceFile(id, uploadForm, sourceLocale);
    }

    public Response tryUploadSourceFile(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm, LocaleId sourceLocale) {
        util.failIfHashNotPresent(uploadForm);
        return tryUploadSourceFileWithoutHash(id, uploadForm, sourceLocale);
    }

    public Response tryValidatedUploadSourceFile(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm, LocaleId sourceLocale) {
        try {
            Optional<File> tempFile;
            int totalChunks;

            if (!uploadForm.getLast()) {
                HDocumentUpload upload =
                        util.saveUploadPart(id, NULL_LOCALE, uploadForm);
                totalChunks = upload.getParts().size();
                return Response
                        .status(Status.ACCEPTED)
                        .entity(new ChunkUploadResponse(upload.getId(),
                                totalChunks, true,
                                "Chunk accepted, awaiting remaining chunks."))
                        .build();
            }

            if (isSinglePart(uploadForm)) {
                totalChunks = 1;
                tempFile = Optional.<File> empty();
            } else {
                HDocumentUpload previousParts =
                        documentUploadDAO.findById(uploadForm.getUploadId());
                totalChunks = previousParts.getParts().size();
                totalChunks++; // add final part
                // FIXME ensure tmpFile is deleted in all cases (async or not)
                tempFile =
                        Optional.of(util
                                .combineToTempFileAndDeleteUploadRecord(
                                        previousParts,
                                        uploadForm));
            }

            HProjectIteration version =
                projectIterationDAO.getBySlug(id.getProjectSlug(), id.getVersionSlug());

            if (version == null) {
                throw new ZanataServiceException("Project version not found: "
                    + id.getProjectSlug() + " " + id.getVersionSlug());
            }

            if (version.getProjectType() == ProjectType.File) {
                if (!tempFile.isPresent()) {
                    tempFile = Optional.of(util
                            .persistTempFileFromUpload(uploadForm));
                }
                assert tempFile.isPresent();
                processAdapterFile(tempFile.get(), id, uploadForm,
                        sourceLocale);
            } else if (DocumentType.getByName(uploadForm.getFileType()) == DocumentType.GETTEXT) {
                try (InputStream potStream = getInputStream(tempFile,
                        uploadForm)) {
                    parsePotFile(potStream, id);
                }
            } else {
                throw new ZanataServiceException("Unsupported source file: "
                    + id.getDocId());
            }

            tempFile.ifPresent(File::delete);
            return sourceUploadSuccessResponse(util.isNewDocument(id),
                    totalChunks);
        } catch (DocumentUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        } catch (IOException e) {
            log.error("error using input stream from temp file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        }
    }

    private void failIfSourceUploadNotValid(GlobalDocumentId id,
            DocumentFileUploadForm uploadForm) throws DocumentUploadException {
        util.failIfUploadNotValid(id, uploadForm);
        failIfSourceUploadNotAllowed(id);
        failIfFileTypeNotValid(uploadForm.getFileType());
    }

    private void failIfSourceUploadNotValid(GlobalDocumentId id,
            FileTypeName fileType) throws DocumentUploadException {
        util.failIfUploadNotValid(id, fileType.getName());
        failIfSourceUploadNotAllowed(id);
        failIfFileTypeNotValid(fileType.getName());
    }

    public void failIfSourceUploadNotAllowed(GlobalDocumentId id)
            throws DocumentUploadException {
        if (!isDocumentUploadAllowed(id)) {
            throw new DocumentUploadException(Status.FORBIDDEN,
                    "You do not have permission to upload source documents to project-version \""
                            + id.getProjectSlug() + ":" + id.getVersionSlug()
                            + "\".");
        }
    }

    private boolean isDocumentUploadAllowed(GlobalDocumentId id) {
        HProjectIteration projectIteration =
                projectIterationDAO.getBySlug(id.getProjectSlug(),
                        id.getVersionSlug());
        return projectIteration.getStatus() == EntityStatus.ACTIVE
                && projectIteration.getProject().getStatus() == EntityStatus.ACTIVE
                && identity != null
                && identity.hasPermissionWithAnyTargets("import-template",
                projectIteration);
    }

    private void failIfFileTypeNotValid(String fileType)
            throws DocumentUploadException {
        DocumentType type = DocumentType.getByName(fileType);
        if (!isSourceDocumentType(type)) {
            throw new DocumentUploadException(Status.BAD_REQUEST, "The type \""
                    + fileType
                    + "is not valid for a source file on this server.");
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
            uploadResponse
                    .setSuccessMessage("Upload of new source document successful.");
            response =
                    Response.status(Status.CREATED).entity(uploadResponse)
                            .build();
        } else {
            uploadResponse
                    .setSuccessMessage("Upload of new version of source document successful.");
            response =
                    Response.status(Status.OK).entity(uploadResponse).build();
        }
        return response;
    }

    private void processAdapterFile(@Nonnull File tempFile,
            GlobalDocumentId id, DocumentFileUploadForm uploadForm,
            LocaleId sourceLocale) {
        Optional<String> params =
                ofNullable(emptyToNull(uploadForm.getAdapterParams()));
        Optional<String> fileTypeName = ofNullable(uploadForm.getFileType());
        Optional<String> contentHash = ofNullable(uploadForm.getHash());
        processAdapterFile(tempFile, id,
                params, fileTypeName, contentHash, sourceLocale);
    }

    public HDocument processAdapterFile(@Nonnull File tempFile,
            GlobalDocumentId id, Optional<String> requestedParams,
            Optional<String> fileTypeName, Optional<String> contentHash,
            LocaleId sourceLocale) {
        String name =
                id.getProjectSlug() + ":" + id.getVersionSlug() + ":"
                        + id.getDocId();
        try {
            virusScanner.scan(tempFile, name);
        } catch (VirusDetectedException e) {
            log.warn("File failed virus scan: {}", e.getMessage());
            throw new DocumentUploadException(Status.BAD_REQUEST,
                    "Uploaded file did not pass virus scan");
        }

        Optional<String> adapterParams =
                requestedParams.isPresent() ? requestedParams
                        : documentDAO.getAdapterParams(id.getProjectSlug(),
                                id.getVersionSlug(), id.getDocId());

        try {
            Resource doc =
                    translationFileServiceImpl.parseUpdatedAdapterDocumentFile(
                            tempFile.toURI(), id.getDocId(),
                            // FIXME is this fileTypeName, or is it uploadFileName??
                            fileTypeName.orElse(null), adapterParams, fileTypeName,
                            sourceLocale);
            // Copy Trans should be invoked by client separately after source/translation upload
            HDocument document =
                    documentServiceImpl.saveDocument(id.getProjectSlug(),
                            id.getVersionSlug(), doc,
                            Sets.newHashSet(PotEntryHeader.ID, SimpleComment.ID), false);

            DocumentType documentType =
                    DocumentType.getByName(fileTypeName.orElse(null));

            persistRawDocument(document, tempFile, contentHash, documentType,
                    adapterParams);

            // FIXME use a finally
            FileUtil.tryDeleteFile(tempFile);

            return document;
        } catch (SecurityException | ZanataServiceException e) {
            throw new DocumentUploadException(Status.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e);
        }
    }

    private void persistRawDocument(HDocument document, File rawFile,
            Optional<String> contentHash, DocumentType documentType,
            Optional<String> params) {
        HRawDocument rawDocument = new HRawDocument();
        rawDocument.setDocument(document);
        rawDocument.setContentHash(contentHash.orElse(null));
        rawDocument.setFileSize(rawFile.length());
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
     * @param potStream
     * @param id
     */
    private void parsePotFile(InputStream potStream, GlobalDocumentId id) {
        //remove .pot extension from docId as per zanata-cli
        String docIdWithoutExtension =
            FilenameUtils.removeExtension(id.getDocId());

        Resource doc = translationFileServiceImpl.parseUpdatedPotFile(potStream,
                docIdWithoutExtension, ".pot",
                useOfflinePo(id));
        doc.setLang(LocaleId.EN_US);
        // TODO Copy Trans values

        StringSet extensions = new StringSet(ExtensionType.GetText.toString());
        extensions.add(SimpleComment.ID);

        documentServiceImpl.saveDocument(id.getProjectSlug(),
                id.getVersionSlug(), doc, extensions, false);
    }

    private boolean useOfflinePo(GlobalDocumentId id) {
        return !util.isNewDocument(id)
                && !translationFileServiceImpl
                        .isPoDocument(id.getProjectSlug(), id.getVersionSlug(),
                                id.getDocId());
    }

}
