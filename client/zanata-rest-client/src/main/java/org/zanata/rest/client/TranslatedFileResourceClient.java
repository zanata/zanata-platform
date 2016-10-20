/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.zanata.common.ProjectType;
import org.zanata.rest.dto.FileUploadResponse;
import org.zanata.rest.service.TranslatedFileResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @see TranslatedFileResource
 */
public class TranslatedFileResourceClient {
    private final RestClientFactory factory;

    private final URI baseUri;

    TranslatedFileResourceClient(RestClientFactory restClientFactory) {
        this.factory = restClientFactory;
        baseUri = restClientFactory.getBaseUri();
    }

    public FileUploadResponse uploadTranslatedFile(
            String projectSlug,
            String iterationSlug, String locale, String docId,
            String mergeType,
            ProjectType projectType,
            InputStream fileStream) {
        Client client = factory.getClient();
        WebResource.Builder builder = client
                .resource(baseUri)
                .path(TranslatedFileResource.SERVICE_PATH)
                .path(projectSlug).path(iterationSlug)
                .path(locale)
                .queryParam("docId", docId)
                .queryParam("merge", mergeType)
                .queryParam("projectType", projectType.name())
                .type(MediaType.APPLICATION_OCTET_STREAM);
//        addBodyPartIfPresent(form, "adapterParams",
//                documentFileUploadForm.getAdapterParams());
//        addBodyPartIfPresent(form, "type", documentFileUploadForm.getFileType());

        ClientResponse response = builder.post(ClientResponse.class, fileStream);
        if (response.getStatus() == 404) {
            throw new RuntimeException("Encountered 404 during post form");
        }
        return response.getEntity(FileUploadResponse.class);
    }

    public ClientResponse downloadTranslatedFile(String projectSlug,
            String iterationSlug, String locale, String fileExtension,
            String docId, ProjectType projectType) {
        WebResource webResource = factory.getClient().resource(baseUri)
                .path(TranslatedFileResource.SERVICE_PATH)
                .path(projectSlug).path(iterationSlug).path(locale)
                .path(fileExtension)
                .queryParam("projectType", projectType.name());
        return webResource.queryParam("docId", docId).get(ClientResponse.class);
    }
}
