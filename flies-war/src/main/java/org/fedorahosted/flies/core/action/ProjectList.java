package org.fedorahosted.flies.core.action;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.fedorahosted.flies.core.model.Project;

@Name("projectList")
public class ProjectList extends EntityQuery<Project> {
	public ProjectList() {
		setEjbql("select project from Project project");
	}
}
