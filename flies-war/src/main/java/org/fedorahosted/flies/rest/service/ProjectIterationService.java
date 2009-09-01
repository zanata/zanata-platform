package org.fedorahosted.flies.rest.service;

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
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.annotations.Name;

@Name("projectIterationService")
@Path("/projects/p/{projectSlug}/iterations")
public class ProjectIterationService {

	@PathParam("projectSlug")
	private String projectSlug;

	@GET
	@Path("/i/{iterationSlug}")
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public ProjectIteration getIteration (
			@PathParam("iterationSlug") String iterationSlug){
		return new ProjectIteration("myid+" +iterationSlug, "myname");
	}

	@POST
	@Path("/i/{iterationSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public Response updateIteration(
			@PathParam("iterationSlug") String iterationSlug,
			ProjectIteration project){
		return null;
	}

	@PUT
	@Path("/i/{iterationSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public Response addIteration(
			@PathParam("iterationSlug") String iterationSlug,
			ProjectIteration project){
		return null;
	}
}
