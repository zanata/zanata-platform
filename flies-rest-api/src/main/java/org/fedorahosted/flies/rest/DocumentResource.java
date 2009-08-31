package org.fedorahosted.flies.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Resource;


public interface DocumentResource {

	@GET
	@Produces("application/flies.document+xml")
	public Document get(@QueryParam("includeTargets") String includeTargets);

	@GET
	@Produces("application/flies.document.resource+xml")
	@Path("resources/{resId}")
	public Resource getResource(@PathParam("resId") String resId);
}
