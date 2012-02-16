package org.zanata.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
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
   private static final String ORDERBY_TIMESTAMP = "creationDate";

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
      return (HProject) getSession().createCriteria(HProject.class).add(Restrictions.naturalId().set("slug", slug)).setCacheable(true).setComment("ProjectDAO.getBySlug").uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HPerson> getProjectMaintainerBySlug(String slug)
   {
      Query query = getSession().createQuery("select p.maintainers from HProject as p where p.slug = :slug").setParameter("slug", slug);
      return query.list();
   }

   @SuppressWarnings("unchecked")
   public List<HProject> getOffsetListByCreateDate(int offset, int count, boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      String condition = constructFilterCondition(filterActive, filterReadOnly, filterObsolete);
      return getSession().createQuery("from HProject p " + condition + "order by p.creationDate desc").setMaxResults(count).setFirstResult(offset).setComment("ProjectDAO.getAllProjectOffsetListByCreateDate").list();
   }

   public int getFilterProjectSize(boolean filterActive, boolean filterReadOnly, boolean filterObsolete)
   {
      String query = "select count(*) from HProject p " + constructFilterCondition(filterActive, filterReadOnly, filterObsolete);
      Long totalCount = (Long) getSession().createQuery(query.toString()).uniqueResult();

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
   public List<HProjectIteration> getActiveIterations(String slug)
   {
      return getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status").setParameter("projectSlug", slug).setParameter("status", EntityStatus.ACTIVE).list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getReadOnlyIterations(String slug)
   {
      return getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status").setParameter("projectSlug", slug).setParameter("status", EntityStatus.READONLY).list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getObsoleteIterations(String slug)
   {
      return getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status").setParameter("projectSlug", slug).setParameter("status", EntityStatus.OBSOLETE).list();
   }
}
