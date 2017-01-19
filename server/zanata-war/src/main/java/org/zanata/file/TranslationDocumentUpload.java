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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentUploadDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ChunkUploadException;
import org.zanata.model.HDocumentUpload;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
//TODO damason: add thorough unit testing

@Dependent
@Named("translationDocumentUploader")
public class TranslationDocumentUpload {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationDocumentUpload.class);

    @Inject
    private DocumentUploadUtil util;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private TranslationService translationServiceImpl;
    @Inject
    private TranslationFileService translationFileServiceImpl;
    @Inject
    private DocumentUploadDAO documentUploadDAO;

    public Response tryUploadTranslationFile(GlobalDocumentId id,
            String localeId, String mergeType, boolean assignCreditToUploader,
            DocumentFileUploadForm uploadForm,
            TranslationSourceType translationSourceType) {
        try {
            failIfTranslationUploadNotValid(id, localeId, uploadForm);
            HLocale locale = findHLocale(localeId);
            Optional<File> tempFile;
            int totalChunks;
            if (isSinglePart(uploadForm)) {
                totalChunks = 1;
                tempFile = Optional.<File> absent();
            } else {
                if (!uploadForm.getLast()) {
                    HDocumentUpload upload =
                            util.saveUploadPart(id, locale, uploadForm);
                    totalChunks = upload.getParts().size();
                    return Response.status(Status.ACCEPTED)
                            .entity(new ChunkUploadResponse(upload.getId(),
                                    totalChunks, true,
                                    "Chunk accepted, awaiting remaining chunks."))
                            .build();
                } else {
                    HDocumentUpload previousParts = documentUploadDAO
                            .findById(uploadForm.getUploadId());
                    totalChunks = previousParts.getParts().size();
                    totalChunks++; // add final part
                    tempFile = Optional
                            .of(util.combineToTempFileAndDeleteUploadRecord(
                                    previousParts, uploadForm));
                }
            }
            TranslationsResource transRes;
            if (uploadForm.getFileType().equals(".po")) {
                InputStream poStream = getInputStream(tempFile, uploadForm);
                transRes = translationFileServiceImpl.parsePoFile(poStream,
                        id.getProjectSlug(), id.getVersionSlug(),
                        id.getDocId());
            } else {
                if (!tempFile.isPresent()) {
                    tempFile = Optional
                            .of(util.persistTempFileFromUpload(uploadForm));
                }
                // FIXME this is misusing the 'filename' field. the method
                // should probably take a
                // type anyway
                Optional<String> docType =
                        Optional.fromNullable(uploadForm.getFileType());
                transRes =
                        translationFileServiceImpl.parseAdapterTranslationFile(
                                tempFile.get(), id.getProjectSlug(),
                                id.getVersionSlug(), id.getDocId(), localeId,
                                uploadForm.getFileType(), docType);
            }
            if (tempFile.isPresent()) {
                tempFile.get().delete();
            }
            Set<String> extensions =
                    newExtensions(uploadForm.getFileType().equals(".po"));
            // TODO useful error message for failed saving?
            List<String> warnings = translationServiceImpl.translateAllInDoc(
                    id.getProjectSlug(), id.getVersionSlug(), id.getDocId(),
                    locale.getLocaleId(), transRes, extensions,
                    mergeTypeFromString(mergeType), assignCreditToUploader,
                    translationSourceType);
            return transUploadResponse(totalChunks, warnings);
        } catch (FileNotFoundException e) {
            log.error("failed to create input stream from temp file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        } catch (ChunkUploadException e) {
            return Response.status(e.getStatusCode())
                    .entity(new ChunkUploadResponse(e.getMessage())).build();
        }
    }

    private void failIfTranslationUploadNotValid(GlobalDocumentId id,
            String localeId, DocumentFileUploadForm uploadForm)
            throws ChunkUploadException {
        util.failIfUploadNotValid(id, uploadForm);
        util.failIfHashNotPresent(uploadForm);
        failIfDocumentDoesNotExist(id);
        failIfFileTypeNotValid(uploadForm);
        failIfTranslationUploadNotAllowed(id, localeId);
    }

    private void failIfDocumentDoesNotExist(GlobalDocumentId id)
            throws ChunkUploadException {
        if (util.isNewDocument(id)) {
            throw new ChunkUploadException(Status.NOT_FOUND,
                    "No document with id \"" + id.getDocId()
                            + "\" exists in project-version \""
                            + id.getProjectSlug() + ":" + id.getVersionSlug()
                            + "\".");
        }
    }

    private void failIfFileTypeNotValid(DocumentFileUploadForm uploadForm)
            throws ChunkUploadException {
        String fileType = uploadForm.getFileType();
        if (!fileType.equals(".po") && !translationFileServiceImpl
                .hasAdapterFor(DocumentType.getByName(fileType))) {
            throw new ChunkUploadException(Status.BAD_REQUEST, "The type \""
                    + fileType
                    + "\" specified in form parameter \'type\' is not valid for a translation file on this server.");
        }
    }

    private void failIfTranslationUploadNotAllowed(GlobalDocumentId id,
            String localeId) throws ChunkUploadException {
        HLocale locale = findHLocale(localeId);
        if (!isTranslationUploadAllowed(id, locale)) {
            throw new ChunkUploadException(Status.FORBIDDEN,
                    "You do not have permission to upload translations for locale \""
                            + localeId + "\" to project-version \""
                            + id.getProjectSlug() + ":" + id.getVersionSlug()
                            + "\".");
        }
    }

    private HLocale findHLocale(String localeString) {
        LocaleId localeId;
        try {
            localeId = new LocaleId(localeString);
        } catch (IllegalArgumentException e) {
            throw new ChunkUploadException(Status.BAD_REQUEST,
                    "Invalid value for locale", e);
        }
        HLocale locale = localeDAO.findByLocaleId(localeId);
        if (locale == null) {
            throw new ChunkUploadException(Status.NOT_FOUND,
                    "The specified locale \"" + localeString
                            + "\" does not exist on this server.");
        }
        return locale;
    }

    private boolean isTranslationUploadAllowed(GlobalDocumentId id,
            HLocale locale) {
        HProjectIteration projectIteration = projectIterationDAO
                .getBySlug(id.getProjectSlug(), id.getVersionSlug());
        // TODO should this check be "add-translation" or "modify-translation"?
        // They appear to be granted identically at the moment.
        return projectIteration.getStatus() == EntityStatus.ACTIVE
                && projectIteration.getProject()
                        .getStatus() == EntityStatus.ACTIVE
                && identity != null
                && identity.hasPermissionWithAnyTargets("add-translation",
                        projectIteration.getProject(), locale);
    }

    private static Set<String> newExtensions(boolean gettextExtensions) {
        Set<String> extensions;
        if (gettextExtensions) {
            extensions = new StringSet(ExtensionType.GetText.toString());
        } else {
            extensions = Collections.<String> emptySet();
        }
        return extensions;
    }

    private static Response transUploadResponse(int totalChunks,
            List<String> warnings) {
        ChunkUploadResponse response = new ChunkUploadResponse();
        response.setExpectingMore(false);
        response.setAcceptedChunks(totalChunks);
        if (warnings != null && !warnings.isEmpty()) {
            response.setSuccessMessage(buildWarningString(warnings));
        } else {
            response.setSuccessMessage("Translations uploaded successfully");
        }
        return Response.status(Status.OK).entity(response).build();
    }

    private static String buildWarningString(List<String> warnings) {
        return Joiner.on("\n\t").join(
                "Upload succeeded but had the following warnings:", warnings)
                + "\n";
    }

    private static MergeType mergeTypeFromString(String type) {
        if ("import".equals(type)) {
            return MergeType.IMPORT;
        } else {
            return MergeType.AUTO;
        }
    }
}
