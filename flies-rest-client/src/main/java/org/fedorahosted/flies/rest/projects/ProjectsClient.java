package org.fedorahosted.flies.rest.projects;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

@Path("/project")
public interface ProjectsClient {

	@GET
	@Produces("application/atom+xml")
	public ClientResponse<Feed> getProjects();
	
	@GET
	@Path("/{projectSlug}")
	@Produces("application/atom+xml")
	public ClientResponse<Entry> getProject(@PathParam("projectSlug") String projectSlug, @HeaderParam("X-Auth-Token") String authToken);
	
}
