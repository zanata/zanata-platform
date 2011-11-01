package org.zanata.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.StatusCount;

@Name("documentDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class DocumentDAO extends AbstractDAOImpl<HDocument, Long>
{

   public DocumentDAO()
   {
      super(HDocument.class);
   }

   public DocumentDAO(Session session)
   {
      super(HDocument.class, session);
   }

   public HDocument getByDocId(HProjectIteration iteration, String id)
   {
      return (HDocument) getSession().createCriteria(HDocument.class).add(Restrictions.naturalId().set("docId", id).set("projectIteration", iteration)).setCacheable(true).setComment("DocumentDAO.getById").uniqueResult();
   }

   public Set<LocaleId> getTargetLocales(HDocument hDoc)
   {
      @SuppressWarnings("unchecked")
      // @formatter:off
      List<LocaleId> locales = getSession().createQuery(
         "select tft.locale from HTextFlowTarget tft " +
         "where tft.textFlow.document = :document")
            .setParameter("document", hDoc)
            .list();
      // @formatter:on
      return new HashSet<LocaleId>(locales);
   }

   /**
    * @see ProjectIterationDAO#getStatisticsForContainer(Long, LocaleId)
    * @param docId
    * @param localeId
    * @return
    */
   public TranslationStats getStatistics(long docId, LocaleId localeId)
   {
      // @formatter:off
      Session session = getSession();

      // calculate unit counts
      @SuppressWarnings("unchecked")
      List<StatusCount> stats = session.createQuery(
         "select new org.zanata.model.StatusCount(tft.state, count(tft)) " + 
         "from HTextFlowTarget tft " + 
         "where tft.textFlow.document.id = :id " + 
         "  and tft.locale.localeId = :locale " + 
         "  and tft.textFlow.obsolete = false " + 
         "group by tft.state")
            .setParameter("id", docId)
            .setParameter("locale", localeId)
            .setCacheable(true).list();
      Long totalCount = (Long) session.createQuery(
         "select count(tf) from HTextFlow tf " +
         "where tf.document.id = :id and tf.obsolete = false")
            .setParameter("id", docId)
            .setCacheable(true).uniqueResult();

      TransUnitCount stat = new TransUnitCount();
      for (StatusCount count : stats)
      {
         stat.set(count.status, count.count.intValue());
      }
      int newCount = totalCount.intValue() - stat.get(ContentState.Approved) - stat.get(ContentState.NeedReview);
      stat.set(ContentState.New, newCount);

      // calculate word counts
      @SuppressWarnings("unchecked")
      List<StatusCount> wordStats = session.createQuery(
         "select new org.zanata.model.StatusCount(tft.state, sum(tft.textFlow.wordCount)) " + 
         "from HTextFlowTarget tft where tft.textFlow.document.id = :id " + 
         "  and tft.locale.localeId = :locale " + 
         "  and tft.textFlow.obsolete = false " + 
         "group by tft.state")
            .setParameter("id", docId)
            .setParameter("locale", localeId)
            .list();
      Long totalWordCount = (Long) session.createQuery(
         "select sum(tf.wordCount) from HTextFlow tf " +
         "where tf.document.id = :id and tf.obsolete = false")
            .setParameter("id", docId)
            .uniqueResult();
      if (totalWordCount == null)
         totalWordCount = 0L;

      TransUnitWords wordCount = new TransUnitWords();
      for (StatusCount count : wordStats)
      {
         wordCount.set(count.status, count.count.intValue());
      }
      long newWordCount = totalWordCount.longValue() - wordCount.get(ContentState.Approved) - wordCount.get(ContentState.NeedReview);
      wordCount.set(ContentState.New, (int) newWordCount);

      TranslationStats transStats = new TranslationStats(stat, wordCount);
      return transStats;
      // @formatter:on
   }

   public void syncRevisions(HDocument doc, HTextFlow... textFlows)
   {
      int rev = doc.getRevision();
      syncRevisions(doc, rev, textFlows);
   }

   public void syncRevisions(HDocument doc, int revision, HTextFlow... textFlows)
   {
      doc.setRevision(revision);
      for (HTextFlow textFlow : textFlows)
      {
         textFlow.setRevision(revision);
      }
   }
   
   public HDocument getByProjectIterationAndDocId(final String projectSlug, final String iterationSlug, final String docId)
   {
      Session session = getSession();
      
      final HDocument doc = (HDocument)
         session.createQuery("from HDocument d where d.projectIteration.slug = :iterationSlug " +
               "and d.projectIteration.project.slug = :projectSlug " +
               "and d.docId = :docId")
               .setParameter("iterationSlug", iterationSlug)
               .setParameter("projectSlug", projectSlug)
               .setParameter("docId", docId)
               .uniqueResult();
      return doc;
   }
   
   public List<HDocument> getAllByProjectIteration(final String projectSlug, final String iterationSlug)
   {
      Session session = getSession();
      
      @SuppressWarnings("unchecked")
      final List<HDocument> documents =
         session.createQuery("from HDocument d " +
         		"where d.projectIteration.slug = :iterationSlug " +
         		"and d.projectIteration.project.slug = :projectSlug")
         		.setParameter("iterationSlug", iterationSlug)
         		.setParameter("projectSlug", projectSlug)
         		.list();
      return documents;
   }

}
