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

	public Response get(ContentQualifier resources);

	public Response put(Document document) throws URISyntaxException;

	public Response getContent(ContentQualifier qualifier, int levels);

	public Response postContent(ResourceList content, ContentQualifier qualifier);

	public Response putContent(ResourceList content, ContentQualifier qualifier);

	public Response postContentByResourceId(DocumentResource resource,
			ContentQualifier qualifier,
			String resourceId);

	public Response getContentByResourceId(
			ContentQualifier qualifier,
			String resourceId,
			int levels);

}