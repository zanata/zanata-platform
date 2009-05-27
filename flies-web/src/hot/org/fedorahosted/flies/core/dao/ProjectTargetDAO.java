package org.fedorahosted.flies.core.dao;

import java.util.List;

import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.hibernate.Session;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("projectTargetDAO")
@AutoCreate
public class ProjectTargetDAO {
	
	@In(value="#{entityManager.delegate}")
	Session session;
	
	@Logger
	Log log;
//Long targetId, String localeId
	
	
	public TranslationStatistics getStatisticsForTarget(){
		List<Long> stats = session.createQuery(
				"select new org.fedorahosted.flies.core.model.StatusCount(pt.status, count(pt)) " +
				"from TextUnitTarget pt " +
				"group by pt.status"
		).list();
		
		return new TranslationStatistics(0l,0l,0l,0l);
	}
}
