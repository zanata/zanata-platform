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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.service.CopyTransResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CopyTransClient implements CopyTransResource {
    private final RestClientFactory factory;
    private final URI baseUri;

    CopyTransClient(RestClientFactory factory) {
        this.factory = factory;
        baseUri = factory.getBaseUri();
    }

    @Override
    public CopyTransStatus startCopyTrans(String projectSlug,
            String iterationSlug, String docId) {
        Client client = factory.getClient();
        Response response = webResource(client, projectSlug, iterationSlug, docId)
                .post(Entity.json(""));
        response.bufferEntity();
        return response.readEntity(CopyTransStatus.class);
    }

    private Invocation.Builder webResource(Client client, String projectSlug,
            String iterationSlug,
            String docId) {
        return client.target(baseUri)
                .path(CopyTransResource.SERVICE_PATH)
                .path("/proj").path(projectSlug)
                .path("iter").path(iterationSlug)
                .path("doc").path(docId)
                .request(MediaType.APPLICATION_XML_TYPE);
    }

    @Override
    public CopyTransStatus getCopyTransStatus(String projectSlug,
            String iterationSlug, String docId) {
        return webResource(factory.getClient(), projectSlug, iterationSlug, docId
        ).get(CopyTransStatus.class);
    }
}
