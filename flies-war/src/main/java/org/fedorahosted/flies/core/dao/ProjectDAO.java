package org.fedorahosted.flies.core.dao;

import org.fedorahosted.flies.core.model.HProject;
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
	
	public HProject getBySlug(String slug){
		return (HProject) session.createCriteria(HProject.class)
			.add( Restrictions.naturalId()
		        .set("slug", slug)
		    	)
		    .setCacheable(true)
		    .setComment("ProjectDAO.getBySlug")
		    .uniqueResult();
	}
	
	public Integer getRevisionBySlug(String slug) {
		return (Integer) session.createQuery(
				"select p.versionNum from HProject p where slug =:slug")
				.setParameter("slug", slug)
				.uniqueResult();
	}
}
