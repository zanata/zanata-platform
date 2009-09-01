package org.fedorahosted.flies.rest;


import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.sun.jndi.toolkit.url.Uri;

public class FliesClient {

	private final URI baseUri;
	private final String apiKey;
	private final ClientRequestFactory clientRequestFactory;
	
	public FliesClient(String baseUrl, String apiKey) throws URISyntaxException{
		this.baseUri = new URI(baseUrl);
		this.apiKey = apiKey;
		clientRequestFactory = initializeRequests(baseUri, apiKey);
	}
		
	private static ClientRequestFactory initializeRequests(URI baseUri, String apiKey) throws URISyntaxException
	{
	  ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
	  RegisterBuiltin.register(instance);

	  ClientRequestFactory clientRequestFactory = new ClientRequestFactory();
	  clientRequestFactory.getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(apiKey));
	  clientRequestFactory.setBase(baseUri);
	  return clientRequestFactory;
	}
	
	public ProjectResource getProjectResource(){
		return clientRequestFactory.createProxy(ProjectResource.class, baseUri);
	}
	
	public ProjectIterationResource getProjectIterationResource(String projectSlug){
		return clientRequestFactory.createProxy(ProjectIterationResource.class, baseUri.toString() + "projects/p/" + projectSlug + "/iterations");
	}
	
	public DocumentResource getDocumentResource(String projectSlug, String iterationSlug){
		return clientRequestFactory.createProxy(DocumentResource.class, baseUri.toString() + "projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/documents");
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public String getApiKey() {
		return apiKey;
	}

	public static void main(String[] args) throws URISyntaxException {
		FliesClient client = new FliesClient("http://localhost:8080/flies/seam/resource/restv1", "bob");
		
		ClientResponse<Project> projectResponse = client.getProjectResource().getProject("myproject");
		
		if (projectResponse.getResponseStatus().getStatusCode() < 399) {
			Project p = projectResponse.getEntity();
			System.out.println( p.getName() );
			p.setName( "replaced "+ p.getName());
			Response r = client.getProjectResource().updateProject("myproject", p);
			System.out.println("Completed with status: " + r.getStatus());
		}
		
	}
}
