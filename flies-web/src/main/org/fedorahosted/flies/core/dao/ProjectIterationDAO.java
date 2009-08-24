package org.fedorahosted.flies.core.dao;

import java.util.List;

import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.core.model.StatusCount;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("projectIterationDAO")
@AutoCreate
public class ProjectIterationDAO {
	
	@In
	Session session;
	
	@Logger
	Log log;

	@In
	ProjectDAO projectDAO;
	
	public ProjectIteration getBySlug(String projectSlug, String iterationSlug){
		return (ProjectIteration) session.createCriteria(ProjectIteration.class)
		.add( Restrictions.naturalId()
	        .set("project", projectDAO.getBySlug( projectSlug ) )
	        .set("slug", iterationSlug )
	    )
		.setCacheable(true).uniqueResult();
	}

	public ProjectIteration getBySlug(IterationProject project, String iterationSlug){
		return (ProjectIteration) session.createCriteria(ProjectIteration.class)
		.add( Restrictions.naturalId()
	        .set("project", project )
	        .set("slug", iterationSlug )
	    )
		.setCacheable(true).uniqueResult();
	}
	
	public TranslationStatistics getStatisticsForIteration(Long iterationId, String localeId){
		if(true)
			return new TranslationStatistics(1l, 1l, 1l, 1l);
		List<StatusCount> stats = session.createQuery(
				"select new org.fedorahosted.flies.core.model.StatusCount(pt.status, count(pt)) " +
				"from TextUnitTarget pt " +
				"where pt.document.projectIteration.id = :id " +
				"  and pt.locale.id = :localeId "+  
				"group by pt.status"
			)
			.setParameter("id", iterationId)
			.setParameter("localeId", localeId)
			.setCacheable(true)
			.list();
		
		TranslationStatistics stat = new TranslationStatistics();
		for(StatusCount count: stats){
			stat.set(count.status, count.count);
		}
		
		return stat;
	}
}
