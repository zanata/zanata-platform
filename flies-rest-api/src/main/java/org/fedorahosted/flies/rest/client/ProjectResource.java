package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationRefs;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.MediaTypeHelper;

@Path("/projects")
public interface ProjectResource {
	
	@GET
	@Path("/p/{projectSlug}")
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public ClientResponse<Project> getProject(@PathParam("projectSlug") String projectSlug);

	@POST
	@Path("/p/{projectSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public Response updateProject(@PathParam("projectSlug") String projectSlug, Project project);

	@PUT
	@Path("/p/{projectSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public Response addProject(@PathParam("projectSlug") String projectSlug, Project project);
	
	@Path("/p/{projectSlug}/iterations/i/{iterationSlug}")
	public ProjectIterationResource getProjectIterationResource(
			@PathParam("projectSlug") String projectSlug,
			@PathParam("iterationSlug") String iterationSlug);

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaType.APPLICATION_JSON })
	public ClientResponse<ProjectRefs> getProjects();
	
}
