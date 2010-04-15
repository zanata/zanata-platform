package org.fedorahosted.flies.rest.service;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Project;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

@Name("projectService")
@Path("/projects/p/{projectSlug}")
public class ProjectService {

	@PathParam("projectSlug")
	String projectSlug;

	@In("projectServiceActionImpl")
	ProjectServiceAction impl;
	
	@GET
	@Produces({ 
		MediaTypes.APPLICATION_FLIES_PROJECT_XML, 
		MediaTypes.APPLICATION_FLIES_PROJECT_JSON,
		MediaType.APPLICATION_JSON})
	public Response get() {
		return impl.get(projectSlug);
	}
	
	@PUT
	@Consumes({ 
		MediaTypes.APPLICATION_FLIES_PROJECT_XML, 
		MediaTypes.APPLICATION_FLIES_PROJECT_JSON,
		MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(Project project) {
		return impl.put(project);
	}
}
