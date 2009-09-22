package org.fedorahosted.flies.core.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("documentDAO")
@AutoCreate
public class DocumentDAO {

	@In
	Session session;
	
	public HDocument getByDocId(HProjectContainer container, String id){
		return (HDocument) session.createCriteria(HDocument.class)
			.add( Restrictions.naturalId()
		        .set("docId", id)
		        .set("project", container)
		    	)
		    .setCacheable(true)
		    .setComment("DocumentDAO.getById")
		    .uniqueResult();
	}

	public Set<LocaleId> getTargetLocales(HDocument hDoc) {
		List<LocaleId> locales = (List<LocaleId>) session.createQuery(
				"select tft.locale from HTextFlowTarget tft where tft.textFlow.document = :document")
			.setParameter("document", hDoc).list();
		return new HashSet<LocaleId>(locales);
	}
}
