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

package org.zanata.rest.service;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.zanata.rest.dto.ProjectIteration;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(ProjectIterationResource.SERVICE_PATH)
public class MockProjectIterationResource implements ProjectIterationResource {
    @Context
    UriInfo uriInfo;

    @Override
    public Response head() {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response get() {
        return Response.ok(new ProjectIteration("master")).build();
    }

    @Override
    public Response put(ProjectIteration project) {
        return Response.created(uriInfo.getRequestUri()).build();
    }

    @Override
    public Response sampleConfiguration() {
        String config =
                new StringBuilder()
                        .append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
                        .append("<config xmlns=\"http://zanata.org/namespace/config/\">\n")
                        .append("  <url>")
                        .append(uriInfo.getBaseUri())
                        .append("</url>\n")
                        .append("  <project>about-fedora</project>\n")
                        .append("  <project-version>master</project-version>\n")
                        .append("</config>").toString();
        return Response.ok(config, MediaType.TEXT_PLAIN_TYPE).build();
    }
}

