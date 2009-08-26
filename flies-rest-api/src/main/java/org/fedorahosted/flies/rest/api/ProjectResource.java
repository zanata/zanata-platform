package org.fedorahosted.flies.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.openl10n.api.rest.project.ProjectRefs;

public interface ProjectResource {

	@Path("/{projectSlug}")
	public Object getProject(@PathParam("projectSlug") String projectSlug);
	
	@GET
	@Produces("application/xml")
	public ProjectRefs get();
	
}
