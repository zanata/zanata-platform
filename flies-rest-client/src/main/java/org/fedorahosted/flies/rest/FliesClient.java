package org.fedorahosted.flies.rest;


import java.net.URI;
import java.net.URISyntaxException;

import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.client.IProjectsResource;

public class FliesClient {

	private final URI baseUri;
	private final String apiKey;
	private final FliesClientRequestFactory clientRequestFactory;
	
	public FliesClient(String baseUrl, String username, String apiKey) throws URISyntaxException{
		this.baseUri = new URI(baseUrl);
		this.apiKey = apiKey;
		clientRequestFactory = new FliesClientRequestFactory(username, apiKey);
	}
	
	public IProjectsResource getProjectsResource() {
	    return clientRequestFactory.getProjectsResource(baseUri);
	}
//		
//	public ProjectResource getProjectResource(String projectSlug) {
////		return clientRequestFactory.getProjectResource(baseUri);
//	    return getProjectsResource().getProject(projectSlug);
//	}
////	
//	public ProjectIterationResource getProjectIterationResource(String projectSlug, String iterationSlug){
////		URI resolvedUri = ProxyFactory.createUri(baseUri.toString() + "/projects/p/" + projectSlug + "/iterations");
////		return clientRequestFactory.getProjectIterationResource(resolvedUri);
//	    return getProjectResource(projectSlug).getIteration(iterationSlug);
//	}
////	
//	public DocumentsResource getDocumentResource(String projectSlug, String iterationSlug){
////		URI resolvedUri = ProxyFactory.createUri(baseUri.toString() + "/projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/documents");
////		return clientRequestFactory.getDocumentResource(resolvedUri); 
//	    return getProjectIterationResource(projectSlug, iterationSlug).getDocuments();
//	}
//	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public String getApiKey() {
		return apiKey;
	}
}
