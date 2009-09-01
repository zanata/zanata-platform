package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.fedorahosted.flies.rest.dto.Resource;

public interface DocumentResource {

	@GET
	@Path("{documentId}")
	@Produces({ "application/flies.document+xml", "application/json" })
	public Document getDocument(@PathParam("documentId") String documentId,
			@QueryParam("includeTargets") String includeTargets);

	@POST
	@Path("{documentId}")
	@Consumes({ "application/flies.document+xml", "application/json" })
	public Response updateDocument(@PathParam("documentId") String documentId,
			Document document);
	
	@PUT
	@Path("{documentId}")
	@Consumes({ "application/flies.document+xml", "application/json" })
	public Response addDocument(@PathParam("documentId") String documentId,
			Document document);

	@GET
	@Path("")
	@Produces({ "application/flies.documents+xml", "application/json" })
	public Documents getDocuments();
	
	@GET
	@Path("{documentId}/resources/{resId}")
	@Produces({ "application/flies.document.resource+xml", "application/json" })
	public Resource getResource(
			@PathParam("documentId") String documentId,
			@PathParam("resId") String resId);
}
