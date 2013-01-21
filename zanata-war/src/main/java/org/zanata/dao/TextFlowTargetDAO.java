package org.zanata.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

@Name("textFlowTargetDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowTargetDAO extends AbstractDAOImpl<HTextFlowTarget, Long>
{

   @In
   private EntityManager entityManager;

   public TextFlowTargetDAO()
   {
      super(HTextFlowTarget.class);
   }

   public TextFlowTargetDAO(Session session)
   {
      super(HTextFlowTarget.class, session);
   }

   /**
    * @param textFlow
    * @param localeId
    * @return
    */
   public HTextFlowTarget getByNaturalId(HTextFlow textFlow, HLocale locale)
   {
      Criteria cr = getSession().createCriteria(HTextFlowTarget.class);
      cr.add(Restrictions.naturalId().set("textFlow", textFlow).set("locale", locale));
      cr.setCacheable(true).setComment("TextFlowTargetDAO.getByNaturalId");
      return (HTextFlowTarget) cr.uniqueResult();
   }

   public int getTotalTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalTextFlowTargets");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalActiveTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.textFlow.obsolete=0");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalActiveTextFlowTargets");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalObsoleteTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.textFlow.obsolete=1");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalObsoleteTextFlowTargets");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalApprovedTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalApprovedTextFlowTargets");
      Long totalCount = (Long) q.setParameter("state", ContentState.Approved).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalNeedReviewTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalNeedReviewTextFlowTargets");
      Long totalCount = (Long) q.setParameter("state", ContentState.NeedReview).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalNewTextFlowTargets()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalNewTextFlowTargets");
      Long totalCount = (Long) q.setParameter("state", ContentState.New).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalApprovedWords()
   {
      Query q = getSession().createQuery("select sum(t.textFlow.wordCount) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalApprovedWords");
      Long totalCount = (Long) q.setParameter("state", ContentState.Approved).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalNeedReviewWords()
   {
      Query q = getSession().createQuery("select sum(t.textFlow.wordCount) from HTextFlowTarget t where t.state = :state and t.textFlow.obsolete=0");
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.getTotalNeedReviewWords");
      Long totalCount = (Long) q.setParameter("state", ContentState.NeedReview).uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   /**
    * Finds all (including obsolete) translations for 'document' in 'locale'.
    * @param document
    * @param locale
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<HTextFlowTarget> findAllTranslations(HDocument document, LocaleId localeId)
   {
      // @formatter:off
      Query q = getSession().createQuery(
            "select t from HTextFlowTarget t where " + 
            "t.textFlow.document =:document " +
            "and t.locale.localeId =:localeId " + 
            "order by t.textFlow.pos");
      q.setParameter("document", document);
      q.setParameter("localeId", localeId);
      q.setCacheable(false);
      q.setComment("TextFlowTargetDAO.findAllTranslations");
      return q.list();
      // @formatter:on
   }

   /**
    * Finds non-obsolete translations for 'document' in 'locale'.
    * @param document
    * @param locale
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<HTextFlowTarget> findTranslations(HDocument document, HLocale locale)
   {
      // @formatter:off
      Query q = getSession().createQuery(
            "select t " +
                  "from HTextFlowTarget t where " +
                  "t.textFlow.document =:document " +
                  "and t.locale =:locale " +
                  "and t.textFlow.obsolete=false " +
                  "order by t.textFlow.pos");
      q.setParameter("document", document);
      q.setParameter("locale", locale);
      q.setCacheable(true);
      q.setComment("TextFlowTargetDAO.findTranslations");
      return q.list();
      // @formatter:on
   }

   /**
    * Finds matching translations for a given document and locale.
    *
    * @param document The document for which to find equivalent translations.
    * @param locale The locale. Only translations for this locale are fetched.
    * @param checkContext Whether to check the text flow's context for matches.
    * @param checkDocument Whether to check the text flow's document for matches.
    * @param checkProject Whether to check the text flow's project for matches.
    * @return A scrollable result set (in case there is a large result set). Position 0 of the
    * result set is the matching translation (HTextFlowTarget), position 1 is the HTextFlow
    * in the document that it matches against.
    */
   public ScrollableResults findMatchingTranslations(HDocument document, HLocale locale, boolean checkContext, boolean checkDocument, boolean checkProject)
   {
      StringBuilder queryStr = new StringBuilder(
"select textFlow, max(match.id) " +
            "from HTextFlowTarget match, HTextFlow textFlow " +
      // "join fetch match.textFlow " +
            "where " +
            "textFlow.document = :document " +
            "and textFlow.contentHash = match.textFlow.contentHash " +
            "and match.locale = :locale " +
            "and match.state = :approvedState " +
            // Do not fetch results for already approved text flow targets
            "and (match.locale not in indices(textFlow.targets) " +
               "or :approvedState != (select t.state from HTextFlowTarget t where t.textFlow = textFlow and t.locale = :locale) ) " +
            // Do not reuse its own translations
            "and match.textFlow != textFlow "
      );
      if( checkContext )
      {
         queryStr.append("and match.textFlow.resId = textFlow.resId ");
      }
      if( checkDocument )
      {
         queryStr.append("and match.textFlow.document.docId = textFlow.document.docId ");
      }
      if( checkProject )
      {
         queryStr.append("and match.textFlow.document.projectIteration.project = textFlow.document.projectIteration.project ");
      }
      queryStr.append("group by textFlow");

      Query q = getSession().createQuery( queryStr.toString() );

      q.setParameter("document", document)
       .setParameter("locale", locale)
       .setParameter("approvedState", ContentState.Approved);
      q.setCacheable(false); // don't try to cache scrollable results
      q.setComment("TextFlowTargetDAO.findMatchingTranslations");
      return q.scroll();
   }

   /**
    * Look up the {@link HTextFlowTarget} for the given hLocale in hTextFlow,
    * creating a new one if none is present.
    *
    * @param hTextFlow The parent text flow.
    * @param hLocale The locale for the text flow target.
    */
   public HTextFlowTarget getOrCreateTarget(HTextFlow hTextFlow, HLocale hLocale)
   {
      HTextFlowTarget hTextFlowTarget = getTextFlowTarget(hTextFlow, hLocale);

      if (hTextFlowTarget == null)
      {
         hTextFlowTarget = new HTextFlowTarget(hTextFlow, hLocale);
         hTextFlowTarget.setVersionNum(0); // this will be incremented when content is set (below)
         hTextFlow.getTargets().put(hLocale.getId(), hTextFlowTarget);
      }
      return hTextFlowTarget;
   }

   /**
    * Look up the {@link HTextFlowTarget} for the given hLocale in hTextFlow.
    * If none can be found, return null.
    *
    * @param hTextFlow The parent text flow.
    * @param hLocale The locale for the text flow target.
    */
   public HTextFlowTarget getTextFlowTarget(HTextFlow hTextFlow, HLocale hLocale)
   {
      HTextFlowTarget hTextFlowTarget =
            (HTextFlowTarget)getSession().createQuery(
                     "select tft from HTextFlowTarget tft where tft.textFlow = :textFlow and tft.locale = :locale")
                  .setParameter("textFlow", hTextFlow)
                  .setParameter("locale", hLocale)
                  .setComment("TextFlowTargetDAO.getTextFlowTarget")
                  .uniqueResult();
      return hTextFlowTarget;
   }
}
