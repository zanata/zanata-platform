package org.zanata.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
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
      return (HTextFlowTarget) getSession().createCriteria(HTextFlowTarget.class).add(Restrictions.naturalId().set("textFlow", textFlow).set("locale", locale)).setCacheable(true).setComment("TextFlowTargetDAO.getByNaturalId").uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlowTarget> findAllTranslations(HDocument document, LocaleId localeId)
   {
      // @formatter:off
      return getSession().createQuery(
         "select t from HTextFlowTarget t where " + 
         "t.textFlow.document =:document " +
         "and t.locale.localeId =:localeId " + 
         "and t.state !=:state " + 
         "order by t.textFlow.pos")
            .setParameter("document", document)
            .setParameter("localeId", localeId)
            .setParameter("state", ContentState.New)
            .list();
      // @formatter:on
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlowTarget> findTranslations(HDocument document, LocaleId localeId)
   {
      // @formatter:off
      return getSession().createQuery(
         "select t from HTextFlowTarget t where " + 
         "t.textFlow.document =:document " +
         "and t.locale.localeId =:localeId " + 
         "and t.state !=:state " +
         "and t.textFlow.obsolete=false " + 
         "order by t.textFlow.pos")
            .setParameter("document", document)
            .setParameter("localeId", localeId)
            .setParameter("state", ContentState.New)
            .list();      
      // @formatter:on
   }
   
   public HTextFlowTarget findLatestEquivalentTranslation(HTextFlow textFlow, HLocale locale)
   {
      // @formatter:off
      return (HTextFlowTarget) getSession().createQuery(
         "select t from HTextFlowTarget t " +
         "where t.textFlow.resId = :resid " +
         "and t.textFlow.content = :content " +
         "and t.textFlow.document.docId =:docId " +
         "and t.locale = :locale " +
         "and t.state = :state " +
         "order by t.lastChanged desc")
            .setParameter("content", textFlow.getContent())
            .setParameter("docId", textFlow.getDocument().getDocId())
            .setParameter("locale", locale)
            .setParameter("resid", textFlow.getResId())
            .setParameter("state", ContentState.Approved)
            .setMaxResults(1).uniqueResult();
      // @formatter:on
   }
   

}
