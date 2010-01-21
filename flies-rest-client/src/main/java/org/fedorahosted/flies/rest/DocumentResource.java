package org.fedorahosted.flies.rest;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.client.IDocumentResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.jboss.resteasy.client.ClientResponse;

public class DocumentResource implements IDocumentResource{

	IDocumentResource documentResource;
	private final FliesClientRequestFactory requestFactory;
	private final URI uri;
	
	public DocumentResource(FliesClientRequestFactory requestFactory, IDocumentResource documentResource, URI uri) {
		this.requestFactory = requestFactory;
		this.documentResource = documentResource;
		this.uri = uri;
	}
	
	
	@Override
	public ClientResponse<Document> get(ContentQualifier resources) {
		return documentResource.get(resources);
	}

	@Override
	public Response put(Document document) {
		return documentResource.put(document);
	}
	

}
