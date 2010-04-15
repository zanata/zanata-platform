package org.fedorahosted.flies.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.rest.dto.ProjectRef;
import org.fedorahosted.flies.rest.dto.ProjectType;
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

	public List<ProjectRef> get() {
		
		List<HProject> projects = session.createQuery("from HProject p").list();

		List<ProjectRef> projectRefs = new ArrayList<ProjectRef>(projects.size());
		
		for(HProject hProject : projects){
			ProjectRef project = 
				new ProjectRef(hProject.getSlug(), hProject.getName(), hProject.getDescription(), ProjectType.IterationProject);
			projectRefs.add( project );
		}
		
		return projectRefs;

	}
}
