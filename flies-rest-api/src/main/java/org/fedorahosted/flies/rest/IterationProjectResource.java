package org.fedorahosted.flies.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

public interface IterationProjectResource {
	
	@Path("{iterationSlug}")
	public Object getProjectIteration(@PathParam("iterationSlug") String iterationSlug);
	
	@GET
	@Produces("text/plain")
	public String get();
	
}
