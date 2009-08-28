package org.fedorahosted.flies.core.dao;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("projectDAO")
@AutoCreate
public class ProjectDAO {

	@In
	Session session;
	
	public Project getBySlug(String slug){
		return (Project) session.createCriteria(Project.class)
			.add( Restrictions.naturalId()
		        .set("slug", slug))
		    .setCacheable(true)
		    .uniqueResult();
	}
}
