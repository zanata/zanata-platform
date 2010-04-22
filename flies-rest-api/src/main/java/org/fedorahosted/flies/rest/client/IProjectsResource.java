package org.fedorahosted.flies.rest.client;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.ProjectInline;
import org.jboss.resteasy.client.ClientResponse;

@Path("/projects")
public interface IProjectsResource {

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaTypes.APPLICATION_FLIES_PROJECTS_JSON })
	public ClientResponse<List<ProjectInline>> get();
	
}
