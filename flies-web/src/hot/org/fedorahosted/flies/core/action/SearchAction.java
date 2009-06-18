package org.fedorahosted.flies.core.action;

import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;

import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.Community;
import org.fedorahosted.flies.core.model.Tribe;

@Name("searchaction")
@Scope(ScopeType.EVENT)
public class SearchAction {
	//@In
	//private EntityManager entityManager;
	
	@In 
	ProjectSearch projectSearch;
	
	@In 
	CommunitySearch communitySearch;
	
	//@In 
	//private TribeSearch tribeSearch;
	
    private String searchQuery;
	
    @Out(required= false)
    private List<Project> projects;
    
    @Out(required= false)
    private List<Community> communities;
    
    @Out(required= false)
    private List<Tribe> tribes;
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
	
    public String search() {
        projectSearch.setSearchQuery(searchQuery);
        projectSearch.doSearch();
        projects = projectSearch.getSearchResults();
        
        communitySearch.setSearchQuery(searchQuery);
        communitySearch.doSearch();
        communities = communitySearch.getSearchResults();
        
        //tribeSearch.setSearchQuery(searchQuery);
        //tribeSearch.doSearch();
        
        return "browse";
    }
    
    

}
