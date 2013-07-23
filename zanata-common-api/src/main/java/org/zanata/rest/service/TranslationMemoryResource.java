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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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
   public Response getAllTranslationMemory(@QueryParam("locale") @Nullable LocaleId locale);

   @GET
   @Path("projects/{projectSlug}")
   public Response getProjectTranslationMemory(@PathParam("projectSlug") @Nonnull String projectSlug,
                                            @QueryParam("locale") @Nullable LocaleId locale);
   @GET
   @Path("projects/{projectSlug}/iterations/{iterationSlug}")
   public Response getProjectIterationTranslationMemory(
         @PathParam("projectSlug") @Nonnull String projectSlug,
         @PathParam("iterationSlug") @Nonnull String iterationSlug,
         @QueryParam("locale") @Nullable LocaleId locale);

   @GET
   @Path("{slug}")
   public Response getTranslationMemory(
         @PathParam("slug") @Nonnull String slug,
         @PathParam("locale") @Nullable String locale);

   @POST
   @Path("{slug}")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   public Response updateTranslationMemory(@PathParam("slug") String slug, MultipartFormDataInput input) throws Exception;

   @DELETE
   @Path("{slug}/transunits")
   public Response deleteTranslationUnits(@PathParam("slug") String slug);
}
