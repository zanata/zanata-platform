package org.fedorahosted.flies.rest.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.ProjectInlineList;
import org.jboss.resteasy.client.ClientResponse;

@Path("/projects")
public interface IProjectsResource {

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaType.APPLICATION_JSON })
	public ClientResponse<ProjectInlineList> get();
	
//  @Path("/p/{projectSlug}")
//	public IProjectResource getProject(
//		@PathParam("projectSlug") String projectSlug);
}
