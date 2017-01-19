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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.rest.dto.Project;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ProjectClient {
    private final RestClientFactory factory;
    private final String projectSlug;

    ProjectClient(RestClientFactory factory, String projectSlug) {
        this.factory = factory;
        this.projectSlug = projectSlug;
    }

    public Project get() {
        try {
            return webResource()
                    .get(Project.class);
        } catch (ResponseProcessingException rpe) {
            if (rpe.getResponse().getStatus() == 404) {
                return null;
            }
            throw rpe;
        }
    }

    private Invocation.Builder webResource() {
        return factory.getClient()
                .target(factory.getBaseUri())
                .path("projects").path("p").path(projectSlug)
                .request(MediaType.APPLICATION_XML_TYPE);
    }

    public void put(Project project) {
        Response response = webResource().put(Entity.xml(project));
        response.close();
    }
}

