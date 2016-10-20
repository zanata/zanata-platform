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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.RawDocumentContentAccessException;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.model.HRawDocument;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.service.TranslationFileService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zanata.rest.service.FileResource.FILETYPE_PO;
import static org.zanata.rest.service.FileResource.FILETYPE_TRANSLATED_APPROVED;
import static org.zanata.rest.service.FileResource.FILETYPE_TRANSLATED_APPROVED_AND_FUZZY;

/**
 * By convention, this would be called TranslatedFileService, but we already have one of those.
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RequestScoped
@Slf4j
@Transactional
public class TranslatedFileResourceService implements TranslatedFileResource {
    /**
     * See also {@link org.zanata.rest.service.SourceFileService#FILE_TYPE_OFFLINE_PO_TEMPLATE}
     */
    private static final String FILE_TYPE_OFFLINE_PO = "offlinepo";

    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private FilePersistService filePersistService;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private TranslatedDocResourceService translatedDocResourceService;
    @Inject
    private TranslationDocumentUpload translationUploader;
    @Inject
    private TranslationFileService translationFileService;
    @Inject
    private VirusScanner virusScanner;

    @Override
    public Response uploadTranslationFile(String projectSlug,
            String iterationSlug, String localeId, String docId, String merge,
            InputStream fileStream,
            String projectType) {
        //assignCreditToUploader is not supported from here
        boolean assignCreditToUploader = false;

        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, docId);
        // FIXME
        return null;
//        return translationUploader.tryUploadTranslationFile(id, localeId,
//                merge, assignCreditToUploader, fileStream,
//                TranslationSourceType.API_UPLOAD);
    }

    @Override
    public Response downloadTranslationFile(String projectSlug,
            String iterationSlug, String locale, String fileType,
            String docId, String projectType) {

        GlobalDocumentId id =
                new GlobalDocumentId(projectSlug, iterationSlug, docId);
        // TODO scan (again) for virus
        final Response response;
        HDocument document =
                this.documentDAO.getByProjectIterationAndDocId(projectSlug,
                        iterationSlug, docId);

        if (document == null) {
            response = Response.status(Response.Status.NOT_FOUND).build();
        } else if (FILETYPE_PO.equals(fileType)
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
                                    extensions, true, null).getEntity();
            Resource res = this.resourceUtils.buildResource(document);

            StreamingOutput output =
                    new POStreamingOutput(res, transRes,
                            FILE_TYPE_OFFLINE_PO.equals(fileType));
            response =
                    Response.ok()
                            .header("Content-Disposition",
                                    "attachment; filename=\""
                                            + document.getName() + ".po\"")
                            .type(MediaType.TEXT_PLAIN).entity(output).build();
        } else if (FILETYPE_TRANSLATED_APPROVED.equals(fileType)
                || FILETYPE_TRANSLATED_APPROVED_AND_FUZZY.equals(fileType)) {
            if (!filePersistService.hasPersistedDocument(id)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Resource res = this.resourceUtils.buildResource(document);

            final Set<String> extensions = Collections.<String> emptySet();
            TranslationsResource transRes =
                    (TranslationsResource) this.translatedDocResourceService
                            .getTranslations(docId, new LocaleId(locale),
                                    extensions, true, null).getEntity();
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
                if (target.getState() == ContentState.Approved
                        || (useFuzzy && target.getState() == ContentState.NeedReview)) {
                    filteredTranslations.add(target);
                }
            }

            transRes.getTextFlowTargets().clear();
            transRes.getTextFlowTargets().addAll(filteredTranslations);

            InputStream inputStream;
            try {
                inputStream =
                        filePersistService
                                .getRawDocumentContentAsStream(document
                                        .getRawDocument());
            } catch (RawDocumentContentAccessException e) {
                log.error(e.toString(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e)
                        .build();
            }
            File tempFile =
                    translationFileService.persistToTempFile(inputStream);
            String name = projectSlug + ":" + iterationSlug + ":" + docId;
            // TODO damason: this file is not transmitted, but used to generate
            // a file later
            // the generated file should be scanned instead
            virusScanner.scan(tempFile, name);
            URI uri = tempFile.toURI();
            HRawDocument hRawDocument = document.getRawDocument();
            FileFormatAdapter adapter =
                    translationFileService.getAdapterFor(hRawDocument.getType());
            String rawParamString = hRawDocument.getAdapterParameters();
            Optional<String> params =
                    Optional.<String> fromNullable(Strings
                            .emptyToNull(rawParamString));
            StreamingOutput output =
                    new FormatAdapterStreamingOutput(uri, res, transRes,
                            locale, adapter, params);
            String translationFilename = adapter.generateTranslationFilename(document, locale);
            response =
                    Response.ok()
                            .header("Content-Disposition",
                                    "attachment; filename=\""
                                            + translationFilename + "\"")
                            .entity(output).build();
            // TODO damason: remove more immediately, but make sure response has
            // finished with the file
            // Note: may not be necessary when file storage is on disk.
            // FIXME this is essentially a memory and file leak, because we rarely shut down the app server
            tempFile.deleteOnExit();
        } else {
            response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        return response;
    }

    private class FormatAdapterStreamingOutput implements StreamingOutput {
        private Resource resource;
        private TranslationsResource translationsResource;
        private String locale;
        private URI original;
        private FileFormatAdapter adapter;
        private Optional<String> params;

        public FormatAdapterStreamingOutput(URI originalDoc,
                Resource resource, TranslationsResource translationsResource,
                String locale, FileFormatAdapter adapter,
                Optional<String> params) {
            this.resource = resource;
            this.translationsResource = translationsResource;
            this.locale = locale;
            this.original = originalDoc;
            this.adapter = adapter;
            this.params = params;
        }

        @Override
        public void write(OutputStream output) throws IOException,
                WebApplicationException {
            // FIXME should the generated file be virus scanned?
            adapter.writeTranslatedFile(output, original, resource,
                    translationsResource, locale, params);
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
        public void write(OutputStream output) throws IOException,
                WebApplicationException {
            PoWriter2 writer = new PoWriter2(false, offlinePo);
            writer.writePo(output, "UTF-8", this.resource, this.transRes);
        }
    }

}
