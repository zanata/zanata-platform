package org.fedorahosted.flies.rest.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.ProjectRef;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("projectsService")
@Path("/projects")
public class ProjectsService {

	@In 
	ProjectsServiceAction projectsServiceAction;
	
	@GET
	@Produces({ 
		MediaTypes.APPLICATION_FLIES_PROJECTS_XML,
		MediaTypes.APPLICATION_FLIES_PROJECTS_JSON,
		MediaType.APPLICATION_JSON })
	@Wrapped(element="projects", namespace=Namespaces.FLIES)
	public List<ProjectRef> get() {

		return projectsServiceAction.get();
	}
	
}
