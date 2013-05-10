package org.zanata.dao;

import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.EntityStatus;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

@Name("projectDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ProjectDAO extends AbstractDAOImpl<HProject, Long>
{
   @In
   private FullTextEntityManager entityManager;

   public ProjectDAO()
   {
      super(HProject.class);
   }

   public ProjectDAO(Session session)
   {
      super(HProject.class, session);
   }

   public HProject getBySlug(String slug)
   {
      return (HProject) getSession().byNaturalId(HProject.class).using("slug", slug).load();
   }

   @SuppressWarnings("unchecked")
   public List<HPerson> getProjectMaintainerBySlug(String slug)
   {
      Query q = getSession().createQuery("select p.maintainers from HProject as p where p.slug = :slug");
      q.setParameter("slug", slug);
      // http://stackoverflow.com/questions/9060403/hibernate-query-cache-issue
      // q.setCacheable(true)
      q.setComment("ProjectDAO.getProjectMaintainerBySlug");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProject> getOffsetListOrderByName(int offset, int count, boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      String condition = constructFilterCondition(filterActive, filterReadOnly, filterObsolete);
      Query q = getSession().createQuery("from HProject p " + condition + "order by UPPER(p.name)");
      q.setMaxResults(count).setFirstResult(offset);
      q.setCacheable(true).setComment("ProjectDAO.getOffsetListOrderByName");
      return q.list();
   }

   public int getFilterProjectSize(boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      String query = "select count(*) from HProject p " + constructFilterCondition(filterActive, filterReadOnly, filterObsolete);
      Query q = getSession().createQuery(query.toString());
      q.setCacheable(true).setComment("ProjectDAO.getFilterProjectSize");
      Long totalCount = (Long) q.uniqueResult();

      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   private String constructFilterCondition(boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      StringBuilder condition = new StringBuilder();
      if (filterActive || filterReadOnly || filterObsolete)
      {
         condition.append("where ");
      }

      if (filterActive)
      {
         // TODO bind this as a parameter
         condition.append("p.status <> '" + EntityStatus.ACTIVE.getInitial() + "' ");
      }

      if (filterReadOnly)
      {
         if (filterActive)
         {
            condition.append("and ");
         }

         // TODO bind this as a parameter
         condition.append("p.status <> '" + EntityStatus.READONLY.getInitial() + "' ");
      }

      if (filterObsolete)
      {
         if (filterActive || filterReadOnly)
         {
            condition.append("and ");
         }

         // TODO bind this as a parameter
         condition.append("p.status <> '" + EntityStatus.OBSOLETE.getInitial() + "' ");
      }
      return condition.toString();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getAllIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug");
      q.setParameter("projectSlug", slug);
      q.setCacheable(true).setComment("ProjectDAO.getAllIterations");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getActiveIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
      q.setParameter("projectSlug", slug).setParameter("status", EntityStatus.ACTIVE);
      q.setCacheable(true).setComment("ProjectDAO.getActiveIterations");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getReadOnlyIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
      q.setParameter("projectSlug", slug).setParameter("status", EntityStatus.READONLY);
      q.setCacheable(true).setComment("ProjectDAO.getReadOnlyIterations");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getObsoleteIterations(String slug)
   {
      Query q = getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status");
      q.setParameter("projectSlug", slug).setParameter("status", EntityStatus.OBSOLETE);
      q.setCacheable(true).setComment("ProjectDAO.getObsoleteIterations");
      return q.list();
   }

   public int getTotalProjectCount()
   {
      String query = "select count(*) from HProject";
      Query q = getSession().createQuery(query.toString());
      q.setCacheable(true).setComment("ProjectDAO.getTotalProjectCount");
      Long totalCount = (Long) q.uniqueResult();

      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalActiveProjectCount()
   {
      Query q = getSession().createQuery("select count(*) from HProject p where p.status = :status");
      q.setParameter("status", EntityStatus.ACTIVE);
      q.setCacheable(true).setComment("ProjectDAO.getTotalActiveProjectCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalReadOnlyProjectCount()
   {
      Query q = getSession().createQuery("select count(*) from HProject p where p.status = :status");
      q.setParameter("status", EntityStatus.READONLY);
      q.setCacheable(true).setComment("ProjectDAO.getTotalReadOnlyProjectCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalObsoleteProjectCount()
   {
      Query q = getSession().createQuery("select count(*) from HProject p where p.status = :status");
      q.setParameter("status", EntityStatus.OBSOLETE);
      q.setCacheable(true).setComment("ProjectDAO.getTotalObsoleteProjectCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }


   public List<HProject> searchQuery(String searchQuery, int maxResult, int firstResult) throws ParseException
   {
      String[] projectFields = { "slug", "name", "description" };
      QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, projectFields, new StandardAnalyzer(Version.LUCENE_24));
      parser.setAllowLeadingWildcard(true);
      org.apache.lucene.search.Query luceneQuery = parser.parse(QueryParser.escape(searchQuery));
      FullTextQuery query=  entityManager.createFullTextQuery(luceneQuery, HProject.class);
      query.setMaxResults(maxResult).setFirstResult(firstResult).getResultList();

      @SuppressWarnings("unchecked")
      List<HProject> resultList = query.getResultList();
      return resultList;
   }
}
