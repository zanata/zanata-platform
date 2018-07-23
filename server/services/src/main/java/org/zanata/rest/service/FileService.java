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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.FileFormatAdapter.ParserOptions;
import org.zanata.adapter.FileFormatAdapter.WriterOptions;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.*;
import org.zanata.common.dto.TranslatedDoc;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.RawDocumentContentAccessException;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.dto.TranslationSourceType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.FileSystemService;
import org.zanata.service.FileSystemService.DownloadDescriptorProperties;
import org.zanata.service.TranslationFileService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RequestScoped
@Named("fileService")
@Path(FileResource.SERVICE_PATH)
@Transactional
public class FileService implements FileResource {
    private static final Logger log =
            LoggerFactory.getLogger(FileService.class);

    // not sure what the use case is here:
    private static final String FILETYPE_OFFLINE_PO_TEMPLATE = "offlinepot";

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private TranslatedDocResourceService translatedDocResourceService;
    @Inject
    private FileSystemService fileSystemServiceImpl;
    @Inject
    private TranslationFileService translationFileServiceImpl;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private VirusScanner virusScanner;
    @Inject
    private SourceDocumentUpload sourceUploader;
    @Inject
    private TranslationDocumentUpload translationUploader;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private ZanataIdentity identity;

    /**
     * Deprecated.
     *
     * @see #fileTypeInfoList
     */
    @Override
    @Deprecated
    public Response acceptedFileTypes() {
        StringSet acceptedTypes = new StringSet("");
        acceptedTypes
                .addAll(translationFileServiceImpl.getSupportedExtensions());
        return Response.ok(acceptedTypes.toString()).build();
    }

    /**
     * Deprecated.
     *
     * @see #fileTypeInfoList
     */
    @Override
    @Deprecated
    public Response acceptedFileTypeList() {
        Object entity = new GenericEntity<List<DocumentType>>(
                Lists.newArrayList(
                        translationFileServiceImpl
                                .getSupportedDocumentTypes())) {
        };
        return Response.ok(entity).build();
    }

    @Override
    public Response fileTypeInfoList() {
        Set<DocumentType> supportedDocumentTypes =
                translationFileServiceImpl.getSupportedDocumentTypes();
        List<FileTypeInfo> docTypes = supportedDocumentTypes.stream()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .map(DocumentType::toFileTypeInfo).collect(Collectors.toList());
        Object entity = new GenericEntity<List<FileTypeInfo>>(docTypes){};
        return Response.ok(entity).build();
    }

