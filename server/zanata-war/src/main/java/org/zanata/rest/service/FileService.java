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
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.resteasy.util.GenericType;

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.adapter.FileFormatAdapter;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.DocumentType;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.file.FilePersistService;
import org.zanata.file.GlobalDocumentId;
import org.zanata.file.RawDocumentContentAccessException;
import org.zanata.file.SourceDocumentUpload;
import org.zanata.file.TranslationDocumentUpload;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
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

import static org.apache.commons.lang3.StringUtils.isEmpty;

@RequestScoped
@Named("fileService")
@Path(FileResource.SERVICE_PATH)
@Slf4j
@Transactional
public class FileService implements FileResource {
    @Inject
    private FileSystemService fileSystemServiceImpl;

    @Inject
    private TranslationFileService translationFileServiceImpl;

    /**
     * Deprecated.
     * @see #fileTypeInfoList
     */
    @Override
    @Deprecated
    public Response acceptedFileTypes() {
        StringSet acceptedTypes = new StringSet("");
        acceptedTypes.addAll(translationFileServiceImpl
                .getSupportedExtensions());
        return Response.ok(acceptedTypes.toString()).build();
    }

    /**
     * Deprecated.
     * @see #fileTypeInfoList
     */
    @Override
    @Deprecated
    public Response acceptedFileTypeList() {
        Type genericType = new GenericType<List<DocumentType>>() {
        }.getGenericType();

        Object entity =
            new GenericEntity<List<DocumentType>>(Lists.newArrayList(translationFileServiceImpl
                .getSupportedDocumentTypes()), genericType);
        return Response.ok(entity).build();
    }

    @Override
    public Response fileTypeInfoList() {
        Type genericType = new GenericType<List<FileTypeInfo>>() {
        }.getGenericType();

        Set<DocumentType> supportedDocumentTypes = translationFileServiceImpl.
                getSupportedDocumentTypes();
        List<FileTypeInfo> docTypes = supportedDocumentTypes.stream()
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .map(DocumentType::toFileTypeInfo)
                .collect(Collectors.toList());
        Object entity = new GenericEntity<>(docTypes, genericType);
        return Response.ok(entity).build();
    }

    @Deprecated
    @Override
    @Produces("")
    public Response uploadSourceFile(String projectSlug, String iterationSlug,
            String docId, DocumentFileUploadForm uploadForm) {
        throw new RuntimeException("routing error");
    }

    @Deprecated
    @Override
    @Produces("")
    public Response uploadTranslationFile(String projectSlug,
            String iterationSlug, String localeId, String docId, String merge,
            DocumentFileUploadForm uploadForm) {
        throw new RuntimeException("routing error");
    }

    @Deprecated
    @Override
    @Produces("")
    public Response downloadSourceFile(String projectSlug,
            String iterationSlug, String fileType, String docId) {
        throw new RuntimeException("routing error");
    }

    @Deprecated
    @Override
    @Produces("")
    public Response downloadTranslationFile(String projectSlug,
            String iterationSlug, String locale, String fileType, String docId) {
        throw new RuntimeException("routing error");
    }

    @Override
    public Response download(String downloadId) {
        // TODO scan (again) for virus
        try {
            // Check that the download exists by looking at the download
            // descriptor
            Properties descriptorProps =
                    this.fileSystemServiceImpl
                            .findDownloadDescriptorProperties(downloadId);

            if (descriptorProps == null) {
                return Response.status(Status.NOT_FOUND).build();
            } else {
                File toDownload =
                        this.fileSystemServiceImpl.findDownloadFile(downloadId);

                if (toDownload == null) {
                    return Response.status(Status.NOT_FOUND).build();
                } else {
                    return Response
                            .ok()
                            .header("Content-Disposition",
                                    "attachment; filename=\""
                                            + descriptorProps
                                                    .getProperty(DownloadDescriptorProperties.DownloadFileName
                                                            .toString()) + "\"")
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
     * Private class that implements downloading from a previously prepared
     * file.
     */
    private class FileStreamingOutput implements StreamingOutput {
        private File file;

        public FileStreamingOutput(File file) {
            this.file = file;
        }

        @Override
        public void write(@Nonnull OutputStream output) throws IOException,
                WebApplicationException {
            FileInputStream input = new FileInputStream(this.file);
            try {
                ByteStreams.copy(input, output);
            } finally {
                input.close();
            }
        }
    }

}
