package net.openl10n.flies.action;

import java.util.List;

import javax.persistence.EntityManager;

import net.openl10n.flies.model.HProject;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
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
public class ProjectSearch
{

   private int pageSize = 30;

   private String searchQuery;

   private List<HProject> searchResults;

   private int currentPage = 1;

   private int resultSize;

   @In
   EntityManager entityManager;

   public String getSearchQuery()
   {
      return searchQuery;
   }

   public void setSearchQuery(String searchQuery)
   {
      this.searchQuery = searchQuery;
   }

   public List<HProject> getSearchResults()
   {
      return searchResults;
   }

   public void setSearchResults(List<HProject> projects)
   {
      this.searchResults = projects;
   }

   public int getResultSize()
   {
      return resultSize;
   }

   public int getCurrentPage()
   {
      return currentPage;
   }

   public void setCurrentPage(int page)
   {
      if (page < 1)
         this.currentPage = 1;
      else
         this.currentPage = page;
   }

   public void search()
   {
      FullTextQuery query;
      try
      {
         query = searchQuery(searchQuery);
      }
      catch (ParseException pe)
      {
         return;
      }
      resultSize = query.getResultSize();
      searchResults = query.setMaxResults(pageSize + 1).setFirstResult(pageSize * (currentPage - 1)).getResultList();
   }

   private FullTextQuery searchQuery(String searchQuery) throws ParseException
   {
      String[] projectFields = { "slug", "name", "description" };
      QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, projectFields, new StandardAnalyzer());
      parser.setAllowLeadingWildcard(true);
      Query luceneQuery = parser.parse(QueryParser.escape(searchQuery));
      return ((FullTextEntityManager) entityManager).createFullTextQuery(luceneQuery, HProject.class);
   }

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

}
