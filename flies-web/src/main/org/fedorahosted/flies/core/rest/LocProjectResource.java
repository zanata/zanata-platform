package org.fedorahosted.flies.core.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import net.openl10n.api.rest.project.Project;
import net.openl10n.packaging.jpa.project.HProject;

public interface LocProjectResource {
	
	@GET
	@Produces({"application/openl10n.project+xml", "application/json"})
	public Project get(@QueryParam("ext") @DefaultValue("") String extensions);

	@POST
	@Consumes({"application/openl10n.project+xml"})
	public Response post(Project project);
	
	@PUT
	@Consumes({"application/openl10n.project+xml"})
	public Response put(Project project);
	
	@Path("documents/{documentId}")
	public DocumentResource getDocument(String documentId);
	
}
