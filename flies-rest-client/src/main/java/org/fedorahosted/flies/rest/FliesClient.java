package org.fedorahosted.flies.rest;

import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.rest.projects.ProjectsClient;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClient {
	public static void main(String[] args) {
	    // this initialization only needs to be done once per VM
	    RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

	    ProjectsClient client = ProxyFactory.create(ProjectsClient.class, "http://localhost:8080/flies/seam/resource/restv1/");
	    ClientResponse<Feed> response = client.getProjects();
	    if(response.getStatus() != 200){
	    	System.err.println("woops");
	    }
	    else{
		    Feed feed = response.getEntity();
			System.out.println(feed.getTitle());
	    }
	    
	    ClientResponse<Entry> prResponse = client.getProject("seam-reference-guide", "bobx");
	    if(prResponse.getStatus() != 200){
	    	System.err.println("woops");
	    }
	    else{
		    Entry e = prResponse.getEntity();
			System.out.println(e.getTitle());
	    }
	    
	    
	    
	}
	
}
