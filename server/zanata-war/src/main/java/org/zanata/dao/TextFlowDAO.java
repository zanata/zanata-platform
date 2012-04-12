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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.DefaultNgramAnalyzer;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlow;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long>
{
   private static final Version LUCENE_VERSION = Version.LUCENE_29;
   private static final String CONTENT_FIELDS[] = { "content0", "content1", "content2", "content3", "content4", "content5" };

   @In
   private FullTextEntityManager entityManager;

   @In
   LocaleDAO localeDAO;

   @Logger
   Log log;

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
   public List<Long> findIdsWithTranslations(LocaleId locale)
   {
      Query q = getSession().getNamedQuery("HTextFlow.findIdsWithTranslations");
      q.setParameter("locale", locale);
      // TextFlowFilter does its own caching, no need for double caching
      q.setCacheable(false).setComment("TextFlowDAO.findIdsWithTranslations");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getNavigationByDocumentId(Long documentId, int offset, boolean reverse)
   {
      Criteria c = getSession().createCriteria(HTextFlow.class);
      c.add(Restrictions.eq("document.id", documentId)).add(Restrictions.eq("obsolete", false));
      c.setCacheable(true).setComment("TextFlowDAO.getNavigationByDocumentId");

      if (reverse)
      {
         c.add(Restrictions.lt("pos", offset)).addOrder(Order.desc("pos"));
      }
      else
      {
         c.add(Restrictions.gt("pos", offset)).addOrder(Order.asc("pos"));
      }

      return c.list();

   }

   public List<Object[]> getSearchResult(TransMemoryQuery query, List<Long> translatedIds, final int maxResult) throws ParseException
   {
      String queryText = null;
      String[] multiQueryText = null;
      
      switch (query.getSearchType())
      {
      case RAW:
         queryText = query.getQueries().get(0);
         break;

      case FUZZY:
         // search by N-grams
         queryText = QueryParser.escape(query.getQueries().get(0));
         break;

      case EXACT:
         queryText = "\"" + QueryParser.escape(query.getQueries().get(0)) + "\"";
         break;

      case FUZZY_PLURAL:
         multiQueryText = new String[ query.getQueries().size() ];
         for(int i=0; i<query.getQueries().size(); i++)
         {
            multiQueryText[i] = QueryParser.escape( query.getQueries().get(i) );
         }
         break;
      default:
         throw new RuntimeException("Unknown query type: " + query.getSearchType());
      }

      org.apache.lucene.search.Query textQuery;
      DefaultNgramAnalyzer analyzer = new DefaultNgramAnalyzer();
      if (query.getSearchType() == SearchType.FUZZY_PLURAL)
      {
         int queriesSize = multiQueryText.length;
         if (queriesSize > CONTENT_FIELDS.length)
         {
            log.warn("query contains {0} fields, but we only index {1}", queriesSize, CONTENT_FIELDS.length);
         }
         String[] searchFields = new String[queriesSize];
         System.arraycopy(CONTENT_FIELDS, 0, searchFields, 0, queriesSize);

         textQuery = MultiFieldQueryParser.parse(LUCENE_VERSION, multiQueryText, searchFields, analyzer);
      }
      else
      {
         MultiFieldQueryParser parser = new MultiFieldQueryParser(
               LUCENE_VERSION, CONTENT_FIELDS, analyzer);
         textQuery = parser.parse(queryText);
      }
      FullTextQuery ftQuery = entityManager.createFullTextQuery(textQuery, HTextFlow.class);
      ftQuery.enableFullTextFilter("textFlowFilter").setParameter("ids", translatedIds);

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
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalTextFlows()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalTextFlows");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalActiveTextFlows()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow tf where tf.obsolete=0");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalActiveTextFlows");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getTotalObsoleteTextFlows()
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow tf where tf.obsolete=1");
      q.setCacheable(true).setComment("TextFlowDAO.getTotalObsoleteTextFlows");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   public int getCountByDocument(Long documentId)
   {
      Query q = getSession().createQuery("select count(*) from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
      q.setParameter("id", documentId);
      q.setCacheable(true).setComment("TextFlowDAO.getCountByDocument");
      Long totalCount = (Long) q.uniqueResult();
      if (totalCount == null)
         return 0;
      return totalCount.intValue();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getTransUnitList(Long documentId, int offset, int count)
   {
      Query q = getSession().createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
      q.setParameter("id", documentId);
      q.setFirstResult(offset).setMaxResults(count);
      q.setCacheable(true).setComment("TextFlowDAO.getTransUnitList");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getTransUnitList(Long documentId)
   {
      Query q = getSession().createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
      q.setParameter("id", documentId);
      q.setCacheable(true).setComment("TextFlowDAO.getTransUnitList");
      return q.list();
   }

   // TODO: use hibernate search
   @SuppressWarnings("unchecked")
   public Set<Object[]> getNavigationBy(Long documentId, String search, int offset, LocaleId localeId, boolean reverse)
   {
      Query textFlowQuery;
      Query textFlowTargetQuery;
      Set<Object[]> idSet;
      // TODO look at using Hibernate Search, because this is *expensive*
      if (reverse)
      {
         textFlowQuery = getSession().createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content and tf.pos < :offset  order by tf.pos desc");
         textFlowQuery.setParameter("id", documentId);
         textFlowQuery.setParameter("content", "%" + search + "%");
         textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getNavigationByTF-fwd");
         textFlowTargetQuery = getSession().createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId and tft.textFlow.pos < :offset order by tft.textFlow.pos desc");
         textFlowTargetQuery.setParameter("id", documentId);
         textFlowTargetQuery.setParameter("content", "%" + search + "%");
         textFlowTargetQuery.setParameter("localeId", localeId);
         textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getNavigationByTFT-fwd");
         idSet = new TreeSet<Object[]>(new Comparator<Object[]>()
         {
            @Override
            public int compare(Object[] arg0, Object[] arg1)
            {
               return ((Integer) arg1[1]).compareTo((Integer) arg0[1]);
            }
         });
      }
      else
      {
         textFlowQuery = getSession().createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content and tf.pos > :offset  order by tf.pos");
         textFlowQuery.setParameter("id", documentId);
         textFlowQuery.setParameter("content", "%" + search + "%");
         textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getNavigationByTF-rev");
         textFlowTargetQuery = getSession().createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId and tft.textFlow.pos > :offset order by tft.textFlow.pos");
         textFlowTargetQuery.setParameter("id", documentId);
         textFlowTargetQuery.setParameter("content", "%" + search + "%");
         textFlowTargetQuery.setParameter("localeId", localeId);
         textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getNavigationByTFT-rev");
         idSet = new TreeSet<Object[]>(new Comparator<Object[]>()
         {
            @Override
            public int compare(Object[] arg0, Object[] arg1)
            {
               return ((Integer) arg0[1]).compareTo((Integer) arg1[1]);
            }
         });
      }
      textFlowQuery.setParameter("offset", offset);
      textFlowTargetQuery.setParameter("offset", offset);

      List<Object[]> ids1 = textFlowQuery.list();
      List<Object[]> ids2 = textFlowTargetQuery.list();
      idSet.addAll(ids1);
      idSet.addAll(ids2);
      return idSet;
   }

}
