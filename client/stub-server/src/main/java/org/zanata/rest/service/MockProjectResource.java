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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.Project;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path(ProjectResource.SERVICE_PATH)
public class MockProjectResource implements ProjectResource {
    private static final long serialVersionUID = -4283910776728392504L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Context
    UriInfo uriInfo;

    @PathParam("projectSlug")
    String projectSlug;

    @Override
    public Response head() {
        return MockResourceUtil.notUsedByClient();
    }

    @Override
    public Response get() {
        return Response.ok(new Project("about-fedora", "About Fedora",
                ProjectType.Podir.name().toLowerCase())).build();
    }

    @Override
    public Response put(Project project) {
        return Response.created(uriInfo.getRequestUri()).build();
    }

    @Override
    public Response getGlossaryQualifiedName() {
        String qualifiedName = "project/" + projectSlug;
        return Response.ok(qualifiedName).build();
    }
}

