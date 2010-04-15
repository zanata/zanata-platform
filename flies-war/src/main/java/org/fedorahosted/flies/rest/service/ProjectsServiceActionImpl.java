package org.fedorahosted.flies.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.rest.dto.Project;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("projectsServiceAction")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class ProjectsServiceActionImpl implements ProjectsServiceAction{

	@In
	Session session;

	public List<Project> get() {
		
		List<HProject> projects = session.createQuery("from HProject p").list();

		List<Project> projectRefs = new ArrayList<Project>(projects.size());
		
		for(HProject hProject : projects){
			Project project = 
				new Project(hProject.getSlug(), hProject.getName(), hProject.getDescription());
			projectRefs.add( project );
		}
		
		return projectRefs;

	}
}
