package org.fedorahosted.flies.client.pseudo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.ProjectsResource;
import org.fedorahosted.flies.rest.client.IDocumentsResource;

public class Pseudo {

	private final String baseUri;
	private final String project;
	private final String iteration;
	private final String user;
	private final String apiKey;

	public static void main(String[] args) throws Exception {
		System.out.println("Pseudo: "+Arrays.asList(args));
		new Pseudo().run();
	}
	
	public Pseudo() {
		baseUri = System.getProperty("flies.baseUri");
		project = System.getProperty("flies.project");
		iteration = System.getProperty("flies.iteration");
		user = System.getProperty("flies.user");
		apiKey = System.getProperty("flies.apiKey");
	}

	private void run() throws Exception {
		System.out.printf("url:%s proj:%s iter:%s user:%s key:%s\n", baseUri, project, iteration, user, apiKey);

		FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
		URI uri = new URI(baseUri);
//		URI docsUri = getDocsUri();
		new ProjectsResource(factory, factory.getProjectsResource(uri), baseUri);
		factory.getProjectsResource(new URI(baseUri));
//		IDocumentsResource documentsResource = factory.getDocumentsResource(docsUri);
//		System.out.println(docsUri);
//		Response response = documentsResource.put(docs);
//		Utility.checkResult(response, dstURL);

		
	}
	
	private URI getDocsUri() throws URISyntaxException {
		return new URI(baseUri).resolve("projects/p/"+project+"/iterations/i/"+iteration+"/documents");
	}


}
