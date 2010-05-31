package org.fedorahosted.flies.rest.client;

import java.util.Set;

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

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.LanguageQualifier;
import org.fedorahosted.flies.rest.StringSet;
import org.fedorahosted.flies.rest.dto.v1.MultiTargetTextFlowList;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.TranslationResource;
import org.jboss.resteasy.client.ClientResponse;


@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface ITranslationResources {

	@GET
	public ClientResponse<ResourcesList> get();
	
	@POST
	public ClientResponse<String> post(
			SourceResource messageBody, 
			@QueryParam("ext") StringSet extensions);

	@GET
	@Path("/r/{id}")
	public ClientResponse<SourceResource> getResource(
			@PathParam("id") String id,
			@QueryParam("ext") StringSet extensions);
	
	@PUT
	@Path("/r/{id}")
	public ClientResponse<String> putResource(
			@PathParam("id") String id, SourceResource messageBody);
	
	@DELETE
	@Path("/r/{id}")
	public ClientResponse<String> deleteResource(
			@PathParam("id") String id);
	
	@GET
	@Path("/r/{id}/meta")
	public ClientResponse<TranslationResource> getResourceMeta(
			@PathParam("id") String id);
	
	@PUT
	@Path("/r/{id}/meta")
	public ClientResponse<String> putResourceMeta(
			@PathParam("id") String id, TranslationResource messageBody);

	@GET
	@Path("/r/{id}/translations/{locales}")
	public ClientResponse<MultiTargetTextFlowList> getTranslations(
			@PathParam("id") String id, 
			@PathParam("locales") LanguageQualifier locales, 
			@QueryParam("ext") StringSet extensions);
	
	@PUT
	@Path("/r/{id}/target/{locale}")
	public ClientResponse<String> putTranslations(
			@PathParam("id") String id, 
			@PathParam("locale") Set<LocaleId> locales);
	
	@GET
	@Path("/r/{id}/translation-as-source/{locale}")
	public ClientResponse<String> getTranslationAsSource(
			@PathParam("id") String id, 
			@PathParam("locale") LocaleId locale);
	
}
