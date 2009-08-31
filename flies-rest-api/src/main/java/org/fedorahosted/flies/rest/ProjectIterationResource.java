package org.fedorahosted.flies.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.dto.ProjectIteration;

public interface ProjectIterationResource{

	@GET
	@Produces({ "application/flies.project.iteration+xml", "application/json" })
	public ProjectIteration get(
			@QueryParam("ext") @DefaultValue("") String extensions
			);

	@POST
	@Consumes( { "application/flies.project.iteration+xml", "application/json" })
	public Response post(ProjectIteration project);

	@PUT
	@Consumes( { "application/flies.project.iteration+xml", "application/json" })
	public Response put(ProjectIteration project);

	@Path("documents/{documentId}")
	public DocumentResource getDocument(String documentId);

}
