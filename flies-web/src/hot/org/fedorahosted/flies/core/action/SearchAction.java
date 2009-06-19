package org.fedorahosted.flies.core.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.In;

@Name("searchaction")
@Scope(ScopeType.EVENT)
public class SearchAction {
	@In 
	ProjectSearch projectSearch;
	
	@In 
	CommunitySearch communitySearch;

    private String searchQuery;
    private String activeView;
       
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
    	projectSearch.setSearchQuery(searchQuery);
    	communitySearch.setSearchQuery(searchQuery);
    }
    
    public String getActiveView() {
    	return activeView;
    }
    
    public void setActiveView(String view) {
    	this.activeView = view;
    }
	
    public void search() {
        projectSearch.doSearch();
        communitySearch.doSearch();
    }
}
