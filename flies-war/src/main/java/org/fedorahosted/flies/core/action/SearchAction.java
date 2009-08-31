package org.fedorahosted.flies.core.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("searchaction")
@Scope(ScopeType.EVENT)
public class SearchAction {
	@In 
	ProjectSearch projectSearch;
	
	@In 
	CommunitySearch communitySearch;

    private String searchQuery;
    private String activeView;
    private int currentPage;
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
    	this.searchQuery = searchQuery;
    	projectSearch.setSearchQuery(searchQuery);
    	communitySearch.setSearchQuery(searchQuery);
    }
    
    public int getCurrentPage() {
    	return currentPage;
    }
    
    public void setCurrentPage(int page) {
    	this.currentPage = page;
    	projectSearch.setCurrentPage(page);
    	communitySearch.setCurrentPage(page);
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
