package org.fedorahosted.flies.rest.service;

import java.util.List;

import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectInline;
import org.fedorahosted.flies.rest.dto.ProjectInlineList;
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

	public ProjectInlineList get() {
		ProjectInlineList projectRefs = new ProjectInlineList();
		
		List<HProject> projects = session.createQuery("from HProject p").list();
		
		for(HProject hProject : projects){
			Project project = 
				new Project(hProject.getSlug(), hProject.getName(), hProject.getDescription());
			projectRefs.getProjects().add( new ProjectInline( project ));
		}
		
		return projectRefs;

	}
}
