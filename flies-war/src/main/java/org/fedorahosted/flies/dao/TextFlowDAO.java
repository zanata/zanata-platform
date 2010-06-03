package org.fedorahosted.flies.dao;

import java.util.List;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HTextFlow;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long>{

	public TextFlowDAO() {
		super(HTextFlow.class);
	}
	
	public TextFlowDAO(Session session) {
		super(HTextFlow.class, session);
	}
	
	/**
	 * @param document
	 * @param id
	 * @return
	 */
	public HTextFlow getById(HDocument document, String id){
		return (HTextFlow) getSession().createCriteria(HTextFlow.class)
			.add( Restrictions.naturalId()
		        .set("resId", id)
		        .set("document", document)
		    	)
		    .setCacheable(true)
		    .setComment("TextFlowDAO.getById")
		    .uniqueResult();
	}
	
	public List<HTextFlow> findByIdList(List<Long> idList){
		Query query = getSession().createQuery(
				"FROM HTextFlow WHERE id in (:idList)");
		query.setParameterList("idList", idList);
		query.setComment("TextFlowDAO.getByIdList");
		return query.list();
	}
	
	public HTextFlow getObsoleteById(HDocument document, String id) {
		return (HTextFlow) getSession().createCriteria(HTextFlow.class)
		.add( Restrictions.naturalId()
	        .set("resId", id)
	        .set("document", document)
	    	)
	    .add( Restrictions.eq("obsolete", true))
	    .setCacheable(true)
	    .setComment("TextFlowDAO.getObsoleteById")
	    .uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Long> getIdsByTargetState(LocaleId locale, ContentState state) {
		Query q = getSession().createQuery(
			"select tft.textFlow.id from HTextFlowTarget tft where tft.locale=:locale and tft.state=:state");
		q.setParameter("locale", locale);
		q.setParameter("state", state);
		q.setComment("TextFlowDAO.getIdsByTargetState");
		return q.list();
	}
}
