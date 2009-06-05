package org.fedorahosted.flies.repository.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.repository.model.TextUnit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("targetManager")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class IterationManager {

	private ProjectIteration target;
	private Long id;;
	
	@In
	EntityManager entityManager;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
		clearCache();
		initInstance();
	}
	
	public boolean isManaged(){
		return target != null;
	}
	
	private void initInstance(){
		target = entityManager.find(ProjectIteration.class, getId()); 
	}
	

	public ProjectIteration getIteration() {
		return target;
	}
	
	public void persist(){
		entityManager.flush();
	}

	private Map<String, Document> documentNamesMap;
	
	
	private void clearCache(){
		documentNamesMap = null;
	}
	private void makeCache(){
		if(documentNamesMap == null){
			documentNamesMap = new HashMap<String, Document>();
			for(Document doc : target.getDocuments()){
				documentNamesMap.put(doc.getName(), doc);
			}
		}
	}
	
	public Document getDocumentByName(String name){
		makeCache();
		return documentNamesMap.get(name);
	}
	
	public Set<String> getDocumentNames(){
		makeCache();
		return documentNamesMap.keySet();
	}

	
}
