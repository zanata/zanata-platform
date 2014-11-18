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
import javax.ws.rs.core.UriBuilder;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.AsynchronousProcessResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import static org.zanata.rest.client.ClientUtil.resolvePath;

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

    @Override
    public ProcessStatus startSourceDocCreation(String idNoSlash,
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copytrans) {
        throw new UnsupportedOperationException(
                "Not supported. Use startSourceDocCreationOrUpdate instead.");
    }

    @Override
    public ProcessStatus startSourceDocCreationOrUpdate(String idNoSlash,
            String projectSlug, String iterationSlug, Resource resource,
            Set<String> extensions, @DefaultValue("true") boolean copytrans) {
        Client client = factory.getClient();
        CacheResponseFilter filter = new CacheResponseFilter();
        client.addFilter(filter);
        WebResource webResource = client.resource(baseUri);
        String path =
                resolvePath(webResource, AsynchronousProcessResource.class,
                        "startSourceDocCreationOrUpdate", projectSlug,
                        iterationSlug, idNoSlash);

        webResource.path(path)
                .queryParams(ClientUtil.asMultivaluedMap("ext", extensions))
                .queryParam("copyTrans", String.valueOf(copytrans))
                .put(resource);
        client.removeFilter(filter);
        return filter.getEntity(ProcessStatus.class);
    }

    @Override
    public ProcessStatus startTranslatedDocCreationOrUpdate(String idNoSlash,
            String projectSlug, String iterationSlug, LocaleId locale,
            TranslationsResource translatedDoc, Set<String> extensions,
            String merge) {
        Client client = factory.getClient();
        CacheResponseFilter filter = new CacheResponseFilter();
        client.addFilter(filter);
        WebResource webResource = client.resource(baseUri);
        String path =
                resolvePath(webResource, AsynchronousProcessResource.class,
                        "startTranslatedDocCreationOrUpdate", projectSlug,
                        iterationSlug, idNoSlash, locale);
        webResource.path(path)
                .queryParams(ClientUtil.asMultivaluedMap("ext", extensions))
                .queryParam("merge", merge)
                .put(translatedDoc);
        client.removeFilter(filter);
        return filter.getEntity(ProcessStatus.class);
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) {
        return factory.getClient().resource(baseUri)
                .path(AsynchronousProcessResource.SERVICE_PATH)
                .path(processId).get(ProcessStatus.class);
    }
}
