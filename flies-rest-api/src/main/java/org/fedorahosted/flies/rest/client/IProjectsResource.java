package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectList;
import org.jboss.resteasy.client.ClientResponse;

@Path("/projects")
public interface IProjectsResource {

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaType.APPLICATION_JSON })
	public ClientResponse<ProjectList> get();
	
	@POST
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public Response post(Project project);

	
//  @Path("/p/{projectSlug}")
//	public IProjectResource getProject(
//		@PathParam("projectSlug") String projectSlug);
}
