/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.common.ContentState;
import org.zanata.common.FileTypeInfo;
import org.zanata.common.DocumentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import static org.zanata.common.ProjectType.fileProjectSourceDocTypes;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(FileResource.SERVICE_PATH)
public class MockFileResource implements FileResource {

    @Override
    @Deprecated
    public Response acceptedFileTypes() {
        StringSet extensions = new StringSet("");
        for (DocumentType docType : ProjectType
                .getSupportedSourceFileTypes(ProjectType.File)) {
            extensions.addAll(docType.getSourceExtensions());
        }
        for (DocumentType docType : ProjectType
                .getSupportedSourceFileTypes(ProjectType.Gettext)) {
            extensions
                    .addAll(docType.getSourceExtensions());
        }
        return Response.ok(extensions.toString()).build();
    }

    @Override
    @Deprecated
    public Response acceptedFileTypeList() {
        GenericEntity<List<DocumentType>> genericEntity =
            new GenericEntity<List<DocumentType>>(fileProjectSourceDocTypes()) {};
        return Response.ok(genericEntity).build();
    }

    @Override
    @Deprecated
    public Response fileTypeInfoList() {
        List<FileTypeInfo> fileTypeInfoList = fileProjectSourceDocTypes().stream().map(
                DocumentType::toFileTypeInfo).collect(Collectors.toList());
        GenericEntity<List<FileTypeInfo>> genericEntity =
                new GenericEntity<List<FileTypeInfo>>(fileTypeInfoList) {};
        return Response.ok(genericEntity).build();
    }

    @Override
    public Response uploadSourceFile(String projectSlug, String iterationSlug,
            String docId, @MultipartForm DocumentFileUploadForm uploadForm) {
        return Response.status(Response.Status.CREATED).entity(
                new ChunkUploadResponse(1L, 1, false,
                        "Upload of new source document successful."))
                .build();
    }

    @Override
    public Response uploadTranslationFile(String projectSlug,
            String iterationSlug, String localeId, String docId, String merge,
            @MultipartForm DocumentFileUploadForm uploadForm) {
        return Response.ok(
                new ChunkUploadResponse(1L, 1, false,
                        "Translations uploaded successfully"))
                .build();
    }

    @Override
    public Response downloadSourceFile(String projectSlug, String iterationSlug,
            String fileType, final String docId) {
        StreamingOutput output = new StreamingOutput() {

            @Override
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
                PoWriter2 writer = new PoWriter2(false, false);
                Resource doc = sampleResource(docId);
                writer.writePot(output, "UTF-8", doc);
            }
        };
        return Response
                .ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + docId
                                + ".pot\"").type(MediaType.TEXT_PLAIN)
                .entity(output).build();
    }

    private static Resource sampleResource(String docId) {
        Resource doc = new Resource(docId);
        doc.getTextFlows().add(new TextFlow("hello", LocaleId.EN_US,
                "hello world"));
        return doc;
    }

    @Override
    public Response downloadTranslationFile(String projectSlug,
            String iterationSlug, String locale, String fileExtension,
            final String docId, final String minContentState) {
        StreamingOutput output = new StreamingOutput() {

            @Override
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
                PoWriter2 writer = new PoWriter2(false, false);
                writer.writePo(output, "UTF-8", sampleResource(docId),
                        sampleTransResource());
            }
        };
        return Response.ok()
                .header("Content-Disposition",
                        "attachment; filename=\""
                                + docId + ".po\"").type(MediaType.TEXT_PLAIN)
                .entity(output).build();
    }

    private static TranslationsResource sampleTransResource() {
        TranslationsResource resource = new TranslationsResource();
        resource.getExtensions(true);
        TextFlowTarget hello = new TextFlowTarget("hello");
        hello.getExtensions(true);
        hello.setState(ContentState.Translated);
        hello.setContents("hola mundo");
        resource.getTextFlowTargets().add(hello);
        return resource;
    }

    @Override
    public Response download(final String downloadId) {
        StreamingOutput output = new StreamingOutput() {

            @Override
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
                PoWriter2 writer = new PoWriter2(false, false);
                writer.writePo(output, "UTF-8", sampleResource(downloadId),
                        sampleTransResource());
            }
        };
        return Response.ok()
                .header("Content-Disposition",
                        "attachment; filename=\""
                                + downloadId + ".po\"")
                .type(MediaType.TEXT_PLAIN)
                .entity(output).build();
    }
}

