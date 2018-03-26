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

package org.zanata.rest.client;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.common.FileTypeInfo;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.zanata.common.DocumentType;
import org.zanata.common.MinContentState;
import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.service.FileResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FileResourceClient {
    private final RestClientFactory factory;

    private final URI baseUri;
    private final Annotation[] multipartFormAnnotations =
            { new MultipartFormLiteral() };

    FileResourceClient(RestClientFactory restClientFactory) {
        this.factory = restClientFactory;
        baseUri = restClientFactory.getBaseUri();

    }

    @Deprecated
    public List<DocumentType> acceptedFileTypes() {
        List<DocumentType> types = factory.getClient()
                .target(baseUri)
                .path(FileResource.SERVICE_PATH
                    + FileResource.ACCEPTED_TYPE_LIST_RESOURCE)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<DocumentType>>() {
            });
        return types;
    }

    public List<FileTypeInfo> fileTypeInfoList() {
        List<FileTypeInfo> types = factory.getClient()
                .target(baseUri)
                .path(FileResource.SERVICE_PATH
                        + FileResource.FILE_TYPE_INFO_RESOURCE)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<FileTypeInfo>>() {
                });
        return types;
    }

    public ChunkUploadResponse uploadSourceFile(
            String projectSlug,
            String iterationSlug, String docId,
            DocumentFileUploadForm documentFileUploadForm) {
        Client client = factory.getClient();
        Invocation.Builder builder = client
                .target(baseUri)
                .path("file").path("source").path(projectSlug)
                .path(iterationSlug)
                .queryParam("docId", docId)
                .request(MediaType.APPLICATION_XML_TYPE);

        // there seems to be a gap in the resteasy api to support multipart form
        // with this fluent client. We have to provide the @MultipartForm
        // annotation to the method.
        // otherwise Resteasy can't find the writer for multipart form
        Response response = builder.post(Entity.entity(documentFileUploadForm,
                MediaType.MULTIPART_FORM_DATA_TYPE, multipartFormAnnotations));
        response.bufferEntity();

        return response.readEntity(ChunkUploadResponse.class);
    }


    public ChunkUploadResponse uploadTranslationFile(
            String projectSlug,
            String iterationSlug, String locale, String docId,
            String mergeType,
            DocumentFileUploadForm documentFileUploadForm) {
        Client client = factory.getClient();
        Invocation.Builder builder = client.target(baseUri)
                .path(FileResource.SERVICE_PATH)
                .path("translation")
                .path(projectSlug)
                .path(iterationSlug)
                .path(locale)
                .queryParam("docId", docId)
                .queryParam("merge", mergeType)
                .request(MediaType.APPLICATION_XML_TYPE);


        Response response = builder.post(Entity.entity(documentFileUploadForm,
                MediaType.MULTIPART_FORM_DATA_TYPE, multipartFormAnnotations));
        response.bufferEntity();
        return response.readEntity(ChunkUploadResponse.class);
    }

    public Response downloadSourceFile(String projectSlug,
            String iterationSlug,
            String fileType, String docId) {
        WebTarget webResource = factory.getClient().target(baseUri)
                .path(FileResource.SERVICE_PATH).path("source")
                .path(projectSlug).path(iterationSlug).path(fileType);
        return webResource.queryParam("docId", docId)
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get();
    }

    public Response downloadTranslationFile(String projectSlug,
                                            String iterationSlug, String locale, String fileExtension,
                                            String docId) {
        WebTarget webResource = factory.getClient().target(baseUri)
                .path(FileResource.SERVICE_PATH).path("translation")
                .path(projectSlug).path(iterationSlug).path(locale)
                .path(fileExtension);
        return webResource.queryParam("docId", docId)
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();
    }

    public Response downloadTranslationFile(String projectSlug,
                                            String iterationSlug, String locale, String fileExtension,
                                            String docId, MinContentState minContentState) {
        WebTarget webResource = factory.getClient().target(baseUri)
                .path(FileResource.SERVICE_PATH).path("translation")
                .path(projectSlug).path(iterationSlug).path(locale)
                .path(fileExtension);
        return webResource.queryParam("docId", docId)
                          .queryParam("minContentState", minContentState.toString())
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();
    }

    @SuppressWarnings("all")
    private static class MultipartFormLiteral implements MultipartForm {

        @Override
        public java.lang.Class<? extends Annotation> annotationType() {
            return MultipartForm.class;
        }
    }
}
