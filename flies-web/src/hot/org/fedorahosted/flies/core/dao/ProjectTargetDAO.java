package org.fedorahosted.flies.core.dao;

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
		TranslationStatistics stats =  (TranslationStatistics) session.createQuery(
				"select new TranslationStatistics(count(pt),2,3,4) " +
				"from ProjectTarget pt"
		).uniqueResult();
		
		log.info("stats {0}:{1}:", stats.getApproved(), stats.getNew());
		return stats;
	}
}
