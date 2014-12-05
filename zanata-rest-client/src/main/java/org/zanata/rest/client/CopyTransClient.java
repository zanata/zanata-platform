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

import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.service.CopyTransResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

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
        CacheResponseFilter filter = new CacheResponseFilter();
        client.addFilter(filter);
        webResource(client, projectSlug, iterationSlug, docId)
                .post();
        client.removeFilter(filter);
        return filter.getEntity(CopyTransStatus.class);
    }

    private WebResource webResource(Client client, String projectSlug,
            String iterationSlug,
            String docId) {
        return client.resource(baseUri)
                .path(CopyTransResource.SERVICE_PATH)
                .path("/proj").path(projectSlug)
                .path("iter").path(iterationSlug)
                .path("doc").path(docId);
    }

    @Override
    public CopyTransStatus getCopyTransStatus(String projectSlug,
            String iterationSlug, String docId) {
        return webResource(factory.getClient(), projectSlug, iterationSlug, docId
        ).get(CopyTransStatus.class);
    }
}
