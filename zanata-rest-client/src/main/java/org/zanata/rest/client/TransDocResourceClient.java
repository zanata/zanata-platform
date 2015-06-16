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
import java.util.Set;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.TranslationsResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static org.zanata.rest.client.ClientUtil.asMultivaluedMap;

/**
 * This "implements" caller methods to endpoints in TranslatedDocResource.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransDocResourceClient {
    private final RestClientFactory factory;
    private final String project;
    private final String projectVersion;
    private final URI baseUri;

    TransDocResourceClient(RestClientFactory factory, String project,
            String projectVersion) {
        this.factory = factory;
        this.project = project;
        this.projectVersion = projectVersion;
        baseUri = factory.getBaseUri();
    }

    public ClientResponse getTranslations(
            @PathParam("id") String idNoSlash,
            @PathParam("locale") LocaleId locale,
            @QueryParam("ext") Set<String> extensions,
            @QueryParam("skeletons") boolean createSkeletons,
            @HeaderParam(HttpHeaders.IF_NONE_MATCH) String eTag) {
        Client client = factory.getClient();
        return getBaseServiceResource(client)
                .path(idNoSlash)
                .path("translations").path(locale.getId())
                .queryParams(asMultivaluedMap("ext", extensions))
                .queryParam("skeletons", String.valueOf(createSkeletons))
                .header(HttpHeaders.IF_NONE_MATCH, eTag)
                .get(ClientResponse.class);
    }

    private WebResource getBaseServiceResource(Client client) {
        return client.resource(baseUri)
                .path("projects").path("p")
                .path(project)
                .path("iterations").path("i")
                .path(projectVersion)
                .path("r");
    }

}
