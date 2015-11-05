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

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;

import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.GlossaryResource;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

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

    public void post(List<GlossaryEntry> glossaryEntries) {
        Type genericType = new GenericType<List<GlossaryEntry>>() {
        }.getType();

        Object entity =
                new GenericEntity<List<GlossaryEntry>>(glossaryEntries,
                        genericType);

        webResource().path("entries").type(MediaType.APPLICATION_XML_TYPE)
                .post(entity);
    }

    public void delete(String id) {
        webResource().path("entries/" + id)
                .delete();
    }

    public void deleteAll() {
        webResource().delete();
    }

    private WebResource webResource() {
        return factory.getClient().resource(baseUri)
                .path(GlossaryResource.SERVICE_PATH);
    }
}
