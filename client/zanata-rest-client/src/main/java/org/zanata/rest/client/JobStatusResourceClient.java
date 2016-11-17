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

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.zanata.rest.dto.JobStatus;
import org.zanata.rest.service.JobStatusResource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @see JobStatusResource
 */
public class JobStatusResourceClient {
    private static final String SERVICE_PATH =
            JobStatusResource.class.getAnnotation(Path.class).value();
    private final RestClientFactory factory;
    private final URI baseUri;

    JobStatusResourceClient(RestClientFactory restClientFactory) {
        this.factory = restClientFactory;
        baseUri = restClientFactory.getBaseUri();
    }

    public JobStatus getJobStatus(
            String jobId) {
        Client client = factory.getClient();

        WebTarget target = client
                .target(baseUri)
                .path(SERVICE_PATH)
                .resolveTemplate("jobId", jobId);
        Invocation.Builder builder = target
                .request(MediaType.APPLICATION_JSON_TYPE);
        return builder.get(JobStatus.class);
    }

}
