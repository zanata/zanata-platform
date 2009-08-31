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

import org.fedorahosted.flies.rest.dto.Project;

public interface ProjectIterationResource{

	@GET
	@Produces({ "application/flies.project+xml", "application/json" })
	public Project get(
			@QueryParam("ext") @DefaultValue("") String extensions
			);

	@POST
	@Consumes( { "application/flies.project+xml", "application/json" })
	public Response post(Project project);

	@PUT
	@Consumes( { "application/flies.project+xml", "application/json" })
	public Response put(Project project);

	@Path("documents/{documentId}")
	public DocumentResource getDocument(String documentId);

}
