package org.fedorahosted.flies.rest.service;

import java.net.URISyntaxException;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.ResourceList;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

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


	public Response get(ContentQualifier resources) {
		return impl.get(resources);
	}

	public Response getContent(ContentQualifier qualifier, int levels) {
		return impl.getContent(qualifier, levels);
	}

	public Response getContentByResourceId(ContentQualifier qualifier,
			String resourceId, int levels) {
		return impl.getContentByResourceId(qualifier, resourceId, levels);
	}

	public Response postContent(ResourceList content, ContentQualifier qualifier) {
		return impl.postContent(content, qualifier);
	}

	public Response postContentByResourceId(DocumentResource resource,
			ContentQualifier qualifier, String resourceId) {
		return impl.postContentByResourceId(resource, qualifier, resourceId);
	}

	public Response put(Document document) throws URISyntaxException {
		return impl.put(document);
	}

	public Response putContent(ResourceList content, ContentQualifier qualifier) {
		return impl.putContent(content, qualifier);
	}



}
