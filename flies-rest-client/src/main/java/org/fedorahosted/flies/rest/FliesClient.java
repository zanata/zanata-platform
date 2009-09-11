package org.fedorahosted.flies.rest;


import java.net.URI;
import java.net.URISyntaxException;

import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClient {

	private final URI baseUri;
	private final String apiKey;
	private final FliesClientRequestFactory clientRequestFactory;
	
	public FliesClient(String baseUrl, String apiKey) throws URISyntaxException{
		this.baseUri = new URI(baseUrl);
		this.apiKey = apiKey;
		clientRequestFactory = initializeRequests(apiKey);
	}
		
	private static FliesClientRequestFactory initializeRequests(String apiKey)
	{
	  ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
	  RegisterBuiltin.register(instance);

	  FliesClientRequestFactory clientRequestFactory = new FliesClientRequestFactory(apiKey);
	  return clientRequestFactory;
	}
	
	public ProjectResource getProjectResource(){
		return clientRequestFactory.getProjectResource(baseUri);
	}
	
	public ProjectIterationResource getProjectIterationResource(String projectSlug){
		URI resolvedUri = ProxyFactory.createUri(baseUri.toString() + "/projects/p/" + projectSlug + "/iterations");
		return clientRequestFactory.getProjectIterationResource(resolvedUri);
	}
	
	public DocumentResource getDocumentResource(String projectSlug, String iterationSlug){
		URI resolvedUri = ProxyFactory.createUri(baseUri.toString() + "/projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/documents");
		return clientRequestFactory.getDocumentResource(resolvedUri); 
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public String getApiKey() {
		return apiKey;
	}
}
