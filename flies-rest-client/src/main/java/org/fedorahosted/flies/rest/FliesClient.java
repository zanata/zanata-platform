package org.fedorahosted.flies.rest;


import org.fedorahosted.flies.rest.client.ProjectResource;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClient {
	public static void main(String[] args) {
	    // this initialization only needs to be done once per VM
	    RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

	    ProjectResource client = ProxyFactory.create(ProjectResource.class, "http://localhost:8080/flies/seam/resource/restv1/");
	    
	    
	}
	
}
