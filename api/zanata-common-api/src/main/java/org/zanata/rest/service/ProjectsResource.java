/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * API endpoint to get a list of projects.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Path(ProjectsResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Project List")
public interface ProjectsResource extends Serializable {
    public static final String SERVICE_PATH = "/projects";

    /**
     * Retrieves a full list of projects in the system.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECTS_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(Project[].class)
    @StatusCodes({
            @ResponseCode(code = 200,
                    condition = "Response containing a full list of projects."),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public
    Response get();

}
