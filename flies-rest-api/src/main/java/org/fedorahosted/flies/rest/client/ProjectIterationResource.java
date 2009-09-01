package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.jboss.resteasy.client.ClientResponse;

public interface ProjectIterationResource{

	@GET
	@Path("/i/{iterationSlug}")
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public ClientResponse<ProjectIteration> getIteration(
			@PathParam("iterationSlug") String iterationSlug);

	@POST
	@Path("/i/{iterationSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public Response updateIteration(
			@PathParam("iterationSlug") String iterationSlug,
			ProjectIteration project);

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaType.APPLICATION_JSON })
	public Response addIteration(
			ProjectIteration project);
/*
	@Path("/i/{iterationSlug}/documents")
	public DocumentResource getDocumentResource(
			@PathParam("iterationSlug") String iterationSlug);
*/
}
