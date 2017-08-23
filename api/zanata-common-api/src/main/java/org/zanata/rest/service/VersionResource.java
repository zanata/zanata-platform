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
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.VersionInfo;

import java.io.Serializable;

/**
 * Retrieves the system's version.
 */
@Path(VersionResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("System Version")
public interface VersionResource extends Serializable {
    public static final String SERVICE_PATH = "/version";

    /**
     * Retrieve Version information for the application.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_VERSION_XML,
                MediaTypes.APPLICATION_ZANATA_VERSION_JSON })
    @TypeHint(VersionInfo.class)
    @StatusCodes({
            @ResponseCode(code = 200,
                condition = "Response with the system's version information in " +
                        "the body"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public Response get();

}
