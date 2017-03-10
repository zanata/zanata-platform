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
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.resteasy.util.GenericType;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.DocumentType;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.RawDocumentContentAccessException;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.model.HRawDocument;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.FileSystemService;
import org.zanata.service.FileSystemService.DownloadDescriptorProperties;
import org.zanata.service.TranslationFileService;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

@RequestScoped
@Named("fileService")
@Path(FileResource.SERVICE_PATH)
@Transactional
public class FileService implements FileResource {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FileService.class);

    private static final String FILE_TYPE_OFFLINE_PO = "offlinepo";
    private static final String FILE_TYPE_OFFLINE_PO_TEMPLATE = "offlinepot";
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
        Type genericType = new GenericType<List<DocumentType>>() {

        }.getGenericType();
        Object entity = new GenericEntity<List<DocumentType>>(
                Lists.newArrayList(
                        translationFileServiceImpl.getSupportedDocumentTypes()),
                genericType);
        return Response.ok(entity).build();
    }

    @Override
    public Response fileTypeInfoList() {
        Type genericType = new GenericType<List<FileTypeInfo>>() {

        }.getGenericType();
        Set<DocumentType> supportedDocumentTypes =
                translationFileServiceImpl.getSupportedDocumentTypes();
        List<FileTypeInfo> docTypes = supportedDocumentTypes.stream()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .map(DocumentType::toFileTypeInfo).collect(Collectors.toList());
        Object entity = new GenericEntity<>(docTypes, genericType);
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
    public Response downloadSourceFile(String projectSlug, String iterationSlug,
            String fileType, String docId) {
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
        } else if ("pot".equals(fileType)
                || FILE_TYPE_OFFLINE_PO_TEMPLATE.equals(fileType)) {
            // Note: could give 404 or unsupported media type for "pot" in
            // non-po projects,
            // and suggest using offlinepo
            Resource res = resourceUtils.buildResource(document);
            StreamingOutput output = new POTStreamingOutput(res,
                    FILE_TYPE_OFFLINE_PO_TEMPLATE.equals(fileType));
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
    public Response downloadTranslationFile(String projectSlug,
            String iterationSlug, String locale, String fileType,
            String docId) {
        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, docId);
        // TODO scan (again) for virus
        final Response response;
        HDocument document = this.documentDAO.getByProjectIterationAndDocId(
                projectSlug, iterationSlug, docId);
        if (document == null) {
            response = Response.status(Status.NOT_FOUND).build();
        } else if ("po".equals(fileType)
                || FILE_TYPE_OFFLINE_PO.equals(fileType)) {
            // Note: could return 404 or Unsupported media type for "po" in
            // non-po projects,
            // and suggest to use offlinepo
            final Set<String> extensions = new HashSet<String>();
            extensions.add("gettext");
            extensions.add("comment");
            // Perform translation of Hibernate DTOs to JAXB DTOs
            TranslationsResource transRes =
                    (TranslationsResource) this.translatedDocResourceService
                            .getTranslations(docId, new LocaleId(locale),
                                    extensions, true, null)
                            .getEntity();
            Resource res = this.resourceUtils.buildResource(document);
            StreamingOutput output = new POStreamingOutput(res, transRes,
                    FILE_TYPE_OFFLINE_PO.equals(fileType));
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
            Resource res = this.resourceUtils.buildResource(document);
            final Set<String> extensions = Collections.<String> emptySet();
            TranslationsResource transRes =
                    (TranslationsResource) this.translatedDocResourceService
                            .getTranslations(docId, new LocaleId(locale),
                                    extensions, true, null)
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
                if (target.getState() == ContentState.Approved || (useFuzzy
                        && target.getState() == ContentState.NeedReview)) {
                    filteredTranslations.add(target);
                }
            }
            transRes.getTextFlowTargets().clear();
            transRes.getTextFlowTargets().addAll(filteredTranslations);
            InputStream inputStream;
            try {
                inputStream = filePersistService.getRawDocumentContentAsStream(
                        document.getRawDocument());
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
            HRawDocument hRawDocument = document.getRawDocument();
            FileFormatAdapter adapter = translationFileServiceImpl
                    .getAdapterFor(hRawDocument.getType());
            String rawParamString = hRawDocument.getAdapterParameters();
            Optional<String> params = Optional
                    .<String> fromNullable(Strings.emptyToNull(rawParamString));
            StreamingOutput output = new FormatAdapterStreamingOutput(uri, res,
                    transRes, locale, adapter, params);
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
            response = Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
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

    private class POStreamingOutput implements StreamingOutput {
        private Resource resource;
        private TranslationsResource transRes;
        private boolean offlinePo;

        /**
         * @param offlinePo
         *            true if text flow id should be inserted into msgctxt to
         *            allow reverse mapping.
         */
        public POStreamingOutput(Resource resource,
                TranslationsResource transRes, boolean offlinePo) {
            this.resource = resource;
            this.transRes = transRes;
            this.offlinePo = offlinePo;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            PoWriter2 writer = new PoWriter2(false, offlinePo);
            writer.writePo(output, "UTF-8", this.resource, this.transRes);
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
        public POTStreamingOutput(Resource resource, boolean offlinePot) {
            this.resource = resource;
            this.offlinePot = offlinePot;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            PoWriter2 writer = new PoWriter2(false, offlinePot);
            writer.writePot(output, "UTF-8", resource);
        }
    }

    private class InputStreamStreamingOutput implements StreamingOutput {
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

    private class FormatAdapterStreamingOutput implements StreamingOutput {
        private Resource resource;
        private TranslationsResource translationsResource;
        private String locale;
        private URI original;
        private FileFormatAdapter adapter;
        private Optional<String> params;

        public FormatAdapterStreamingOutput(URI originalDoc, Resource resource,
                TranslationsResource translationsResource, String locale,
                FileFormatAdapter adapter, Optional<String> params) {
            this.resource = resource;
            this.translationsResource = translationsResource;
            this.locale = locale;
            this.original = originalDoc;
            this.adapter = adapter;
            this.params = params;
        }

        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            // FIXME should the generated file be virus scanned?
            adapter.writeTranslatedFile(output, original, resource,
                    translationsResource, locale, params);
        }
    }
    /*
     * Private class that implements downloading from a previously prepared
     * file.
     */

    private class FileStreamingOutput implements StreamingOutput {
        private File file;

        public FileStreamingOutput(File file) {
            this.file = file;
        }

        @Override
        public void write(@Nonnull OutputStream output)
                throws IOException, WebApplicationException {
            FileInputStream input = new FileInputStream(this.file);
            try {
                ByteStreams.copy(input, output);
            } finally {
                input.close();
            }
        }
    }
}
