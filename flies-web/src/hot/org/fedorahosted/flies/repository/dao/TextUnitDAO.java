package org.fedorahosted.flies.repository.dao;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.repository.model.Document;
//import org.fedorahosted.flies.repository.model.TextUnit;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("textUnitDAO")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class TextUnitDAO {

	
	@In(value="#{entityManager.delegate}")
	Session session;
	/*
	public TextUnit getTextUnitById(Long docId, String hashId){
		return (TextUnit) session.createCriteria(TextUnit.class)
			.add( Restrictions.naturalId()
			        .set("resourceId", hashId)
			        .set("document", session.load(Document.class, docId))
			        )
			    .setCacheable(true)
			    .uniqueResult();
	}*/
}
