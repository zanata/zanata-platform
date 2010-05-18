package org.fedorahosted.flies.core.dao;

import java.util.List;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("textFlowDAO")
@AutoCreate
public class TextFlowDAO {

	@In
	Session session;
	
	/**
	 * @param document
	 * @param id
	 * @return
	 */
	public HTextFlow getById(HDocument document, String id){
		return (HTextFlow) session.createCriteria(HTextFlow.class)
			.add( Restrictions.naturalId()
		        .set("resId", id)
		        .set("document", document)
		    	)
		    .setCacheable(true)
		    .setComment("ResourceDAO.getById")
		    .uniqueResult();
	}
	
	public HTextFlow getObsoleteById(HDocument document, String id) {
		return (HTextFlow) session.createCriteria(HTextFlow.class)
		.add( Restrictions.naturalId()
	        .set("resId", id)
	        .set("document", document)
	    	)
	    .add( Restrictions.eq("obsolete", true))
	    .setCacheable(true)
	    .setComment("ResourceDAO.getObsoleteById")
	    .uniqueResult();
	}

	public List<Long> getIdsByTargetState(LocaleId locale, ContentState state) {
		Query q = session.createQuery(
			"select tft.textFlow.id from HTextFlowTarget tft where tft.locale=:locale and tft.state=:state");
		q.setParameter("locale", locale);
		q.setParameter("state", state);
		q.setComment("TextFlowDAO.getIdsByTargetState");
		return q.list();
	}
}
