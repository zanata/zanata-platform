package org.fedorahosted.flies.dao;


import java.util.List;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

@Name("textFlowTargetDAO")
@AutoCreate
public class TextFlowTargetDAO extends AbstractDAOImpl<HTextFlowTarget, Long>{

	public TextFlowTargetDAO() {
		super(HTextFlowTarget.class);
	}
	
	public TextFlowTargetDAO(Session session) {
		super(HTextFlowTarget.class, session);
	}
	
	/**
	 * @param textFlow
	 * @param localeId
	 * @return
	 */
	public HTextFlowTarget getByNaturalId(HTextFlow textFlow, LocaleId localeId){
		return (HTextFlowTarget) getSession().createCriteria(HTextFlowTarget.class)
			.add( Restrictions.naturalId()
		        .set("textFlow", textFlow)
		        .set("locale", localeId)
		    	)
		    .setCacheable(true)
		    .setComment("TextFlowTargetDAO.getByNaturalId")
		    .uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<HTextFlowTarget> findAllTranslations(HDocument document, LocaleId locale) {
		return getSession().createQuery(
				"select t from HTextFlowTarget t where " +
				"t.textFlow.document =:document and t.locale =:locale " +
				"and t.state !=:state " +
				"order by t.textFlow.pos")
				.setParameter("document", document)
				.setParameter("locale", locale)
				.setParameter("state", ContentState.New)
				.list();
		
	}
	
}
