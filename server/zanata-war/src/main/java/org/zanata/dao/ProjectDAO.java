package org.zanata.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.SlugEntityBase;

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
   public List<HProject> getOffsetListByCreateDate(int offset, int count, boolean filterCurrent, boolean filterRetired, boolean filterObsolete)
   {
      if (!filterCurrent && !filterRetired && !filterObsolete) // all records
      {
         return getSession().createCriteria(HProject.class).addOrder(Order.desc(ORDERBY_TIMESTAMP)).setMaxResults(count).setFirstResult(offset).setComment("ProjectDAO.getAllProjectOffsetListByCreateDate").list();
      }
      
      String condition = constructFilterCondition(filterCurrent, filterRetired, filterObsolete);
      return getSession().createQuery("from HProject p " + condition + "order by p.creationDate").setMaxResults(count).setFirstResult(offset).setComment("ProjectDAO.getAllProjectOffsetListByCreateDate").list();
   }

   public int getProjectSize()
   {
      Long totalCount = (Long) getSession().createQuery("select count(*) from HProject").uniqueResult();

      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   private String constructFilterCondition(boolean filterCurrent, boolean filterRetired, boolean filterObsolete)
   {
      StringBuilder condition = new StringBuilder();
      if (filterCurrent || filterRetired || filterObsolete)
      {
         condition.append("where ");
      }

      if (filterCurrent)
      {
         condition.append("p.status <> '" + SlugEntityBase.StatusType.Current + "' ");
      }

      if (filterRetired)
      {
         if (filterCurrent)
         {
            condition.append("and ");
         }

         condition.append("p.status <> '" + SlugEntityBase.StatusType.Retired + "' ");
      }

      if (filterObsolete)
      {
         if (filterCurrent || filterRetired)
         {
            condition.append("and ");
         }

         condition.append("p.status <> '" + SlugEntityBase.StatusType.Obsolete + "' ");
      }
      return condition.toString();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getCurrentIterations(String slug)
   {
      return getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status").setParameter("projectSlug", slug).setParameter("status", SlugEntityBase.StatusType.Current).list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getRetiredIterations(String slug)
   {
      return getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status").setParameter("projectSlug", slug).setParameter("status", SlugEntityBase.StatusType.Retired).list();
   }

   @SuppressWarnings("unchecked")
   public List<HProjectIteration> getObsoleteIterations(String slug)
   {
      return getSession().createQuery("from HProjectIteration t where t.project.slug = :projectSlug and t.status = :status").setParameter("projectSlug", slug).setParameter("status", SlugEntityBase.StatusType.Obsolete).list();
   }
}
