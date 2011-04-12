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

package net.openl10n.flies.rest.service;

import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public interface TranslationResourcesResource
{
   public static final String RESOURCE_SLUG_TEMPLATE = "/{id:[a-zA-Z0-9]+([a-zA-Z0-9_\\-,{.}]*[a-zA-Z0-9]+)?}";


   @HEAD
   public Response head();

   /**
    * Retrieve the List of Resources
    * 
    * @return Response.ok with ResourcesList or Response(404) if not found
    */
   @GET
   @Wrapped(element = "resources", namespace = Namespaces.FLIES)
   public Response get();

   @POST
   public Response post(InputStream messageBody);

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response getResource(@PathParam("id") String idNoSlash);

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response putResource(@PathParam("id") String idNoSlash, InputStream messageBody);

   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response deleteResource(@PathParam("id") String idNoSlash);

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response getResourceMeta(@PathParam("id") String idNoSlash);

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response putResourceMeta(@PathParam("id") String idNoSlash, InputStream messageBody);

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response getTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale);

   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response deleteTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale);

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response putTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, @PathParam("merge") String merge, InputStream messageBody);

}