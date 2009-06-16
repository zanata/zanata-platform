package org.fedorahosted.flies.core.action;

import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.In;
import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.Community;
import org.fedorahosted.flies.core.model.Tribe;

@Name("searchaction")
@Scope(ScopeType.EVENT)
public class SearchAction {
	@In
	private EntityManager entityManager;
	
    private String searchQuery;
	
    @DataModel
    private List<Project> projects;
    
    @DataModel
    private List<Community> communities;
    
    @DataModel
    private List<Tribe> tribes;
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
	
    public String search() {
        ProjectSearch projectSearch = new ProjectSearch(entityManager);
        //CommunitySearch communitySearch = new CommunitySearch()
        //TribeSearch tribeSearch = new TribeSearch();
        
        projectSearch.setSearchQuery(searchQuery);
        projectSearch.doSearch();
        projects = projectSearch.getSearchResults();
        
        //communitySearch.setSearchQuery(searchQuery);
        //communitySearch.doSearch();
        
        //tribeSearch.setSearchQuery(searchQuery);
        //tribeSearch.doSearch();
        
        return "browse";
    }
    
    

}
