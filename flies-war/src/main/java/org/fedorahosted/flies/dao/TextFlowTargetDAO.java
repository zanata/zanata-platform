package org.fedorahosted.flies.dao;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("textFlowTargetDAO")
@AutoCreate
public class TextFlowTargetDAO {

	@In
	Session session;
	
	/**
	 * @param textFlow
	 * @param localeId
	 * @return
	 */
	public HTextFlowTarget getByNaturalId(HTextFlow textFlow, LocaleId localeId){
		return (HTextFlowTarget) session.createCriteria(HTextFlowTarget.class)
			.add( Restrictions.naturalId()
		        .set("textFlow", textFlow)
		        .set("locale", localeId)
		    	)
		    .setCacheable(true)
		    .setComment("TextFlowTargetDAO.getById")
		    .uniqueResult();
	}
}
