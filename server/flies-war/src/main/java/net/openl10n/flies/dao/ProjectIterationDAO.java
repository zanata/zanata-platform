/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package net.openl10n.flies.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.EntityTag;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.TransUnitCount;
import net.openl10n.flies.common.TransUnitWords;
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

   /**
    * @see DocumentDAO#getStatistics(long, LocaleId)
    * @param iterationId
    * @param localeId
    * @return
    */
   public TransUnitCount getStatisticsForContainer(Long iterationId, LocaleId localeId)
   {

      @SuppressWarnings("unchecked")
      // @formatter:off
      List<StatusCount> stats = getSession().createQuery("select new net.openl10n.flies.model.StatusCount(tft.state, count(tft)) " +
         "from HTextFlowTarget tft " + 
         "where tft.textFlow.document.projectIteration.id = :id " + 
         "  and tft.locale.localeId = :locale" +
         " and tft.textFlow.obsolete = false" + 
         " and tft.textFlow.document.obsolete = false" + 
         " group by tft.state")
         .setParameter("id", iterationId)
         .setParameter("locale", localeId)
         .setCacheable(true).list();

      // @formatter:on

      TransUnitCount stat = new TransUnitCount();

      for (StatusCount count : stats)
      {
         stat.set(count.status, count.count.intValue());
      }

      Long totalCount = getTotalCountForIteration(iterationId);

      stat.set(ContentState.New, totalCount.intValue() - (stat.getApproved() + stat.getNeedReview()));

      return stat;
   }

   /**
    * @see DocumentDAO#getStatistics(long, LocaleId)
    * @param iterationId
    * @param localeId
    * @return
    */
   public TransUnitWords getWordStatsForContainer(Long iterationId, LocaleId localeId)
   {

      @SuppressWarnings("unchecked")
      // @formatter:off
      List<StatusCount> stats = getSession().createQuery("select new net.openl10n.flies.model.StatusCount(tft.state, sum(tft.textFlow.wordCount)) " +
         "from HTextFlowTarget tft " + 
         "where tft.textFlow.document.projectIteration.id = :id " + 
         "  and tft.locale.localeId = :locale" +
         " and tft.textFlow.obsolete = false" + 
         " and tft.textFlow.document.obsolete = false" + 
         " group by tft.state")
         .setParameter("id", iterationId)
         .setParameter("locale", localeId)
         .setCacheable(true).list();

      // @formatter:on

      TransUnitWords stat = new TransUnitWords();

      for (StatusCount count : stats)
      {
         stat.set(count.status, count.count.intValue());
      }

      Long totalCount = getTotalCountForIteration(iterationId);

      stat.set(ContentState.New, totalCount.intValue() - (stat.getApproved() + stat.getNeedReview()));

      return stat;
   }

   public EntityTag getResourcesETag(HProjectIteration projectIteration)
   {
      @SuppressWarnings("unchecked")
      // @formatter:off
      List<Integer> revisions = getSession().createQuery(
         "select d.revision from HDocument d " +
         "where d.projectIteration =:iteration " + 
         "and d.obsolete = false")
            .setParameter("iteration", projectIteration)
            .list();
      // @formatter:on

      int hashCode = 1;
      for (int revision : revisions)
      {
         hashCode = 31 * hashCode + revision;
      }

      String hash = HashUtil.generateHash(String.valueOf(hashCode));

      return EntityTag.valueOf(hash);
   }

   /**
    * @param iterationId
    * @param localeId
    * @return
    */
   public Map<String, TransUnitWords> getAllWordStatsStatistics(Long iterationId)
   {
      // @formatter:off
      @SuppressWarnings("unchecked")
      List< Object[]> stats = getSession().createQuery("select tft.state, sum(tft.textFlow.wordCount), tft.locale.localeId " +
      		"from HTextFlowTarget tft where tft.textFlow.document.projectIteration.id = :id  and tft.textFlow.obsolete = false" +
      		" and tft.textFlow.document.obsolete = false group by tft.state, tft.locale.localeId").setParameter("id", iterationId).setCacheable(true).list();
      // @formatter:on

      Map<String, TransUnitWords> result = new HashMap<String, TransUnitWords>();

      for (Object[] count : stats)
      {
         TransUnitWords stat;
         ContentState state = (ContentState) count[0];
         Long word = (Long) count[1];
         LocaleId locale = (LocaleId) count[2];
         if (!result.containsKey(locale))
         {
            stat = new TransUnitWords();
            result.put(locale.getId(), stat);
         }
         else
         {
            stat = result.get(locale);
         }

         stat.set(state, word.intValue());
      }

      Long totalCount = getTotalCountForIteration(iterationId);
      for (TransUnitWords count : result.values())
      {
         count.set(ContentState.New, totalCount.intValue() - (count.getApproved() + count.getNeedReview()));
      }
      return result;
   }

   public Long getTotalCountForIteration(Long iterationId)
   {
      // @formatter:off
      Long totalCount = (Long) getSession().createQuery(
            "select sum(tf.wordCount) from HTextFlow tf " +
            "where tf.document.projectIteration.id = :id" +
            " and tf.obsolete = false" +
            " and tf.document.obsolete = false")
               .setParameter("id", iterationId)
               .setCacheable(true).uniqueResult();
      // @formatter:on
      if (totalCount == null)
      {
         totalCount = 0L;
      }
      return totalCount;
   }

}
