package net.openl10n.flies.dao;

import java.util.List;

import javax.ws.rs.core.EntityTag;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.TransUnitCount;
import net.openl10n.flies.model.HIterationProject;
import net.openl10n.flies.model.HProject;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.model.StatusCount;
import net.openl10n.flies.util.HashUtil;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("projectIterationDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ProjectIterationDAO extends AbstractDAOImpl<HProjectIteration, Long>
{

   public ProjectIterationDAO()
   {
      super(HProjectIteration.class);
   }

   public ProjectIterationDAO(Session session)
   {
      super(HProjectIteration.class, session);
   }

   public HProjectIteration getBySlug(String projectSlug, String iterationSlug)
   {
      HIterationProject project = (HIterationProject) getSession().createCriteria(HProject.class).add(Restrictions.naturalId().set("slug", projectSlug)).setCacheable(true).uniqueResult();

      return getBySlug(project, iterationSlug);
   }

   public HProjectIteration getBySlug(HIterationProject project, String iterationSlug)
   {
      return (HProjectIteration) getSession().createCriteria(HProjectIteration.class).add(Restrictions.naturalId().set("project", project).set("slug", iterationSlug)).setCacheable(true).uniqueResult();
   }

   public TransUnitCount getStatisticsForContainer(Long iterationId, LocaleId localeId)
   {

      @SuppressWarnings("unchecked")
      List<StatusCount> stats = getSession().createQuery("select new net.openl10n.flies.model.StatusCount(tft.state, count(tft)) " + "from HTextFlowTarget tft " + "where tft.textFlow.document.projectIteration.id = :id " + "  and tft.locale.localeId = :locale " + "group by tft.state").setParameter("id", iterationId).setParameter("locale", localeId).setCacheable(true).list();

      Long totalCount = (Long) getSession().createQuery("select count(tf) from HTextFlow tf where tf.document.projectIteration.id = :id").setParameter("id", iterationId).setCacheable(true).uniqueResult();

      TransUnitCount stat = new TransUnitCount();
      for (StatusCount count : stats)
      {
         stat.set(count.status, count.count.intValue());
      }

      stat.set(ContentState.New, totalCount.intValue() - (stat.getApproved() + stat.getNeedReview()));

      return stat;
   }

   public EntityTag getResourcesETag(HProjectIteration projectIteration)
   {
      @SuppressWarnings("unchecked")
      List<Integer> revisions = getSession().createQuery("select d.revision from HDocument d where d.projectIteration =:iteration " + "and d.obsolete =:obsolete").setParameter("iteration", projectIteration).setParameter("obsolete", false).list();

      int hashCode = 1;
      for (int revision : revisions)
      {
         hashCode = 31 * hashCode + revision;
      }

      String hash = HashUtil.generateHash(String.valueOf(hashCode));

      return EntityTag.valueOf(hash);
   }

}
