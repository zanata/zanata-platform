package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

public interface ProjectIterationResource {

	@Produces("text/plain")
	@GET
	public String get();

}
