package org.fedorahosted.flies.core.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.web.RequestParameter;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import org.fedorahosted.flies.core.model.IterationProject;

@Name("search")
@Scope(ScopeType.EVENT)
public class ProjectSearch {
    @In
    private EntityManager entityManager;

    Long id;

    int pageSize = 15;
    int currentPage = 0;
    boolean hasMore = false;
    int numberOfResults;
    
    //@RequestParameter("q")
    private String searchQuery;

    @DataModel
    List<IterationProject> searchResults;

    IterationProject selectedProject;

    @Out(required = false)
    IterationProject project;

    Map<IterationProject, Boolean> searchSelections;


    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    
    public int getNumberOfResults() {
        return numberOfResults;
    }
    
    @Begin(join = true)
    public String doSearch() {
        currentPage = 0;
        updateResults();
        return "browse";
    }
    
    public void nextPage() {
        if (!isLastPage()) {
            currentPage++;
            updateResults();
        }
    }

    public void prevPage() {
        if (!isFirstPage()) {
            currentPage--;
            updateResults();
        }
    }
    
    @Begin(join = true)
    public void selectFromRequest() {
        if (id != null)  {
            project = entityManager.find(IterationProject.class, id);
        } else if (selectedProject != null) {
            project = selectedProject;
        }
    }

    public boolean isLastPage() {
        return ( searchResults != null ) && !hasMore;
    }

    public boolean isFirstPage() {
        return ( searchResults != null ) && ( currentPage == 0 );
    }

    @SuppressWarnings("unchecked")
    private void updateResults() {
        FullTextQuery query;
        try {
            query = searchQuery(searchQuery);
        } catch (ParseException pe) { 
            return; 
        }
      
        List<IterationProject> items = query
            .setMaxResults(pageSize + 1)
            .setFirstResult(pageSize * currentPage)
            .getResultList();
        numberOfResults = query.getResultSize();
        
        if (items.size() > pageSize) {
            searchResults = new ArrayList(items.subList(0, pageSize));
            hasMore = true;
        } else {
            searchResults = items;
            hasMore = false;
        }

        searchSelections = new HashMap<IterationProject, Boolean>();
    }

    private FullTextQuery searchQuery(String searchQuery) throws ParseException
    {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
    
        List<IterationProject> projects = entityManager.createQuery("select project from IterationProject as project").getResultList();
	for (IterationProject project : projects) {
    		fullTextEntityManager.index(project);
	} 
        
        String[] projectFields = {"name", "description"};
        QueryParser parser = new MultiFieldQueryParser(projectFields, new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        org.apache.lucene.search.Query luceneQuery;
        luceneQuery = parser.parse(searchQuery);
        return ( (FullTextEntityManager) entityManager ).createFullTextQuery(luceneQuery, IterationProject.class);
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getSelectedId() {
        return id;
    }

    public void setSelectedId(Long id) {
        this.id = id;
    }
    
    @End
    public void reset() { }

    @Destroy
    @Remove
    public void destroy() { }
}