    @Override
    public Response uploadSourceFile(String projectSlug, String iterationSlug,
            String docId, DocumentFileUploadForm uploadForm) {
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, docId);
        return sourceUploader.tryUploadSourceFile(id, uploadForm);
    }

    @Override
    public Response uploadTranslationFile(String projectSlug,
            String iterationSlug, String localeId, String docId, String merge,
            DocumentFileUploadForm uploadForm) {
        // assignCreditToUploader is not supported from here
        boolean assignCreditToUploader = false;
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, docId);
        return translationUploader.tryUploadTranslationFile(id, localeId, merge,
                assignCreditToUploader, uploadForm,
                TranslationSourceType.API_UPLOAD);
    }

    @Override
    @SuppressFBWarnings({"SLF4J_FORMAT_SHOULD_BE_CONST"})
    public Response downloadSourceFile(String projectSlug, String iterationSlug,
            String fileType, String docId) {
        if (!hasProjectVersionAccess(projectSlug, iterationSlug)) {
            return Response.status(Status.NOT_FOUND).build();
        }
        // TODO scan (again) for virus
        HDocument document = documentDAO.getByProjectIterationAndDocId(
                projectSlug, iterationSlug, docId);
        if (document == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (FILETYPE_RAW_SOURCE_DOCUMENT.equals(fileType)) {
            if (document.getRawDocument() == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            InputStream fileContents;
            try {
                fileContents = filePersistService.getRawDocumentContentAsStream(
                        document.getRawDocument());
            } catch (RawDocumentContentAccessException e) {
                log.error(e.toString(), e);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
                        .build();
            }
            StreamingOutput output =
                    new InputStreamStreamingOutput(fileContents);
            return Response.ok().header("Content-Disposition",
                    "attachment; filename=\"" + document.getName() + "\"")
                    .entity(output).build();
        } else if (FILETYPE_GETTEXT_TEMPLATE.equals(fileType)
                || FILETYPE_OFFLINE_PO_TEMPLATE.equals(fileType)) {
            // Note: could give 404 or unsupported media type for "pot" in
            // non-po projects,
            // and suggest using offlinepo
            Resource res = resourceUtils.buildResource(document);
            StreamingOutput output = new POTStreamingOutput(res,
                    FILETYPE_OFFLINE_PO_TEMPLATE.equals(fileType));
            return Response.ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"" + document.getName()
                                    + ".pot\"")
                    .type(MediaType.TEXT_PLAIN).entity(output).build();
        } else {
            return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
    }

    @Override
    @SuppressFBWarnings({"SLF4J_FORMAT_SHOULD_BE_CONST"})
    public Response downloadTranslationFile(String projectSlug,
            String iterationSlug, String locale, String fileType,
            String docId, boolean approvedOnly) {
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, docId);
        // TODO scan (again) for virus
        final Response response;
        HDocument document = this.documentDAO.getByProjectIterationAndDocId(
                projectSlug, iterationSlug, docId);
        if (document == null) {
            response = Response.status(Status.NOT_FOUND).build();
        } else {
            LocaleId localeId = new LocaleId(locale);
            if (FILETYPE_GETTEXT.equals(fileType)
                    || FILETYPE_OFFLINE_PO.equals(fileType)) {
                // Note: could return 404 or Unsupported media type for "po" in
                // non-po projects,
                // and suggest to use offlinepo
                final Set<String> extensions = new HashSet<String>();
                extensions.add("gettext");
                extensions.add("comment");
                // Perform translation of Hibernate DTOs to JAXB DTOs

                // FIXME convertFromDocumentURIId expects an idNoSlash, but what type is docId?
                String convertedId = RestUtil.convertFromDocumentURIId(docId);
                TranslationsResource transRes =
                        (TranslationsResource) this.translatedDocResourceService
                                .getTranslationsWithDocId(localeId, convertedId,
                                        extensions, true, false, null)
                                .getEntity();
                Resource res = this.resourceUtils.buildResource(document);
                StreamingOutput output = new POStreamingOutput(res, transRes,
                        FILETYPE_OFFLINE_PO.equals(fileType), approvedOnly);
                response = Response.ok()
                        .header("Content-Disposition",
                                "attachment; filename=\"" + document.getName()
                                        + ".po\"")
                        .type(MediaType.TEXT_PLAIN).entity(output).build();
            } else if (FILETYPE_TRANSLATED_APPROVED.equals(fileType)
                    || FILETYPE_TRANSLATED_APPROVED_AND_FUZZY.equals(fileType)) {
                if (!filePersistService.hasPersistedDocument(id)) {
                    return Response.status(Status.NOT_FOUND).build();
                }
                assert document.getRawDocument() != null;
                HRawDocument hRawDocument = document.getRawDocument();
                Resource res = this.resourceUtils.buildResource(document);
                final Set<String> extensions = Collections.<String> emptySet();
                // FIXME convertFromDocumentURIId expects an idNoSlash, but what type is docId?
                String convertedId = RestUtil.convertFromDocumentURIId(docId);
                TranslationsResource transRes =
                        (TranslationsResource) this.translatedDocResourceService
                                .getTranslationsWithDocId(localeId, convertedId,
                                        extensions, true, false, null)
                                .getEntity();
                // Filter to only provide translated targets. "Preview" downloads
                // include fuzzy.
                // New list is used as transRes list appears not to be a modifiable
                // implementation.
                List<TextFlowTarget> filteredTranslations = Lists.newArrayList();
                boolean useFuzzy =
                        FILETYPE_TRANSLATED_APPROVED_AND_FUZZY.equals(fileType);
                for (TextFlowTarget target : transRes.getTextFlowTargets()) {
                    // TODO rhbz953734 - translatedDocResourceService will map
                    // review content state to old state. For now this is
                    // acceptable. Once we have new REST options, we should review
                    // this
                    ContentState state = target.getState();
                    if (state.isApproved() ||
                            (useFuzzy && state.isRejectedOrFuzzy()) ||
                            (!approvedOnly && state.isTranslated())) {
                        filteredTranslations.add(target);
                    }
                }
                transRes.getTextFlowTargets().clear();
                transRes.getTextFlowTargets().addAll(filteredTranslations);
                InputStream inputStream;
                try {
                    inputStream = filePersistService.getRawDocumentContentAsStream(
                            hRawDocument);
                } catch (RawDocumentContentAccessException e) {
                    log.error(e.toString(), e);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
                            .build();
                }
                File tempFile =
                        translationFileServiceImpl.persistToTempFile(inputStream);
                String name = projectSlug + ":" + iterationSlug + ":" + docId;
                // TODO damason: this file is not transmitted, but used to generate
                // a file later
                // the generated file should be scanned instead
                virusScanner.scan(tempFile, name);
                URI uri = tempFile.toURI();
                FileFormatAdapter adapter = translationFileServiceImpl
                        .getAdapterFor(hRawDocument.getType());
                String rawParamString = hRawDocument.getAdapterParameters();
                String params = Strings.nullToEmpty(rawParamString);
                StreamingOutput output = new FormatAdapterStreamingOutput(uri, res,
                        transRes, localeId, adapter, params, approvedOnly);
                String translationFilename =
                        adapter.generateTranslationFilename(document, locale);
                response = Response.ok()
                        .header("Content-Disposition", "attachment; filename=\""
                                + translationFilename + "\"")
                        .entity(output).build();
                // TODO damason: remove more immediately, but make sure response has
                // finished with the file
                // Note: may not be necessary when file storage is on disk.
                tempFile.deleteOnExit();
            } else {
                // TODO wrong code: fileType is not a mime media type
                response = Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
            }
        }
        return response;
    }

    @Override
    public Response download(String downloadId) {
        // TODO scan (again) for virus
        try {
            // Check that the download exists by looking at the download
            // descriptor
            Properties descriptorProps = this.fileSystemServiceImpl
                    .findDownloadDescriptorProperties(downloadId);
            if (descriptorProps == null) {
                return Response.status(Status.NOT_FOUND).build();
            } else {
                File toDownload =
                        this.fileSystemServiceImpl.findDownloadFile(downloadId);
                if (toDownload == null) {
                    return Response.status(Status.NOT_FOUND).build();
                } else {
                    return Response.ok()
                            .header("Content-Disposition",
                                    "attachment; filename=\""
                                            + descriptorProps.getProperty(
                                                    DownloadDescriptorProperties.DownloadFileName
                                                            .toString())
                                            + "\"")
                            .header("Content-Length", toDownload.length())
                            .entity(new FileStreamingOutput(toDownload))
                            .build();
                }
            }
        } catch (IOException e) {
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
    /*
     * Private class that implements PO file streaming of a document.
     */

    private static class POStreamingOutput implements StreamingOutput {
        private Resource resource;
        private TranslationsResource transRes;
        private boolean offlinePo;
        private boolean approvedOnly;

        /**
         * @param offlinePo
         *            true if text flow id should be inserted into msgctxt to
         *            allow reverse mapping.
         * @param approvedOnly
         */
        public POStreamingOutput(Resource resource,
                TranslationsResource transRes, boolean offlinePo,
                boolean approvedOnly) {
            this.resource = resource;
            this.transRes = transRes;
            this.offlinePo = offlinePo;
            this.approvedOnly = approvedOnly;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            PoWriter2 writer =
                    new PoWriter2.Builder().mapIdToMsgctxt(offlinePo)
                            .approvedOnly(approvedOnly).create();
            writer.writePo(output, "UTF-8", this.resource, this.transRes);
        }
    }

    private static class POTStreamingOutput implements StreamingOutput {
        private Resource resource;
        private boolean offlinePot;

        /**
         * @param offlinePot
         *            true if text flow id should be inserted into msgctxt to
         *            allow reverse mapping
         */
        public POTStreamingOutput(Resource resource, boolean offlinePot) {
            this.resource = resource;
            this.offlinePot = offlinePot;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            PoWriter2 writer =
                    new PoWriter2.Builder().mapIdToMsgctxt(offlinePot)
                            .create();
            writer.writePot(output, "UTF-8", resource);
        }
    }

    private static class InputStreamStreamingOutput implements StreamingOutput {
        private InputStream input;

        public InputStreamStreamingOutput(InputStream input) {
            this.input = input;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            byte[] buffer = new byte[4096]; // To hold file contents
            int bytesRead; // How many bytes in buffer
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    private static class FormatAdapterStreamingOutput implements StreamingOutput {
        private Resource resource;
        private TranslationsResource translationsResource;
        private LocaleId locale;
        private URI original;
        private FileFormatAdapter adapter;
        private String params;
        private final boolean approvedOnly;

        FormatAdapterStreamingOutput(URI originalDoc, Resource resource,
                TranslationsResource translationsResource, LocaleId locale,
                FileFormatAdapter adapter, String params,
                boolean approvedOnly) {
            this.resource = resource;
            this.translationsResource = translationsResource;
            this.locale = locale;
            this.original = originalDoc;
            this.adapter = adapter;
            this.params = params;
            this.approvedOnly = approvedOnly;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            // FIXME should the generated file be virus scanned?
            adapter.writeTranslatedFile(output,
                    new WriterOptions(
                            new ParserOptions(original, locale, params),
                            new TranslatedDoc(resource, translationsResource, locale)),
                    approvedOnly);
        }
    }
    /*
     * Private class that implements downloading from a previously prepared
     * file.
     */

    private static class FileStreamingOutput implements StreamingOutput {
        private File file;

        public FileStreamingOutput(File file) {
            this.file = file;
        }

        @Override
        public void write(@Nonnull OutputStream output)
                throws IOException, WebApplicationException {
            try (FileInputStream input = new FileInputStream(this.file)) {
                IOUtils.copy(input, output);
            }
        }
    }

    private boolean hasProjectVersionAccess(@NotNull String projectSlug,
            @NotNull String versionSlug) {
        HProjectIteration version =
                projectIterationDAO.getBySlug(projectSlug, versionSlug);
        return version != null && identity.hasPermission(version, "read");
    }
}
