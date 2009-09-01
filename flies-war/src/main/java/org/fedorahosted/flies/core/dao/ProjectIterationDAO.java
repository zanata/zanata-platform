package org.fedorahosted.flies.core.dao;

import java.util.List;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.core.model.StatusCount;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
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
	
	public TranslationStatistics getStatisticsForContainer(Long containerId, LocaleId localeId){
		
		List<StatusCount> stats = session.createQuery(
				"select new org.fedorahosted.flies.core.model.StatusCount(tft.state, count(tft)) " +
				"from HTextFlowTarget tft " +
				"where tft.textFlow.document.project.id = :id " +
				"  and tft.locale = :locale "+  
				"group by tft.state"
			)
			.setParameter("id", containerId)
			.setParameter("locale", localeId)
			.setCacheable(true)
			.list();
		
		
		Long totalCount = (Long) session.createQuery("select count(tf) from HTextFlow tf where tf.document.project.id = :id")
			.setParameter("id", containerId)
			.setCacheable(true).uniqueResult();
		
		TranslationStatistics stat = new TranslationStatistics();
		for(StatusCount count: stats){
			stat.set(count.status, count.count);
		}
		
		stat.set(ContentState.New, totalCount - stat.getNotApproved());
		
		return stat;
	}
}
