package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.ProjectRef;
import org.fedorahosted.flies.rest.dto.ProjectRes;
import org.fedorahosted.flies.rest.dto.ProjectType;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("projectsService")
@Path("/projects")
public class ProjectsService {

	@In
	Session session;

	@Logger
	Log log;

	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_PROJECTS_XML,
			MediaTypes.APPLICATION_FLIES_PROJECTS_JSON,
			MediaType.APPLICATION_JSON })
	@Wrapped(element = "projects", namespace = Namespaces.FLIES)
	public List<ProjectRef> get() {
		List<HProject> projects = session.createQuery("from HProject p").list();

		List<ProjectRef> projectRefs = new ArrayList<ProjectRef>(projects
				.size());

		for (HProject hProject : projects) {
			ProjectRef project = new ProjectRef(hProject.getSlug(), hProject
					.getName(), hProject.getDescription(),
					ProjectType.IterationProject);
			project.getLinks().add( 
					new Link(URI.create("p/"+hProject.getSlug()), "self", MediaTypes.APPLICATION_FLIES_PROJECT));
			projectRefs.add(project);
		}

		log.info("All still good hot deploying again...");

		return projectRefs;
	}

}
