/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zanata.common.LocaleId;

/**
 * This resource allows clients to [push and] pull translation memories.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Produces( { MediaType.APPLICATION_XML /*, "application/x-tmx"*/ })
@Consumes( { MediaType.APPLICATION_XML /*, "application/x-tmx"*/ })
public interface TranslationMemoryResource
{
   public static final String PREFERRED_MEDIA_TYPE = MediaType.APPLICATION_XML;

   @GET
   @Path("all")
   public Response getAllTranslationMemory(@QueryParam("locale") LocaleId locale);

   @GET
   @Path("projects/{projectSlug}")
   public Response getProjectTranslationMemory(@PathParam("projectSlug") String projectSlug,
                                            @QueryParam("locale") LocaleId locale);
   @GET
   @Path("projects/{projectSlug}/iterations/{iterationSlug}")
   public Response getProjectIterationTranslationMemory(
         @PathParam("projectSlug") String projectSlug,
         @PathParam("iterationSlug") String iterationSlug,
         @QueryParam("locale") LocaleId locale);
}
