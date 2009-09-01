package org.fedorahosted.flies.rest;


import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
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
		String resolvedUri = baseUri.toString() + "/projects/p/" + projectSlug + "/iterations";
		return clientRequestFactory.createProxy(ProjectIterationResource.class, resolvedUri);
	}
	
	public DocumentResource getDocumentResource(String projectSlug, String iterationSlug){
		return clientRequestFactory.createProxy(DocumentResource.class, 
				baseUri.toString() + "projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/documents");
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public String getApiKey() {
		return apiKey;
	}

	public static void main(String[] args) throws URISyntaxException {
		FliesClient client = new FliesClient("http://localhost:8080/flies/seam/resource/restv1", "bob");
		
		ProjectResource projectResource = client.getProjectResource();
		
		ClientResponse<Project> projectResponse = projectResource.getProject("sample-project");
		
		if (projectResponse.getResponseStatus().getStatusCode() < 399) {
			Project p = projectResponse.getEntity();
			System.out.println( p.getName() );
			p.getIterations().clear();
			p.setName( "replaced "+ p.getName());
			Response r = projectResource.updateProject("myproject", p);
			System.out.println("Completed with status: " + r.getStatus());
			
			for (int i = 1; i < 100; i++) {
				p = new Project("myxproject-"+i, "Project #"+i, "Sample Description #"+i);
				r = projectResource.addProject(p);
				Status s = Status.fromStatusCode(r.getStatus());
				if(Status.CREATED == s ) {
					System.out.println("Created project " + i);
				}
				else{
					System.err.println(i + "Failed with status: " + s);
				}
				
				ProjectIterationResource projectIterationResource = 
					client.getProjectIterationResource(p.getId());
				for (int j = 1; j < 6; j++) {
					ProjectIteration pIt = new ProjectIteration();
					pIt.setId("iteration-"+j);
					pIt.setName("Project Iteration #"+j);
					pIt.setSummary("A sample Iteration #"+j);
					r = projectIterationResource.addIteration(pIt);
					s = Status.fromStatusCode(r.getStatus());
					if(Status.CREATED == s ) {
						System.out.println("  Iteration Created: " + j);
					}
					else{
						System.err.println("  " + j + " Iteration Creation Failed with status: " + s);
					}
					
				}
			}
		}
		
		DocumentResource documentResource = client.getDocumentResource("myproject", "myiteration");
		//r = documentResource.addDocument("myid", document);
		
	}
}
