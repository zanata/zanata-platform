package org.fedorahosted.flies.rest.service;

import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.ResourceList;
import org.jboss.seam.annotations.security.Restrict;

public interface DocumentServiceAction {

	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_DOCUMENT_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON})
	public Response get(
			@QueryParam("resources") @DefaultValue("") ContentQualifier resources);

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENT_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON})
	@Restrict("#{identity.loggedIn}")
	public Response put(Document document) throws URISyntaxException;

	@GET
	@Path("content/{qualifier}")
	public Response getContent(
			@PathParam("qualifier") ContentQualifier qualifier,
			@QueryParam("levels") @DefaultValue("1") int levels);

	@POST
	@Path("content/{qualifier}")
	@Restrict("#{identity.loggedIn}")
	public Response postContent(ResourceList content,
			@PathParam("qualifier") ContentQualifier qualifier);

	@PUT
	@Path("content/{qualifier}")
	public Response putContent(ResourceList content,
			@PathParam("qualifier") ContentQualifier qualifier);

	@POST
	@Path("content/{qualifier}/{resourceId}")
	@Restrict("#{identity.loggedIn}")
	public Response postContentByResourceId(DocumentResource resource,
			@PathParam("qualifier") ContentQualifier qualifier,
			@PathParam("resourceId") String resourceId);

	@GET
	@Path("content/{qualifier}/{resourceId}")
	public Response getContentByResourceId(
			@PathParam("qualifier") ContentQualifier qualifier,
			@PathParam("resourceId") String resourceId,
			@QueryParam("levels") @DefaultValue("1") int levels);

}