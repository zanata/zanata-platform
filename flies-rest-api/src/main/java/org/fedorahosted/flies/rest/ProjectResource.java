package org.fedorahosted.flies.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationRefs;
import org.fedorahosted.flies.rest.dto.ProjectRefs;

public interface ProjectResource {

	@GET
	@Path("/{projectSlug}")
	@Produces({ "application/flies.project+xml", "application/json" })
	public Project getProject(@PathParam("projectSlug") String projectSlug);

	@POST
	@Path("/{projectSlug}")
	@Consumes( { "application/flies.project+xml", "application/json" })
	public Response updateProject(@PathParam("projectSlug") String projectSlug, Project project);

	@PUT
	@Path("/{projectSlug}")
	@Consumes( { "application/flies.project+xml", "application/json" })
	public Response addProject(@PathParam("projectSlug") String projectSlug, Project project);
	
	@Path("/{projectSlug}/iteration")
	public ProjectIterationResource getProjectIterationResource(
			@PathParam("projectSlug") String projectSlug);

	@GET
	@Path("")
	@Produces({ "application/flies.projects+xml", "application/json" })
	public ProjectRefs getProjects();
	
}
