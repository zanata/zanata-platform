package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.openl10n.api.rest.project.Project;

public interface LocProjectResource {
	
	@GET
	@Produces("application/openl10n.project+xml")
	public Project get(@QueryParam("ext") String extensions);

	@Path("documents/{documentId}")
	public DocumentResource getDocument(String documentId);
	
}
