/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.LocaleDetails;

import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.zanata.rest.dto.SourceLocaleDetails;

import java.io.Serializable;

/**
 * REST interface for configured version locales.
 * @see {@link ProjectLocalesResource} Project locales
 */
@Path(ProjectIterationLocalesResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@ResourceLabel("Version Locales")
public interface ProjectIterationLocalesResource extends Serializable {
    @SuppressWarnings("deprecation")
    public static final String SERVICE_PATH = ProjectIterationResource.SERVICE_PATH
            + "/locales";

    /**
     * Returns list of active locales for a single project-version.
     *
     * This may be the list of locales inherited from the project.
     *
     * This also returns locale aliases.
     *
     * @see ProjectVersionResource#getLocales(String, String)
     *
     * @return
     *    OK 200 containing the list of LocaleDetails
     *    NOT FOUND 404 if the project-version does not exist
     */
    @GET
    @TypeHint(LocaleDetails[].class)
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_LOCALES_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_LOCALES_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response get();

    /**
     * Returns list of active source locales of all documents for a single project-version.
     *
     * @return
     *    OK 200 containing the list of SourceLocaleDetails
     *    NOT FOUND 404 if the project does not exist
     */
    @GET
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Content contains a list of source locale details"),
            @ResponseCode(code = 404, condition = "The project version is not found"),
    })
    @TypeHint(SourceLocaleDetails[].class)
    @Path("/source")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourceLocales();

}
