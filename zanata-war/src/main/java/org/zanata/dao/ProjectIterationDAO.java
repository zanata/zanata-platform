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
package org.zanata.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.EntityTag;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.StatusCount;
import org.zanata.util.HashUtil;

@Name("projectIterationDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ProjectIterationDAO extends AbstractDAOImpl<HProjectIteration, Long>
{
   @In
   private FullTextEntityManager entityManager;

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
      Criteria crit = getSession().createCriteria(HProject.class);
      crit.add(Restrictions.naturalId().set("slug", projectSlug));
      crit.setCacheable(true).setComment("ProjectIterationDAO.getBySlugs");
      HIterationProject project = (HIterationProject) crit.uniqueResult();

      return getBySlug(project, iterationSlug);
   }

   public HProjectIteration getBySlug(HIterationProject project, String iterationSlug)
   {
      Criteria crit = getSession().createCriteria(HProjectIteration.class);
      crit.add(Restrictions.naturalId().set("project", project).set("slug", iterationSlug));
      crit.setCacheable(true).setComment("ProjectIterationDAO.getBySlug");
      return (HProjectIteration) crit.uniqueResult();
   }

   /**
    * @see DocumentDAO#getStatistics(long, LocaleId)
    * @param iterationId
    * @param localeId
    * @return
    */
   public TransUnitCount getStatisticsForContainer(Long iterationId, LocaleId localeId)
   {

      // @formatter:off
      Query q = getSession().createQuery("select new org.zanata.model.StatusCount(tft.state, count(tft)) " +
         "from HTextFlowTarget tft " + 
         "where tft.textFlow.document.projectIteration.id = :id " + 
         "  and tft.locale.localeId = :locale" +
         " and tft.textFlow.obsolete = false" + 
         " and tft.textFlow.document.obsolete = false" + 
         " group by tft.state");
      // @formatter:on
      q.setParameter("id", iterationId)
            .setParameter("locale", localeId);
      q.setCacheable(true).setComment("ProjectIterationDAO.getStatisticsForContainer");
      @SuppressWarnings("unchecked")
      List<StatusCount> stats = q.list();

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

      // @formatter:off
      Query q = getSession().createQuery("select new org.zanata.model.StatusCount(tft.state, sum(tft.textFlow.wordCount)) " +
         "from HTextFlowTarget tft " + 
         "where tft.textFlow.document.projectIteration.id = :id " + 
         "  and tft.locale.localeId = :locale" +
         " and tft.textFlow.obsolete = false" + 
         " and tft.textFlow.document.obsolete = false" + 
         " group by tft.state");
         // @formatter:on
      q.setParameter("id", iterationId)
            .setParameter("locale", localeId);
      q.setCacheable(true).setComment("ProjectIterationDAO.getWordStatsForContainer");
      @SuppressWarnings("unchecked")
      List<StatusCount> stats = q.list();


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
      // @formatter:off
      Query q = getSession().createQuery(
         "select d.revision from HDocument d " +
         "where d.projectIteration =:iteration " + 
         "and d.obsolete = false")
            .setParameter("iteration", projectIteration);
      // @formatter:on
      q.setCacheable(true).setComment("ProjectIterationDAO.getResourcesETag");
      @SuppressWarnings("unchecked")
      List<Integer> revisions = q.list();

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
      Query q = getSession().createQuery("select tft.state, sum(tft.textFlow.wordCount), tft.locale.localeId " +
      		"from HTextFlowTarget tft where tft.textFlow.document.projectIteration.id = :id  and tft.textFlow.obsolete = false" +
      		" and tft.textFlow.document.obsolete = false group by tft.state, tft.locale.localeId");
      // @formatter:on
      q.setParameter("id", iterationId);
      q.setCacheable(true).setComment("ProjectIterationDAO.getAllWordStatsStatistics");
      @SuppressWarnings("unchecked")
      List<Object[]> stats = q.list();

      Map<String, TransUnitWords> result = new HashMap<String, TransUnitWords>();

      for (Object[] count : stats)
      {
         TransUnitWords stat;
         ContentState state = (ContentState) count[0];
         Long word = (Long) count[1];
         LocaleId locale = (LocaleId) count[2];
         if (!result.containsKey(locale.getId()))
         {
            stat = new TransUnitWords();
            result.put(locale.getId(), stat);
         }
         else
         {
            stat = result.get(locale.getId());
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
      Query q = getSession().createQuery(
            "select sum(tf.wordCount) from HTextFlow tf " +
            "where tf.document.projectIteration.id = :id" +
            " and tf.obsolete = false" +
            " and tf.document.obsolete = false");
      // @formatter:on
      q.setParameter("id", iterationId);
      q.setCacheable(true).setComment("ProjectIterationDAO.getTotalCountForIteration");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
      {
         totalCount = 0L;
      }
      return totalCount;
   }

   public int getTotalProjectIterCount()
   {
      String query = "select count(*) from HProjectIteration";
      Query q = getSession().createQuery(query.toString());
      q.setCacheable(true).setComment("ProjectIterationDAO.getTotalProjectIterCount");
      Long totalCount = (Long) q.uniqueResult();

      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalActiveProjectIterCount()
   {
      Query q = getSession().createQuery("select count(*) from HProjectIteration t where t.status = :status");
      q.setParameter("status", EntityStatus.ACTIVE);
      q.setCacheable(true).setComment("ProjectIterationDAO.getTotalActiveProjectIterCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalReadOnlyProjectIterCount()
   {
      Query q = getSession().createQuery("select count(*) from HProjectIteration t where t.status = :status");
      q.setParameter("status", EntityStatus.READONLY);
      q.setCacheable(true).setComment("ProjectIterationDAO.getTotalReadOnlyProjectIterCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalObsoleteProjectIterCount()
   {
      Query q = getSession().createQuery("select count(*) from HProjectIteration t where t.status = :status");
      q.setParameter("status", EntityStatus.OBSOLETE);
      q.setCacheable(true).setComment("ProjectIterationDAO.getTotalObsoleteProjectIterCount");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public List<HProjectIteration> searchLikeSlugOrProjectSlug(String searchTerm)
   {
      Query q = getSession().createQuery("from HProjectIteration t where lower(t.slug) LIKE :searchTerm OR lower(t.project.slug) LIKE :searchTerm OR lower(t.project.name) LIKE :searchTerm");
      q.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
      q.setCacheable(true).setComment("ProjectIterationDAO.searchLikeSlugOrProjectSlug");

      return q.list();

      // final String FIELDS[] = { "slug", GroupSearchBridge.PROJECT_FIELD };
      //
      // MultiFieldQueryParser parser = new
      // MultiFieldQueryParser(Version.LUCENE_29, FIELDS, new
      // StandardAnalyzer(Version.LUCENE_29));
      // org.apache.lucene.search.Query textQuery = parser.parse(searchTerm);
      //
      // org.hibernate.search.jpa.FullTextQuery ftQuery =
      // entityManager.createFullTextQuery(textQuery, HProjectIteration.class);
      // @SuppressWarnings("unchecked")
      // List<HProjectIteration> matches = (List<HProjectIteration>)
      // ftQuery.getResultList();
      // return matches;
   }
}
