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
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.DefaultNgramAnalyzer;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlow;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long>
{
   @In
   private FullTextEntityManager entityManager;


   public TextFlowDAO()
   {
      super(HTextFlow.class);
   }

   public TextFlowDAO(Session session)
   {
      super(HTextFlow.class, session);
   }

   /**
    * @param document
    * @param id
    * @return
    */
   public HTextFlow getById(HDocument document, String id)
   {
      return (HTextFlow) getSession().createCriteria(HTextFlow.class).add(Restrictions.naturalId().set("resId", id).set("document", document)).setCacheable(true).setComment("TextFlowDAO.getById").uniqueResult();
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
      query.setComment("TextFlowDAO.getByIdList");
      return query.list();
   }

   public HTextFlow getObsoleteById(HDocument document, String id)
   {
      return (HTextFlow) getSession().createCriteria(HTextFlow.class).add(Restrictions.naturalId().set("resId", id).set("document", document)).add(Restrictions.eq("obsolete", true)).setCacheable(true).setComment("TextFlowDAO.getObsoleteById").uniqueResult();
   }

   @SuppressWarnings("unchecked")
   public List<Long> getIdsByTargetState(LocaleId locale, ContentState state)
   {
      Query q = getSession().createQuery("select tft.textFlow.id from HTextFlowTarget tft where tft.locale.localeId=:locale and tft.state=:state");
      q.setParameter("locale", locale);
      q.setParameter("state", state);
      q.setComment("TextFlowDAO.getIdsByTargetState");
      return q.list();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getNavigationByDocumentId(Long documentId, int offset, boolean reverse)
   {
      Criteria c = getSession().createCriteria(HTextFlow.class).add(Restrictions.eq("document.id", documentId)).add(Restrictions.eq("obsolete", false)).setComment("TextFlowDAO.getNavigationByDocumentId");

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

   public List<Object[]> getSearchResult(String searchText, SearchType searchType, List<Long> translatedIds, final int maxResult) throws ParseException
   {
      String queryText;
      switch (searchType)
      {
      case RAW:
         queryText = searchText;
         break;

      case FUZZY:
         // search by N-grams
         queryText = QueryParser.escape(searchText);
         break;

      case EXACT:
         queryText = "\"" + QueryParser.escape(searchText) + "\"";
         break;

      default:
         throw new RuntimeException("Unknown query type: " + searchType);
      }

      QueryParser parser = new QueryParser(Version.LUCENE_29, "content", new DefaultNgramAnalyzer());
      org.apache.lucene.search.Query textQuery = parser.parse(queryText);
      FullTextQuery ftQuery = entityManager.createFullTextQuery(textQuery, HTextFlow.class);
      ftQuery.enableFullTextFilter("textFlowFilter").setParameter("translatedIds", translatedIds);
      ftQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
      @SuppressWarnings("unchecked")
      List<Object[]> matches = ftQuery.setMaxResults(maxResult).getResultList();
      return matches;
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> findEquivalents(HTextFlow textFlow)
   {
      // @formatter:off
      return getSession().createQuery(
         "select tf from HTextFlow tf " +
         "where tf.resId = :resid " +
         "and tf.document.docId = :docId " +
         "and tf.document.projectIteration.project = :project " +
         "and tf.document.projectIteration != :iteration ")
            .setParameter("docId", textFlow.getDocument().getDocId())
            .setParameter("project", textFlow.getDocument().getProjectIteration().getProject())
            .setParameter("iteration", textFlow.getDocument().getProjectIteration())
            .setParameter("resid", textFlow.getResId())
            .list();
      // @formatter:on
   }

   @SuppressWarnings({ "unchecked" })
   public List<HTextFlow> getByDocument(Long documentId)
   {
      Query query = getSession().createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos").setParameter("id", documentId);
      return query.list();
   }

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getOffsetListByDocument(Long documentId, int offset, int count)
   {
      Query query = getSession().createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos").setParameter("id", documentId);
      return query.setFirstResult(offset).setMaxResults(count).list();
   }
   
   
   @SuppressWarnings("unchecked")
   // TODO: use hibernate search
   public Set<Object[]> getIdsBySearch(Long documentId, int offset, int count, String search, LocaleId localeId)
   {
      Query textFlowQuery = getSession().createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content order by tf.pos");
      textFlowQuery.setParameter("id", documentId);
      textFlowQuery.setParameter("content", "%" + search + "%");
      List<Object[]> ids1 = textFlowQuery.list();
      Query textFlowTargetQuery = getSession().createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId order by tft.textFlow.pos");
      textFlowTargetQuery.setParameter("id", documentId);
      textFlowTargetQuery.setParameter("content", "%" + search + "%");
      textFlowTargetQuery.setParameter("localeId", localeId);
      List<Object[]> ids2 = textFlowTargetQuery.list();
      Set<Object[]> idSet = new TreeSet<Object[]>(new Comparator<Object[]>()
      {
         @Override
         public int compare(Object[] arg0, Object[] arg1)
         {
            return ((Integer) arg0[1]).compareTo((Integer) arg1[1]);
         }
      });
      idSet.addAll(ids1);
      idSet.addAll(ids2);
      return idSet;
   }


   // TODO: use hibernate search
   @SuppressWarnings("unchecked")
   public Set<Object[]> getNavigationBy(Long documentId, String search, int offset, LocaleId localeId, boolean reverse)
   {
      Query textFlowQuery;
      Query textFlowTargetQuery;
      Set<Object[]> idSet;
      if (reverse)
      {
         textFlowQuery = getSession().createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content and tf.pos < :offset  order by tf.pos desc");
         textFlowQuery.setParameter("id", documentId);
         textFlowQuery.setParameter("content", "%" + search + "%");
         textFlowTargetQuery = getSession().createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId and tft.textFlow.pos < :offset order by tft.textFlow.pos desc");
         textFlowTargetQuery.setParameter("id", documentId);
         textFlowTargetQuery.setParameter("content", "%" + search + "%");
         textFlowTargetQuery.setParameter("localeId", localeId);
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
         textFlowTargetQuery = getSession().createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId and tft.textFlow.pos > :offset order by tft.textFlow.pos");
         textFlowTargetQuery.setParameter("id", documentId);
         textFlowTargetQuery.setParameter("content", "%" + search + "%");
         textFlowTargetQuery.setParameter("localeId", localeId);
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
