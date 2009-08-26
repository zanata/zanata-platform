package org.fedorahosted.flies.core.action;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.Community;
import org.fedorahosted.flies.core.model.Project;
import org.hibernate.search.jpa.Search; 
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("searchadmin")
@Scope(ScopeType.EVENT)
public class SearchAdmin {
    @In
	EntityManager entityManager;

    public void resetProjectIndex() throws Exception {
    	FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager); 

    	List<Project> projects = entityManager.createQuery("select project from Project as project").getResultList(); 
	 	            for (Project project : projects) { 
	                fullTextEntityManager.index(project); 
	 	            }  

        
    }
    
    public void resetCommunityIndex() throws Exception {
    	FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager); 

    	List<Community> communities = entityManager.createQuery("select community from Community as community").getResultList(); 
	 	            for (Community community : communities) { 
	                fullTextEntityManager.index(community); 
	 	            }  

        
    }
}
