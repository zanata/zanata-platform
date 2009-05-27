package org.fedorahosted.flies.repository.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.repository.model.Document;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("documentDAO")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class DocumentDAO {

	@In
	EntityManager entityManager;
	
	public List<Document> getDocumentsForTarget(Long targetId){
		return entityManager.createQuery("from Document d where d.projectTarget.id = :projectTargetId")
			.setParameter("projectTargetId", targetId)
			.getResultList();
	}
	
	public Document getDocumentWithUnits(Long docId){
		return null;
	}
	
	public void removeDocument(Document doc){
		
	}
	
}
