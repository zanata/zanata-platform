package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Feed;

public interface ProjectResource {

	@Path("/{projectSlug}")
	public Object getProject(@PathParam("projectSlug") String projectSlug);
	
	@GET
	@Produces("application/atom+xml")
	public Feed get();
	
}
