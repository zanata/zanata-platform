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

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.common.ProjectType;
import org.zanata.rest.dto.JobStatus;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.service.SourceFileResource;

import static javax.ws.rs.client.Entity.entity;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @see SourceFileResource
 */
public class SourceFileResourceClient {
    private static final String SERVICE_PATH =
            SourceFileResource.class.getAnnotation(Path.class).value();
    private final RestClientFactory factory;
    private final URI baseUri;

    SourceFileResourceClient(RestClientFactory restClientFactory) {
        this.factory = restClientFactory;
        baseUri = restClientFactory.getBaseUri();
    }

    public JobStatus uploadSourceFile(
            String projectSlug,
            String versionSlug,
            String docId,
            ProjectType projectType,
            InputStream fileStream,
            long size) {
        Client client = factory.getClient();

        WebTarget target = client
                .target(baseUri)
                .path(SERVICE_PATH)
                .resolveTemplate("projectSlug", projectSlug)
                .resolveTemplate("versionSlug", versionSlug);
        Invocation.Builder builder = target
                .queryParam("docId", docId)
                .queryParam("projectType", projectType.name())
                .queryParam("size", size)
                .request(MediaType.APPLICATION_JSON_TYPE);
        return builder.post(entity(fileStream, MediaType.APPLICATION_OCTET_STREAM_TYPE),
                JobStatus.class);
    }

    public Response downloadSourceFile(String projectSlug,
            String versionSlug,
            String docId, ProjectType projectType) {
        Invocation.Builder builder = factory.getClient().target(baseUri)
                .path(SERVICE_PATH)
                .resolveTemplate("projectSlug", projectSlug)
                .resolveTemplate("versionSlug", versionSlug)
                .queryParam("projectType", projectType.name())
                .queryParam("docId", docId)
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        return builder.get();
    }

}
