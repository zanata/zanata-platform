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

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.zanata.rest.DocumentFileUploadForm;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ChunkUploadResponse;
import org.zanata.rest.service.FileResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FileResourceClient {
    private final RestClientFactory factory;

    private final URI baseUri;

    FileResourceClient(RestClientFactory restClientFactory) {
        this.factory = restClientFactory;
        baseUri = restClientFactory.getBaseUri();

    }

    public StringSet acceptedFileTypes() {
        String types = factory.getClient()
                .resource(baseUri)
                .path(FileResource.SERVICE_PATH)
                .path("accepted_types")
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
        return new StringSet(types);
    }

    public ChunkUploadResponse uploadSourceFile(
            String projectSlug,
            String iterationSlug, String docId,
            DocumentFileUploadForm documentFileUploadForm) {
        CacheResponseFilter filter = new CacheResponseFilter();
        Client client = factory.getClient();
        client.addFilter(filter);
        WebResource.Builder builder = client
                .resource(baseUri)
                .path("file").path("source").path(projectSlug)
                .path(iterationSlug)
                .queryParam("docId", docId)
                .type(MediaType.MULTIPART_FORM_DATA_TYPE);
        FormDataMultiPart form =
                prepareFormDataMultiPart(documentFileUploadForm);

        builder.post(form);
        ChunkUploadResponse chunkUploadResponse =
                filter.getEntity(ChunkUploadResponse.class);
        client.removeFilter(filter);
        return chunkUploadResponse;
    }

    private FormDataMultiPart prepareFormDataMultiPart(
            DocumentFileUploadForm documentFileUploadForm) {
        FormDataMultiPart form =
                new FormDataMultiPart()
                        .field("file", documentFileUploadForm
                                .getFileStream(),
                                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        addBodyPartIfPresent(form, "adapterParams",
                documentFileUploadForm.getAdapterParams());
        addBodyPartIfPresent(form, "type", documentFileUploadForm.getFileType());
        addBodyPartIfPresent(form, "first", documentFileUploadForm.getFirst());
        addBodyPartIfPresent(form, "hash", documentFileUploadForm.getHash());
        addBodyPartIfPresent(form, "last", documentFileUploadForm.getLast());
        addBodyPartIfPresent(form, "size", documentFileUploadForm.getSize());
        addBodyPartIfPresent(form, "uploadId",
                documentFileUploadForm.getUploadId());
        return form;
    }

    public ChunkUploadResponse uploadTranslationFile(
            String projectSlug,
            String iterationSlug, String locale, String docId,
            String mergeType,
            DocumentFileUploadForm documentFileUploadForm) {
        CacheResponseFilter filter = new CacheResponseFilter();
        Client client = factory.getClient();
        client.addFilter(filter);
        WebResource.Builder builder = client.resource(baseUri)
                .path(FileResource.SERVICE_PATH)
                .path("translation")
                .path(projectSlug)
                .path(iterationSlug)
                .path(locale)
                .queryParam("docId", docId)
                .queryParam("merge", mergeType)
                .type(MediaType.MULTIPART_FORM_DATA_TYPE);
        FormDataMultiPart form =
                prepareFormDataMultiPart(documentFileUploadForm);

        builder.post(form);
        ChunkUploadResponse chunkUploadResponse =
                filter.getEntity(ChunkUploadResponse.class);
        client.removeFilter(filter);
        return chunkUploadResponse;
    }

    public ClientResponse downloadSourceFile(String projectSlug,
            String iterationSlug,
            String fileType, String docId) {
        WebResource webResource = factory.getClient().resource(baseUri)
                .path(FileResource.SERVICE_PATH).path("source")
                .path(projectSlug).path(iterationSlug).path(fileType);
        return webResource.queryParam("docId", docId).get(ClientResponse.class);
    }

    public ClientResponse downloadTranslationFile(String projectSlug,
            String iterationSlug, String locale, String fileExtension,
            String docId) {
        WebResource webResource = factory.getClient().resource(baseUri)
                .path(FileResource.SERVICE_PATH).path("translation")
                .path(projectSlug).path(iterationSlug).path(locale)
                .path(fileExtension);
        return webResource.queryParam("docId", docId).get(ClientResponse.class);
    }

    private static <T> FormDataMultiPart addBodyPartIfPresent(
            FormDataMultiPart form, String field, T value) {
        if (value != null) {
            return form.field(field, value.toString());
        }
        return form;
    }
}
