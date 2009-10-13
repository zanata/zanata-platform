package org.fedorahosted.flies.rest;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.client.IDocumentResource;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.Documents;
import org.jboss.resteasy.client.ClientResponse;

public class DocumentsResource implements IDocumentsResource{

	IDocumentsResource documentsResource;
	private final FliesClientRequestFactory requestFactory;
	private final URI uri;
	
	public DocumentsResource(FliesClientRequestFactory requestFactory, IDocumentsResource documentsResource, URI uri) {
		this.requestFactory = requestFactory;
		this.documentsResource = documentsResource;
		this.uri = uri;
	}
	
	public DocumentResource getDocumentResource(String documentId) {
		URI uri = this.uri.resolve("d/"+documentId);
		IDocumentResource documentResource = requestFactory.getDocumentResource(uri);
		return new DocumentResource(requestFactory, documentResource, uri);
	}

	@Override
	public ClientResponse<Documents> getDocuments() {
		return documentsResource.getDocuments();
	}

	@Override
	public ClientResponse post(Documents documents) {
		return documentsResource.post(documents);
	}

	@Override
	public ClientResponse put(Documents documents) {
		return documentsResource.put(documents);
	}
	

}
