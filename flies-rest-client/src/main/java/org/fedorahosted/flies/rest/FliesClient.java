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
	
	public ProjectsResource getProjectsResource() {
		IProjectsResource projectsResource = clientRequestFactory.getProjectsResource(baseUri);
		return new ProjectsResource(clientRequestFactory, projectsResource, baseUri);
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public String getApiKey() {
		return apiKey;
	}
}
