package org.fedorahosted.flies.rest.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectRef;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("projectsService")
@Path("/projects")
public class ProjectsService {

	@In
	Session session;

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaType.APPLICATION_JSON })
	public ProjectRefs get() {
		ProjectRefs projectRefs = new ProjectRefs();
		
		List<HProject> projects = session.createQuery("from HProject p").list();
		
		for(HProject hProject : projects){
			Project project = 
				new Project(hProject.getSlug(), hProject.getName(), hProject.getDescription());
			projectRefs.getProjects().add( new ProjectRef( project ));
		}
		
		return projectRefs;
	}
}
