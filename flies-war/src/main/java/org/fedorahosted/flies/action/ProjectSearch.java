package org.fedorahosted.flies.action;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.fedorahosted.flies.model.HProject;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("projectSearch")
@Scope(ScopeType.EVENT)
@AutoCreate
public class ProjectSearch {
    
    int pageSize = 5;
    
    boolean hasMore = false;
    
    private String searchQuery;

    private List<HProject> searchResults;
	
    private int currentPage = 0;
    
    private int resultSize;
    
    @In
	EntityManager entityManager;
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    public List<HProject> getSearchResults() {
        return searchResults;
    }
    
    public void setSearchResults(List<HProject> projects) {
        this.searchResults = projects;
    }
    
    public int getResultSize () {
    	return resultSize;
    }
    
    public void setResultSize(int value) {
    	this.resultSize = value;
    }
    
    public int getCurrentPage() {
    	return currentPage;
    }
    
    public void setCurrentPage(int page) {
    	this.currentPage = page;
    }
    
    public void doSearch() {
        updateResults();
    }
    
    public void nextPage() {
        if (!lastPage()) {
            currentPage++;
        }
    }

    public void prevPage() {
        if (!firstPage()) {
            currentPage--;
        }
    }

    public boolean lastPage() {
        return ( searchResults != null ) && !hasMore;
    }

    public boolean firstPage() {
        return ( searchResults != null ) && ( currentPage == 0 );
    }

    private void updateResults() {
        FullTextQuery query;
        try {
            query = searchQuery(searchQuery);
        } catch (ParseException pe) { 
            return; 
        }
        resultSize = query.getResultSize();
        List<HProject> items = query
            .setMaxResults(pageSize + 1)
            .setFirstResult(pageSize * currentPage)
            .getResultList();
        
        if (items.size() > pageSize) {
            searchResults = new ArrayList(items.subList(0, pageSize));
            hasMore = true;
        } else {
            searchResults = items;
            hasMore = false;
        }

    }

    private FullTextQuery searchQuery(String searchQuery) throws ParseException
    {
        String[] projectFields = {"name", "description"};
        QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, projectFields, new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        Query luceneQuery = parser.parse(QueryParser.escape(searchQuery));
        return ( (FullTextEntityManager) entityManager ).createFullTextQuery(luceneQuery, HProject.class);
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
