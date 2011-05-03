package org.zanata.rest.client;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


import org.jboss.resteasy.client.ClientResponse;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

@Produces({ MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_XML })
public interface ITranslationResources
{

   @GET
   public ClientResponse<List<ResourceMeta>> get(@QueryParam("ext") StringSet extensions);

   @POST
   public ClientResponse<String> post(Resource messageBody, @QueryParam("ext") StringSet extensions);

   @GET
   @Path("{id}")
   public ClientResponse<Resource> getResource(@PathParam("id") String idNoSlash, @QueryParam("ext") StringSet extensions);

   @PUT
   @Path("{id}")
   public ClientResponse<String> putResource(@PathParam("id") String idNoSlash, Resource messageBody, @QueryParam("ext") StringSet extensions);

   @PUT
   @Path("{id}")
   public ClientResponse<String> putResource(@PathParam("id") String idNoSlash, Resource messageBody, @QueryParam("ext") StringSet extensions, @QueryParam("copyTrans") boolean copyTrans);
   
   @DELETE
   @Path("{id}")
   public ClientResponse<String> deleteResource(@PathParam("id") String idNoSlash);

   @GET
   @Path("{id}/meta")
   public ClientResponse<ResourceMeta> getResourceMeta(@PathParam("id") String idNoSlash, @QueryParam("ext") StringSet extensions);

   @PUT
   @Path("{id}/meta")
   public ClientResponse<String> putResourceMeta(@PathParam("id") String idNoSlash, ResourceMeta messageBody, @QueryParam("ext") StringSet extensions);

   @GET
   @Path("{id}/translations/{locale}")
   public ClientResponse<TranslationsResource> getTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, @QueryParam("ext") StringSet extensions);

   @DELETE
   @Path("{id}/translations/{locale}")
   public ClientResponse<String> deleteTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale);

   @PUT
   @Path("{id}/translations/{locale}")
   public ClientResponse<String> putTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, TranslationsResource messageBody, @QueryParam("ext") StringSet extensions);
   
   @PUT
   @Path("{id}/translations/{locale}")
   public ClientResponse<String> putTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, TranslationsResource messageBody, @QueryParam("ext") StringSet extensions, @QueryParam("merge") String mergeType);

}
