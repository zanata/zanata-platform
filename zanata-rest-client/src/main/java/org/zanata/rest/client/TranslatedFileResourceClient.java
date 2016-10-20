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

import static org.zanata.common.ProjectType.File;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.zanata.rest.dto.FileUploadResponse;
import org.zanata.rest.service.FileResource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TranslatedFileResourceClient {
    private final RestClientFactory factory;

    private final URI baseUri;

    TranslatedFileResourceClient(RestClientFactory restClientFactory) {
        this.factory = restClientFactory;
        baseUri = restClientFactory.getBaseUri();
    }

    public FileUploadResponse uploadSourceFile(
            String projectSlug,
            String iterationSlug, String docId,
            InputStream fileStream) {
        Client client = factory.getClient();
        WebResource.Builder builder = client
                .resource(baseUri)
                .path("file").path("source").path(projectSlug)
                .path(iterationSlug)
                .queryParam("docId", docId)
                .queryParam("projectType", File.name())
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

    public ClientResponse downloadSourceFile(String projectSlug,
            String iterationSlug,
            String fileType, String docId) {
        WebResource webResource = factory.getClient().resource(baseUri)
                .path(FileResource.SERVICE_PATH).path("source")
                .path(projectSlug).path(iterationSlug).path(fileType);
        return webResource.queryParam("docId", docId).get(ClientResponse.class);
    }

}
