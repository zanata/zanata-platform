package org.fedorahosted.flies.core.action;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.HCommunity;
import org.fedorahosted.flies.core.model.HProject;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
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

    	List<HProject> projects = entityManager.createQuery("select project from HProject as project").getResultList(); 
	 	            for (HProject project : projects) { 
	                fullTextEntityManager.index(project); 
	 	            }  

        
    }
    
    public void resetCommunityIndex() throws Exception {
    	FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager); 

    	List<HCommunity> communities = entityManager.createQuery("select community from HCommunity as community").getResultList(); 
	 	            for (HCommunity community : communities) { 
	                fullTextEntityManager.index(community); 
	 	            }  

        
    }
}
