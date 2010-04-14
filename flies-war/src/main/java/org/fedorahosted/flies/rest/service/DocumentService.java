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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.ResourceList;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

@Name("documentService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/documents/d/{documentId}")
public class DocumentService implements DocumentServiceAction {
	
	@PathParam("projectSlug")
	private String projectSlug;
	
	@PathParam("iterationSlug")
	private String iterationSlug;

	@PathParam("documentId")
	private String documentId;
	
	@Context
	private UriInfo uri;
	
	@In("DocumentServiceActionImpl")
	private DocumentServiceAction impl;

	
	public String getDocumentId() {
		return documentId;
	}
	
	public String getIterationSlug() {
		return iterationSlug;
	}

	public String getProjectSlug() {
		return projectSlug;
	}
	
	public UriInfo getUri() {
		return uri;
	}


	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_DOCUMENT_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON})
	public Response get(
			@QueryParam("resources") @DefaultValue("") ContentQualifier resources) {
		return impl.get(resources);
	}

	@GET
	@Path("content/{qualifier}")
	public Response getContent(
			@PathParam("qualifier") ContentQualifier qualifier, 
			@QueryParam("levels") @DefaultValue("1") int levels) {
		return impl.getContent(qualifier, levels);
	}

	@GET
	@Path("content/{qualifier}/{resourceId}")
	public Response getContentByResourceId(
			@PathParam("qualifier") ContentQualifier qualifier,
			@PathParam("resourceId") String resourceId, 
			@QueryParam("levels") @DefaultValue("1") int levels) {
		return impl.getContentByResourceId(qualifier, resourceId, levels);
	}

	@POST
	@Path("content/{qualifier}")
	@Restrict("#{identity.loggedIn}")
	public Response postContent(ResourceList content, @PathParam("qualifier") ContentQualifier qualifier) {
		return impl.postContent(content, qualifier);
	}

	@POST
	@Path("content/{qualifier}/{resourceId}")
	@Restrict("#{identity.loggedIn}")
	public Response postContentByResourceId(DocumentResource resource,
			@PathParam("qualifier") ContentQualifier qualifier, 
			@PathParam("resourceId") String resourceId) {
		return impl.postContentByResourceId(resource, qualifier, resourceId);
	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENT_XML,
			MediaTypes.APPLICATION_FLIES_DOCUMENT_JSON})
	@Restrict("#{identity.loggedIn}")
	public Response put(Document document) throws URISyntaxException {
		return impl.put(document);
	}

	@PUT
	@Path("content/{qualifier}")
	public Response putContent(ResourceList content, 
			@PathParam("qualifier") ContentQualifier qualifier) {
		return impl.putContent(content, qualifier);
	}



}
