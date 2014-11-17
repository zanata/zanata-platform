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
import java.util.List;
import java.util.Set;

import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;

import com.sun.jersey.api.client.WebResource;

/**
 * This "implements" caller methods to endpoints in SourceDocResource.
 *
 * N.B.(as of 11/11/2014 commit 8dbf5ec) post is not used. putResource(with
 * copyTrans) is only used by PublicanPushCommand. putResource is not used.
 * getResourceMeta is not used. putResourceMeta is not used.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SourceDocResourceClient {
    private final RestClientFactory factory;
    private final String project;
    private final String projectVersion;
    private final URI baseUri;

    public SourceDocResourceClient(RestClientFactory factory, String project,
            String projectVersion) {
        this.factory = factory;
        this.project = project;
        this.projectVersion = projectVersion;
        baseUri = factory.getBaseUri();
    }

    public List<ResourceMeta> getResourceMeta(Set<String> extensions) {
        Client client = factory.getClient();
        WebResource webResource = getBaseServiceResource(client)
                .queryParams(ClientUtil.asMultivaluedMap(
                        "ext", extensions));
        return webResource
                .get(new GenericType<List<ResourceMeta>>() {
                });
    }

    private WebResource getBaseServiceResource(Client client) {
        return client.resource(baseUri)
                .path("projects").path("p")
                .path(project)
                .path("iterations").path("i")
                .path(projectVersion)
                .path("r");
    }

    public Resource getResource(String idNoSlash, Set<String> extensions) {
        Client client = factory.getClient();
        WebResource webResource =
                getBaseServiceResource(client)
                        .path(idNoSlash)
                        .queryParams(ClientUtil.asMultivaluedMap(
                                "ext", extensions));
        return webResource.get(Resource.class);
    }

    public String putResource(String idNoSlash, Resource resource,
            Set<String> extensions, boolean copyTrans) {
        Client client = factory.getClient();
        CacheResponseFilter filter = new CacheResponseFilter();
        client.addFilter(filter);
        WebResource webResource = getBaseServiceResource(client)
                .path(idNoSlash)
                .queryParams(ClientUtil.asMultivaluedMap(
                        "ext", extensions))
                .queryParam("copyTrans", String.valueOf(copyTrans));

        webResource.put(resource);
        client.removeFilter(filter);
        return filter.getEntity(String.class);
    }

    public String deleteResource(String idNoSlash) {
        Client client = factory.getClient();
        WebResource webResource = getBaseServiceResource(client);
        return webResource.path(idNoSlash).delete(String.class);
    }

}
