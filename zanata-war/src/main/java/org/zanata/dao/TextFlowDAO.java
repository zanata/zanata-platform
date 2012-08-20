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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.CaseInsensitiveNgramAnalyzer;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.util.HTextFlowPosComparator;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long>
{
   //TODO replace all getSession() code to use entityManager
   private static final Version LUCENE_VERSION = Version.LUCENE_29;

   @In
   private FullTextEntityManager entityManager;

   @In
   LocaleDAO localeDAO;

   public TextFlowDAO()
   {
      super(HTextFlow.class);
   }

   public TextFlowDAO(Session session)
   {
      super(HTextFlow.class, session);
   }

   public HTextFlow getById(HDocument document, String id)
   {
      Criteria cr = getSession().createCriteria(HTextFlow.class);
      cr.add(Restrictions.naturalId().set("resId", id).set("document", document));
      cr.setCacheable(true).setComment("TextFlowDAO.getById");
      return (HTextFlow) cr.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> findByIdList(List<Long> idList)
   {
      if (idList == null || idList.isEmpty())
      {
         return new ArrayList<HTextFlow>();
      }
      Query query = getSession().createQuery("FROM HTextFlow WHERE id in (:idList)");
      query.setParameterList("idList", idList);
      // caching could be expensive for long idLists
      query.setCacheable(false).setComment("TextFlowDAO.getByIdList");
      return query.list();
   }

   public HTextFlow getObsoleteById(HDocument document, String id)
   {
      Criteria cr = getSession().createCriteria(HTextFlow.class);
      cr.add(Restrictions.naturalId().set("resId", id).set("document", document)).add(Restrictions.eq("obsolete", true));
      cr.setCacheable(true).setComment("TextFlowDAO.getObsoleteById");
      return (HTextFlow) cr.uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getNavigationByDocumentId(Long documentId)
   {
      Criteria c = getSession().createCriteria(HTextFlow.class);
      c.add(Restrictions.eq("document.id", documentId)).add(Restrictions.eq("obsolete", false));
      c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
      c.setCacheable(true).setComment("TextFlowDAO.getNavigationByDocumentId");

      c.addOrder(Order.asc("pos"));

      return c.list();
   }

   public List<Object[]> getSearchResult(TransMemoryQuery query, LocaleId locale, final int maxResult) throws ParseException
   {
      String queryText = null;
      String[] multiQueryText = null;

      switch (query.getSearchType())
      {
      // 'Lucene' in the editor
      case RAW:
         queryText = query.getQueries().get(0);
         break;

      // 'Fuzzy' in the editor
      case FUZZY:
         queryText = QueryParser.escape(query.getQueries().get(0));
         break;

      // 'Phrase' in the editor
      case EXACT:
         queryText = "\"" + QueryParser.escape(query.getQueries().get(0)) + "\"";
         break;

      // 'Fuzzy' in the editor, plus it is a plural entry
      case FUZZY_PLURAL:
         multiQueryText = new String[query.getQueries().size()];
         for (int i = 0; i < query.getQueries().size(); i++)
         {
            multiQueryText[i] = QueryParser.escape(query.getQueries().get(i));
         }
         break;
      default:
         throw new RuntimeException("Unknown query type: " + query.getSearchType());
      }

      org.apache.lucene.search.Query textQuery;
      // Analyzer determined by the language
      String analyzerDefName = TextContainerAnalyzerDiscriminator.getAnalyzerDefinitionName( locale.getId() );
      Analyzer analyzer = entityManager.getSearchFactory().getAnalyzer(analyzerDefName);

      if (query.getSearchType() == SearchType.FUZZY_PLURAL)
      {
         int queriesSize = multiQueryText.length;
         if (queriesSize > IndexFieldLabels.TF_CONTENT_FIELDS.length)
         {
            log.warn("query contains {} fields, but we only index {}", queriesSize, IndexFieldLabels.TF_CONTENT_FIELDS.length);
         }
         String[] searchFields = new String[queriesSize];
         System.arraycopy(IndexFieldLabels.TF_CONTENT_FIELDS, 0, searchFields, 0, queriesSize);

         textQuery = MultiFieldQueryParser.parse(LUCENE_VERSION, multiQueryText, searchFields, analyzer);
      }
      else
      {
         MultiFieldQueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, IndexFieldLabels.TF_CONTENT_FIELDS, analyzer);
         textQuery = parser.parse(queryText);
      }
      FullTextQuery ftQuery = entityManager.createFullTextQuery(textQuery, HTextFlowTarget.class);

      ftQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
      @SuppressWarnings("unchecked")
      List<Object[]> matches = ftQuery.setMaxResults(maxResult).getResultList();
      return matches;
   }

   public int getTotalWords()
   {
      Query q = getSession().createQuery("select sum(tf.wordCount) from HTextFlow tf where tf.obsolete=0");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalWords");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount == null ? 0 : totalCount.intValue();
   }

   public int getTotalTextFlows()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalTextFlows");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount == null ? 0 : totalCount.intValue();
   }

   public int getTotalActiveTextFlows()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow tf where tf.obsolete=0");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalActiveTextFlows");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount == null ? 0 : totalCount.intValue();
   }

   public int getTotalObsoleteTextFlows()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow tf where tf.obsolete=1");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalObsoleteTextFlows");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount == null ? 0 : totalCount.intValue();
   }

   public int getCountByDocument(Long documentId)
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
      q.setParameter("id", documentId);
      q.setCacheable(true).setComment("TextFlowDAO.getCountByDocument");
      Long totalCount = (Long) q.uniqueResult();
      return totalCount == null ? 0 : totalCount.intValue();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getTextFlows(Long documentId)
   {
      Query q = getSession().createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
      q.setParameter("id", documentId);
      q.setCacheable(true).setComment("TextFlowDAO.getTransUnitList");
      return q.list();
   }

   // TODO: use hibernate search
   @SuppressWarnings("unchecked")
   public List<Long> getNavigationBy(Long documentId, String search, LocaleId localeId)
   {
      Query textFlowQuery;
      Query textFlowTargetQuery;
      Set<Long> idSet;

      textFlowQuery = getSession().createQuery("select tf.id from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content order by tf.pos");
      textFlowQuery.setParameter("id", documentId);
      textFlowQuery.setParameter("content", "%" + search + "%");
      textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getNavigationByTF-rev");

      textFlowTargetQuery = getSession().createQuery("select tft.textFlow.id from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId order by tft.textFlow.pos");
      textFlowTargetQuery.setParameter("id", documentId);
      textFlowTargetQuery.setParameter("content", "%" + search + "%");
      textFlowTargetQuery.setParameter("localeId", localeId);
      textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getNavigationByTFT-rev");
      idSet = new TreeSet<Long>(new Comparator<Long>()
      {
         @Override
         public int compare(Long arg0, Long arg1)
         {
            return ((Long) arg0).compareTo((Long) arg1);
         }
      });

      List<Long> ids1 = textFlowQuery.list();
      List<Long> ids2 = textFlowTargetQuery.list();
      idSet.addAll(ids1);
      idSet.addAll(ids2);

      return new ArrayList<Long>(idSet);
   }

   /**
    * for a given locale, we first find text flow where has no target (targets map has no key equals the locale),
    * or (the text flow target has zero size contents OR content state is NEW).
    *
    * @param documentId document id (NOT the String type docId)
    * @param hLocale locale
    * @return a list of HTextFlow that has no translation for given locale.
    */
   public List<HTextFlow> getAllUntranslatedTextFlowByDocumentId(DocumentId documentId, HLocale hLocale)
   {
      // @formatter:off
      String query = "select distinct tf from HTextFlow tf left join tf.targets " +
            "where tf.obsolete = 0 and tf.document.id = :docId and " +
            "(:locale not in indices(tf.targets) or exists " + //text flow does not have a target for given locale
            "  (select tft.id from HTextFlowTarget tft where tft.textFlow.id = tf.id and tft.locale = :locale and tft.state = :contentState)" + //text flow has target but target has either empty contents or content state is NEW
            ") order by tf.pos";
      // @formatter:on

      Query textFlowQuery = getSession().createQuery(query);
      textFlowQuery.setParameter("docId", documentId.getId());
      textFlowQuery.setParameter("locale", hLocale);
      textFlowQuery.setParameter("contentState", ContentState.New);
      textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getAllUntranslatedTextFlowByDocId");

      @SuppressWarnings("unchecked")
      List<HTextFlow> result = textFlowQuery.list();
      log.debug("doc {} has {} untranslated textFlow for locale {}",
            new Object [] { documentId, result.size(), hLocale.getLocaleId()});
      return result;
   }

   public List<HTextFlow> getTextFlowsByStatus(DocumentId documentId, HLocale hLocale, boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated)
   {
      List<HTextFlow> result = Lists.newArrayList();
      List<HTextFlow> untranslated = Lists.newArrayList();
      List<HTextFlow> translated = Lists.newArrayList();

      if (filterUntranslated)
      {
         //hard part. leave it alone.
         untranslated = getAllUntranslatedTextFlowByDocumentId(documentId, hLocale);
         result.addAll(untranslated);
      }
      if (filterNeedReview || filterTranslated)
      {
         // @formatter:off
         String queryString = "select distinct tf from HTextFlow tf inner join tf.targets as tft " +
               "where tf.document.id = :docId and tft.locale = :locale and tft.state in (:contentStates) " +
               "order by tf.pos";
         // @formatter:on
         List<ContentState> contentStates = Lists.newArrayList();
         if (filterNeedReview)
         {
            contentStates.add(ContentState.NeedReview);
         }
         if (filterTranslated)
         {
            contentStates.add(ContentState.Approved);
         }
         Query query = getSession().createQuery(queryString);
         query.setParameter("docId", documentId.getId());
         query.setParameter("locale", hLocale);
         query.setParameterList("contentStates", contentStates);
         query.setCacheable(true).setComment("TextFlowDAO.getTextFlowsByStatus");

         translated = query.list();
         result.addAll(translated);
      }

      if (!untranslated.isEmpty() && !translated.isEmpty())
      {
         Collections.sort(result, HTextFlowPosComparator.INSTANCE);
      }
      return result;
   }
}
