package org.fedorahosted.flies.client.pseudo;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.fedorahosted.flies.rest.DocumentsResource;
import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.ProjectIterationResource;
import org.fedorahosted.flies.rest.ProjectsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;

public class Pseudo {

	private final String baseUri;
	private final String projectSlug;
	private final String iterationSlug;
	private final String user;
	private final String apiKey;

	public static void main(String[] args) throws Exception {
		System.out.println("Pseudo: "+Arrays.asList(args));
		new Pseudo().run();
	}
	
	public Pseudo() {
		baseUri = System.getProperty("flies.baseUri");
		projectSlug = System.getProperty("flies.project");
		iterationSlug = System.getProperty("flies.iteration");
		user = System.getProperty("flies.user");
		apiKey = System.getProperty("flies.apiKey");
	}

	private void run() throws Exception {
		System.out.printf("url:%s proj:%s iter:%s user:%s key:%s\n", baseUri, projectSlug, iterationSlug, user, apiKey);

		FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
		URI uri = new URI(baseUri);
		ProjectsResource projectsResource = new ProjectsResource(factory, factory.getProjectsResource(uri), uri);
		ProjectIterationResource iterationResource = projectsResource.getProjectResource(projectSlug).getIterationResource(iterationSlug);
		ClientResponse<ProjectIteration> clientResponse = iterationResource.get();
		List<Document> documents = clientResponse.getEntity().getDocuments();
		
		System.out.println(documents);
		
//		DocumentsResource documentsResource = iterationResource.getDocumentsResource();
//		documentsResource.get
		
//		Response response = documentsResource.put(docs);
//		Utility.checkResult(response, dstURL);
		
	}
	
}
