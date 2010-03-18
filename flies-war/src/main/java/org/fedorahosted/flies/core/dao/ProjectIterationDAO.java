package org.fedorahosted.flies.core.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.core.model.StatusCount;
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
	EntityManager entityManager;

	@In
	Session session;
	
	@Logger
	Log log;

	@In
	ProjectDAO projectDAO;
	
	public HProjectIteration getById(long projectIterationId){
		return entityManager.find(HProjectIteration.class, projectIterationId);
	}
	
	public HProjectIteration getBySlug(String projectSlug, String iterationSlug){
		return (HProjectIteration) session.createCriteria(HProjectIteration.class)
		.add( Restrictions.naturalId()
	        .set("project", projectDAO.getBySlug( projectSlug ) )
	        .set("slug", iterationSlug )
	    )
		.setCacheable(true).uniqueResult();
	}

	public HProjectIteration getBySlug(HIterationProject project, String iterationSlug){
		return (HProjectIteration) session.createCriteria(HProjectIteration.class)
		.add( Restrictions.naturalId()
	        .set("project", project )
	        .set("slug", iterationSlug )
	    )
		.setCacheable(true).uniqueResult();
	}
	
	public TransUnitCount getStatisticsForContainer(Long containerId, LocaleId localeId){
		
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
		
		TransUnitCount stat = new TransUnitCount();
		for(StatusCount count: stats){
			stat.set(count.status, count.count.intValue());
		}
		
		stat.set(ContentState.New, totalCount.intValue() - stat.getNotApproved());
		
		return stat;
	}
}
