package org.fedorahosted.flies.dao;

import java.util.List;

import javax.ws.rs.core.EntityTag;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.model.HProject;
import org.fedorahosted.flies.util.HashUtil;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("projectDAO")
@AutoCreate
public class ProjectDAO extends AbstractDAOImpl<HProject, Long>{

	public ProjectDAO() {
		super(HProject.class);
	}
	
	public ProjectDAO(Session session) {
		super(HProject.class, session);
	}
	
	public HProject getBySlug(String slug){
		return (HProject) getSession().createCriteria(HProject.class)
			.add( Restrictions.naturalId()
		        .set("slug", slug)
		    	)
		    .setCacheable(true)
		    .setComment("ProjectDAO.getBySlug")
		    .uniqueResult();
	}
	
	/**
	 * Retrieves the ETag for the Project
	 * 
	 * This algorithm takes into account changes in Project Iterations as well.
	 * 
	 * @param slug Project slug
	 * @return calculated EntityTag or null if project does not exist
	 */
	public EntityTag getETag(String slug) {
		Integer projectVersion = (Integer) getSession().createQuery(
		"select p.versionNum from HProject p where slug =:slug")
		.setParameter("slug", slug)
		.uniqueResult();
		
		if(projectVersion == null)
			return null;
		
		List<Integer> iterationVersions =  getSession().createQuery(
		"select i.versionNum from HProjectIteration i where i.project.slug =:slug")
		.setParameter("slug", slug).list();

		String hash = HashUtil.generateHash(projectVersion + ':' + StringUtils.join(iterationVersions, ':'));
		
		return EntityTag.valueOf( hash );
	}
	
}
