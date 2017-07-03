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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.RestUtil;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;

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

    SourceDocResourceClient(RestClientFactory factory, String project,
            String projectVersion) {
        this.factory = factory;
        this.project = project;
        this.projectVersion = projectVersion;
        baseUri = factory.getBaseUri();
    }

    public List<ResourceMeta> getResourceMeta(Set<String> extensions) {
        Client client = factory.getClient();
        WebTarget webResource = getBaseServiceResource(client);
        if (extensions != null) {
            webResource.queryParam("ext", extensions.toArray());
        }
        return webResource.request(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<List<ResourceMeta>>() {});
    }

    private WebTarget getBaseServiceResource(Client client) {
        return client.target(baseUri)
                .path("projects").path("p")
                .path(project)
                .path("iterations").path("i")
                .path(projectVersion)
                .path("r");
    }

    public Resource getResource(String id, Set<String> extensions) {
        Client client = factory.getClient();
        WebTarget webResource =
                getBaseServiceResource(client)
                        .path("resource")
                        .queryParam("id", id)
                        .queryParam("ext", extensions.toArray());
        Response response = webResource.request(MediaType.APPLICATION_XML_TYPE)
                .get();
        if (RestUtil.isNotFound(response)) {
            // fallback to old endpoint
            String idNoSlash = RestUtil.convertToDocumentURIId(id);
            webResource =
                    getBaseServiceResource(client)
                            .path(idNoSlash)
                            .queryParam("ext", extensions.toArray());
            return webResource.request(MediaType.APPLICATION_XML_TYPE)
                    .get(Resource.class);
        }
        return response.readEntity(Resource.class);
    }

    public String putResource(String id, Resource resource,
            Set<String> extensions, boolean copyTrans) {
        Client client = factory.getClient();
        WebTarget webResource = getBaseServiceResource(client).path("resource")
                .queryParam("id", id)
                .queryParam("ext", extensions.toArray())
                .queryParam("copyTrans", String.valueOf(copyTrans));

        Response response = webResource.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(resource, MediaType.APPLICATION_XML_TYPE));
        if (RestUtil.isNotFound(response)) {
            // fallback to old endpoint
            String idNoSlash = RestUtil.convertToDocumentURIId(id);
            webResource = getBaseServiceResource(client)
                    .path(idNoSlash)
                    .queryParam("ext", extensions.toArray())
                    .queryParam("copyTrans", String.valueOf(copyTrans));
            response = webResource.request(MediaType.APPLICATION_XML_TYPE)
                    .put(Entity.entity(resource, MediaType.APPLICATION_XML_TYPE));
        }
        response.bufferEntity();
        return response.readEntity(String.class);
    }

    public String deleteResource(String id) {
        Client client = factory.getClient();
        WebTarget webResource = getBaseServiceResource(client);
        Response response =
                webResource.path("resource").queryParam("id", id).request()
                        .delete();
        if (RestUtil.isNotFound(response)) {
            String idNoSlash = RestUtil.convertToDocumentURIId(id);
            return webResource.path(idNoSlash).request().delete(String.class);
        }
        return response.readEntity(String.class);
    }
}
