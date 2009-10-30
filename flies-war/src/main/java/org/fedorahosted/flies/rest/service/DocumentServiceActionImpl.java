package org.fedorahosted.flies.rest.service;

import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.ResourceList;

public class DocumentServiceActionImpl implements DocumentServiceAction {

	@Override
	public Response get(ContentQualifier resources) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getContent(ContentQualifier qualifier, int levels) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getContentByResourceId(ContentQualifier qualifier,
			String resourceId, int levels) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response postContent(ResourceList content, ContentQualifier qualifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response postContentByResourceId(DocumentResource resource,
			ContentQualifier qualifier, String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response put(Document document) throws URISyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response putContent(ResourceList content, ContentQualifier qualifier) {
		// TODO Auto-generated method stub
		return null;
	}

}
