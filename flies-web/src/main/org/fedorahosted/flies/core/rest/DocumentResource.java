package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.openl10n.api.rest.document.Document;
import net.openl10n.api.rest.document.Resource;

public interface DocumentResource {

	@GET
	@Produces("application/openl10n.document+xml")
	public Document get(@QueryParam("includeTargets") String includeTargets);

	@GET
	@Produces("application/openl10n.document.resource+xml")
	@Path("resources/{resId}")
	public Resource getResource(@PathParam("resId") String resId);
}
