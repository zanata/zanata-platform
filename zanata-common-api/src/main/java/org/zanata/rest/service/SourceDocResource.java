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

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.zanata.common.Namespaces;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface SourceDocResource
{
   public static final String RESOURCE_SLUG_REGEX = "[a-zA-Z0-9]+([a-zA-Z0-9_\\-,{.}]*[a-zA-Z0-9]+)?";
   public static final String RESOURCE_NAME_REGEX = "[a-zA-Z0-9]+([/a-zA-Z0-9_\\-,{.}]*[a-zA-Z0-9]+)?"; // as above, plus the '/'
   public static final String RESOURCE_SLUG_TEMPLATE = "/{id:" + RESOURCE_SLUG_REGEX + "}";

   @HEAD
   public Response head();

   /**
    * Retrieve the List of Resources
    *
    * @return Response.ok with ResourcesList or Response(404) if not found
    */
   @GET
   @Wrapped(element = "resources", namespace = Namespaces.ZANATA_API)
   public Response get(@QueryParam("ext") Set<String> extensions);

   @POST
   public Response post(Resource resource, @QueryParam("ext") Set<String> extensions, @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response getResource(@PathParam("id") String idNoSlash, @QueryParam("ext") Set<String> extensions);

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response putResource(@PathParam("id") String idNoSlash, Resource resource, @QueryParam("ext") Set<String> extensions, @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans);

   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response deleteResource(@PathParam("id") String idNoSlash);

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response getResourceMeta(@PathParam("id") String idNoSlash, @QueryParam("ext") Set<String> extensions);

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response putResourceMeta(@PathParam("id") String idNoSlash, ResourceMeta messageBody, @QueryParam("ext") Set<String> extensions);

}
