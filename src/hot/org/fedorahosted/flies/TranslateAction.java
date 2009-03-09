package org.fedorahosted.flies;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.resources.model.Document;
import org.fedorahosted.flies.resources.model.DocumentTarget;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;

@Name("translateAction")
@Scope(ScopeType.CONVERSATION)
public class TranslateAction {

	@RequestParameter("dId")
	private Long documentId;
	
	@RequestParameter("lId")
	private String localeId;

	@In
	private EntityManager entityManager;
	
	@Logger
	private Log log;
	
	private DocumentTarget documentTarget;

	@Begin(join=true)
	public boolean isValid(){
		if(documentTarget == null)
			if(documentId == null || localeId == null)
				return false;
		return getDocumentTarget() != null;
	}
	
	public DocumentTarget getDocumentTarget() {
		if(documentTarget == null){
			Session session = (Session) entityManager.getDelegate();
			try{
				documentTarget = (DocumentTarget) session.createCriteria(DocumentTarget.class)
					.add( Restrictions.naturalId()
							.set("template", session.load(Document.class, documentId))
							.set("locale", session.load(FliesLocale.class, localeId))).uniqueResult();
			}
			catch(NoResultException e){
				log.warn("Unable to find DocumentTarget with doc_id {0} and locale {1}", documentId, localeId);
			}
			
		}
		return documentTarget;
	}
	
	@End @Destroy
	public void destroy(){
		
	}
}
