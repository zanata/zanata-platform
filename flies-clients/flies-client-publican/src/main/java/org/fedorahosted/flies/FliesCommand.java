package org.fedorahosted.flies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.adapter.po.PoReader;
import org.fedorahosted.flies.rest.FliesClient;
import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.client.IDocumentResource;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;
import org.xml.sax.InputSource;

public class FliesCommand {

	public static Properties createProperties(){
		Properties properties = new Properties();
		properties.setProperty("baseurl", "http://localhost:8080/flies/seam/resource/restv1/");
		properties.setProperty("project", "sample-project");
		properties.setProperty("iteration", "1.0");
		properties.setProperty("account", "demo");
		properties.setProperty("apikey", "23456789012345678901234567890123");

		File f = new File("flies.conf");
		if(f.exists()){
			try{
				properties.load( new FileInputStream(f));
			}
			catch(IOException e){
				System.err.println("Error reading flies.conf: " + e.getMessage());
			}
		}
		else{
			try{
				System.out.println("No configuration found. Generating default flies.conf");
				properties.store(new FileOutputStream(f), "Sample Configuration");
			}
			catch (Exception e) {
				System.err.println("Error saving flies.conf: " + e.getMessage());
			}
		}
		
		return properties;
	}

	private final String baseurl;
	private final String account;
	private final String apiKey;
	private final String iteration;
	private final String project;

	private final String iterationUri;
	
	private final IProjectIterationResource iterationResource;
	private FliesClientRequestFactory clientRequestFactory;
	
	private IDocumentResource getDocumentResource(String docId){
		return clientRequestFactory.getDocumentResource(URI.create(iterationUri + "/documents/d/"+docId));
	}
	
	public FliesCommand(Properties properties) {
		this.baseurl = properties.getProperty("baseurl", "http://localhost:8080/flies/seam/resource/restv1/");
		this.account = properties.getProperty("account", "demo");
		this.apiKey = properties.getProperty("apikey", "23456789012345678901234567890123");
		this.iteration = properties.getProperty("iteration", "1.0");
		this.project = properties.getProperty("project", "sample-project");
		
		clientRequestFactory = new FliesClientRequestFactory(account, apiKey);
		this.iterationUri = baseurl + "projects/p/" + project + "/iterations/i/" + iteration;
	    iterationResource = clientRequestFactory.getProjectIterationResource(URI.create(iterationUri));
	    
	}

	public void putPO(File poFile){
		System.out.println("Attempting to publish " + poFile.getName());
		IDocumentResource docResource = getDocumentResource(poFile.getName());
		Document newDocument = new Document("/" + poFile.getName(),ContentType.TextPlain);
		PoReader poReader = new PoReader();
		poReader.extract(newDocument, new LocaleInputSourcePair(new InputSource(poFile.toURI().toString()), LocaleId.EN));
		ClientResponse<Document> response = docResource.get(ContentQualifier.NONE);

		if(response.getStatus() == Status.OK.getStatusCode()){
			Document oldDocument = response.getEntity();
			newDocument.setRevision(oldDocument.getRevision());
			Response res = docResource.put(newDocument);
			System.out.println(res.getStatus());
		}
		else{
			docResource.put(newDocument);
			Response res = docResource.put(newDocument);
			System.out.println(res.getStatus());
		}
		
	}

	public boolean printInfo(){
	    System.out.println("Retrieving data for project...");
	    ClientResponse<ProjectIteration> response = iterationResource.get();
	    if(response.getStatus() >399){
	    	System.err.println("Failed with code " + response.getStatus());
	    	return false;
	    }
	    System.out.println("Project: " + response.getEntity().getName());
	    System.out.println(response.getEntity().getSummary());
	    return true;
	}
	
	public static void main(String[] args) {
		FliesCommand command = new FliesCommand(createProperties());
		
		if(args.length != 2){
			System.err.println("Usage: flies publish POFILE");
			return;
		}/*
		if(!command.printInfo()){
			return;
		}*/
		if("publish".equals(args[0])){
			File poFile = new File(args[1]);
			if(!poFile.exists()){
				System.err.println("File does not exist");
				return;
			}
			command.putPO(poFile);
		}
		
	}
}
