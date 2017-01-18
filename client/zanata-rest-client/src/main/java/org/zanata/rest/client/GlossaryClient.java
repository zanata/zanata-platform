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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.QualifiedName;
import org.zanata.rest.service.GlossaryResource;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GlossaryClient {
    private final RestClientFactory factory;
    private final URI baseUri;

    GlossaryClient(RestClientFactory factory) {
        this.factory = factory;
        baseUri = factory.getBaseUri();
    }

    public Response post(List<GlossaryEntry> glossaryEntries, LocaleId localeId,
            String qualifiedName) {

        Entity<List<GlossaryEntry>> entity = Entity.json(glossaryEntries);

        Response response = webResource().path("entries")
                .queryParam("locale", localeId.getId())
                .queryParam("qualifiedName", qualifiedName)
                .request(MediaType.APPLICATION_JSON_TYPE).post(entity);
        response.close();
        return response;
    }

    public Response downloadFile(String fileType,
            ImmutableList<String> transLang, String qualifiedName) {
        if (transLang != null && !transLang.isEmpty()) {
            return webResource().path("file").queryParam("fileType", fileType)
                    .queryParam("locales", Joiner.on(",").join(transLang))
                    .queryParam("qualifiedName", qualifiedName)
                    .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .get();
        }
        return webResource().path("file").queryParam("fileType", fileType)
                .queryParam("qualifiedName", qualifiedName)
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get();
    }

    public void delete(String id, String qualifiedName) {
        webResource().path("entries/" + id)
                .queryParam("qualifiedName", qualifiedName)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete().close();
    }

    public int deleteAll(String qualifiedName) {
        return webResource().queryParam("qualifiedName", qualifiedName)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete(Integer.class);
    }

    public String getProjectQualifiedName(String projectSlug) {
        return projectGlossaryWebResource(projectSlug).path("qualifiedName")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(QualifiedName.class).getName();
    }

    public String getGlobalQualifiedName() {
        return webResource().path("qualifiedName")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(QualifiedName.class)
                .getName();
    }

    public Response find(String query, String qualifiedName) {
        return webResource().path("entries/")
                .queryParam("qualifiedName", qualifiedName)
                .queryParam("filter", query)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
    }

    private WebTarget webResource() {
        return factory.getClient().target(baseUri)
                .path(GlossaryResource.SERVICE_PATH);
    }

    private WebTarget projectGlossaryWebResource(String projectSlug) {
        return factory.getClient()
                .target(factory.getBaseUri())
                .path("projects").path("p").path(projectSlug).path("glossary");
    }

}
