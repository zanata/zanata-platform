package org.fedorahosted.flies.core.dao;

import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("projectContainerDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ProjectContainerDAO {

	@In Session session;
	
	public HProjectContainer getBySlug(String projectSlug, String iterationSlug){
		return (HProjectContainer) session.createQuery(
				"select it.container " +
				"from HProjectIteration it " +
				"where it.project.slug = :projectSlug " +
				"  and it.slug = :iterationSlug"
				)
				.setParameter("projectSlug", projectSlug)
				.setParameter("iterationSlug", iterationSlug)
				.uniqueResult();
		
	}
}
