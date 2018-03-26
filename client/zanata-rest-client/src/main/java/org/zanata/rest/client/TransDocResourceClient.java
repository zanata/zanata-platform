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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;
import org.zanata.common.MinContentState;
import org.zanata.rest.RestUtil;

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

    public Response getTranslations(String docId, LocaleId locale,
                                    Set<String> extensions, boolean createSkeletons,
                                    MinContentState minContentState, String eTag) {
        Client client = factory.getClient();
        try {
            return getBaseServiceResource(client)
                    .path("resource")
                    .path("translations")
                    .path(locale.getId())
                    .queryParam("docId", docId)
                    .queryParam("ext", extensions.toArray())
                    .queryParam("skeletons", String.valueOf(createSkeletons))
                    .queryParam("minContentState", minContentState.toString())
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .header(HttpHeaders.IF_NONE_MATCH, eTag)
                    .get();
        } catch (ResponseProcessingException e) {
            if (RestUtil.isNotFound(e.getResponse())) {
                // fallback to old endpoint
                String idNoSlash = RestUtil.convertToDocumentURIId(docId);
                return getBaseServiceResource(client)
                        .path("r")
                        .path(idNoSlash)
                        .path("translations").path(locale.getId())
                        .queryParam("ext", extensions.toArray())
                        .queryParam("skeletons", String.valueOf(createSkeletons))
                        .request(MediaType.APPLICATION_XML_TYPE)
                        .header(HttpHeaders.IF_NONE_MATCH, eTag)
                        .get();
            }
            throw e;
        }
    }

    private WebTarget getBaseServiceResource(Client client) {
        return client.target(baseUri)
                .path("projects").path("p")
                .path(project)
                .path("iterations").path("i")
                .path(projectVersion);
    }

}
