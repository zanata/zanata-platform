package net.openl10n.flies.rest.client;

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

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.jboss.resteasy.client.ClientResponse;

@Produces({ MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_XML })
public interface ITranslationResources
{

   @GET
   public ClientResponse<List<ResourceMeta>> get();

   @POST
   public ClientResponse<String> post(Resource messageBody, @QueryParam("ext") StringSet extensions);

   @GET
   @Path("{id}")
   public ClientResponse<Resource> getResource(@PathParam("id") String id, @QueryParam("ext") StringSet extensions);

   @PUT
   @Path("{id}")
   public ClientResponse<String> putResource(@PathParam("id") String id, Resource messageBody);

   @DELETE
   @Path("{id}")
   public ClientResponse<String> deleteResource(@PathParam("id") String id);

   @GET
   @Path("{id}/meta")
   public ClientResponse<ResourceMeta> getResourceMeta(@PathParam("id") String id);

   @PUT
   @Path("{id}/meta")
   public ClientResponse<String> putResourceMeta(@PathParam("id") String id, ResourceMeta messageBody);

   @GET
   @Path("{id}/translations/{locale}")
   public ClientResponse<TranslationsResource> getTranslations(@PathParam("id") String id, @PathParam("locale") LocaleId locale);

   @DELETE
   @Path("{id}/translations/{locale}")
   public ClientResponse<String> deleteTranslations(@PathParam("id") String id, @PathParam("locale") LocaleId locale);

   @PUT
   @Path("{id}/translations/{locale}")
   public ClientResponse<String> putTranslations(@PathParam("id") String id, @PathParam("locale") LocaleId locale, TranslationsResource messageBody);

}
