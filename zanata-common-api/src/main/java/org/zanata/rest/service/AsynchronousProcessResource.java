/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * Represents a resource for an asynchronous process. Only certain processes are exposed
 * as an asynchronous resource.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path("/async")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public interface AsynchronousProcessResource
{

   @POST
   @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/r")
   /* Same as SourceDocResourceService.SERVICE_PATH */
   public ProcessStatus startSourceDocCreation(@PathParam("id") String idNoSlash,
                                               @PathParam("projectSlug") String projectSlug,
                                               @PathParam("iterationSlug") String iterationSlug,
                                               Resource resource,
                                               @QueryParam("ext") Set<String> extensions,
                                               @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

   @PUT
   @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/r" + SourceDocResource.RESOURCE_SLUG_TEMPLATE)
   /* Same as SourceDocResourceService.SERVICE_PATH */
   public ProcessStatus startSourceDocCreationOrUpdate(@PathParam("id") String idNoSlash,
                                                       @PathParam("projectSlug") String projectSlug,
                                                       @PathParam("iterationSlug") String iterationSlug,
                                                       Resource resource,
                                                       @QueryParam("ext") Set<String> extensions,
                                                       @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

   @PUT
   @Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/r/{id}/translations/{locale}")
   /* Same as TranslatedDocResource.putTranslations */
   public ProcessStatus startTranslatedDocCreationOrUpdate(@PathParam("id") String idNoSlash,
                                                           @PathParam("projectSlug") String projectSlug,
                                                           @PathParam("iterationSlug") String iterationSlug,
                                                           @PathParam("locale") LocaleId locale,
                                                           TranslationsResource translatedDoc,
                                                           @QueryParam("ext") Set<String> extensions,
                                                           @QueryParam("merge") String merge);

   @GET
   @Path("/{processId}")
   public ProcessStatus getProcessStatus( @PathParam("processId") String processId );

}
