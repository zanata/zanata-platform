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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.AsynchronousProcessResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AsyncProcessClient implements AsynchronousProcessResource {

    private final RestClientFactory factory;
    private final URI baseUri;

    AsyncProcessClient(RestClientFactory factory) {
        this.factory = factory;
        baseUri = factory.getBaseUri();
    }

    @Deprecated
    @Override
    public ProcessStatus startSourceDocCreation(String idNoSlash,
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copytrans) {
        throw new UnsupportedOperationException(
                "Not supported. Use startSourceDocCreationOrUpdate instead.");
    }

    @Override
    @Deprecated
    public ProcessStatus startSourceDocCreationOrUpdate(String idNoSlash,
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copytrans) {
        Client client = factory.getClient();
        WebTarget webResource = client.target(baseUri)
                .path(AsynchronousProcessResource.SERVICE_PATH)
                .path("projects").path("p").path(projectSlug)
                .path("iterations").path("i").path(iterationSlug)
                .path("r").path(idNoSlash);
        Response response = webResource
                .queryParam("ext", extensions.toArray())
                .queryParam("copyTrans", String.valueOf(copytrans))
                .request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.xml(resource));
        response.bufferEntity();
        return response.readEntity(ProcessStatus.class);
    }

    @Override
    public ProcessStatus startSourceDocCreationOrUpdateWithDocId(
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, String docId) {
        Client client = factory.getClient();
        WebTarget webResource = client.target(baseUri)
                .path(AsynchronousProcessResource.SERVICE_PATH)
                .path("projects").path("p").path(projectSlug)
                .path("iterations").path("i").path(iterationSlug)
                .path("resource");
        try {
            Response response = webResource
                    .queryParam("docId", docId)
                    .queryParam("ext", extensions.toArray())
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .put(Entity.xml(resource));
            response.bufferEntity();
            return response.readEntity(ProcessStatus.class);
        } catch (ResponseProcessingException e) {
            if (RestUtil.isNotFound(e.getResponse())) {
                // fallback to old endpoint
                String idNoSlash = RestUtil.convertToDocumentURIId(docId);
                return startSourceDocCreationOrUpdate(idNoSlash, projectSlug,
                        iterationSlug, resource, extensions, false);
            }
            throw e;
        }
    }

    @Override
    @Deprecated
    public ProcessStatus startTranslatedDocCreationOrUpdate(String idNoSlash,
            String projectSlug, String iterationSlug, LocaleId locale,
            TranslationsResource translatedDoc, Set<String> extensions,
            String merge, @DefaultValue("false") boolean myTrans) {
        Client client = factory.getClient();
        WebTarget webResource = client.target(baseUri)
                .path(AsynchronousProcessResource.SERVICE_PATH)
                .path("projects").path("p").path(projectSlug)
                .path("iterations").path("i").path(iterationSlug)
                .path("r").path(idNoSlash)
                .path("translations").path(locale.toString());
        Response response = webResource
                .queryParam("ext", extensions.toArray())
                .queryParam("merge", merge)
                .queryParam("assignCreditToUploader", String.valueOf(myTrans))
                .request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity
                        .xml(translatedDoc));
        response.bufferEntity();
        return response.readEntity(ProcessStatus.class);
    }

    @Override
    public ProcessStatus startTranslatedDocCreationOrUpdateWithDocId(
            String projectSlug, String iterationSlug, LocaleId locale,
            TranslationsResource translatedDoc, String docId,
            Set<String> extensions,
            String merge, boolean assignCreditToUploader) {
        Client client = factory.getClient();
        WebTarget webResource = client.target(baseUri)
                .path(AsynchronousProcessResource.SERVICE_PATH)
                .path("projects").path("p").path(projectSlug)
                .path("iterations").path("i").path(iterationSlug)
                .path("resource")
                .path("translations").path(locale.toString());
        try {
            Response response = webResource
                    .queryParam("docId", docId)
                    .queryParam("ext", extensions.toArray())
                    .queryParam("merge", merge)
                    .queryParam("assignCreditToUploader", String.valueOf(assignCreditToUploader))
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .put(Entity.xml(translatedDoc));
            response.bufferEntity();
            return response.readEntity(ProcessStatus.class);
        } catch (ResponseProcessingException e) {
            if (RestUtil.isNotFound(e.getResponse())) {
                // fallback to old endpoint
                String idNoSlash = RestUtil.convertToDocumentURIId(docId);
                return startTranslatedDocCreationOrUpdate(idNoSlash,
                        projectSlug,
                        iterationSlug, locale, translatedDoc, extensions, merge,
                        assignCreditToUploader);
            }
            throw e;
        }
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) {
        return factory.getClient().target(baseUri)
                .path(AsynchronousProcessResource.SERVICE_PATH)
                .path(processId)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(ProcessStatus.class);
    }
}
