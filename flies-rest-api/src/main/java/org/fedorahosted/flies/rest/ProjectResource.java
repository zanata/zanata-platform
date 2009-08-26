package org.fedorahosted.flies.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.openl10n.api.rest.project.ProjectRefs;

public interface ProjectResource {

	/**
	 * This method will pass control on to the specific type of project
	 * being queried for. e.g. for an IterationProject, it will pass control
	 * on to IterationProjectResource
	 *  
	 * @param projectSlug textual id of project
	 * 
	 * @return a SubResource based on the type of project 
	 */
	@Path("/{projectSlug}")
	public Object getProject(@PathParam("projectSlug") String projectSlug);

	/**
	 * Retrieve a list of projects
	 *  
	 * @return list of Project references
	 */
	@GET
	@Produces("application/xml")
	public ProjectRefs get();
	
}
