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
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.util.OpenBitSet;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.ResultTransformer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.search.FilterConstraintToQuery;
import org.zanata.search.FilterConstraints;
import org.zanata.webtrans.shared.model.DocumentId;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Name("textFlowDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TextFlowDAO extends AbstractDAOImpl<HTextFlow, Long>
{
   // TODO replace all getSession() code to use entityManager

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

   @SuppressWarnings("unchecked")
   public OpenBitSet findIdsWithTranslations(LocaleId locale)
   {
      Query q = getSession().getNamedQuery(HTextFlow.QUERY_TRANSLATED_TEXTFLOWIDS);
      q.setReadOnly(true);
      // MIN_VALUE gives hint to JDBC driver to stream results
      q.setFetchSize(Integer.MIN_VALUE);
      q.setParameter("locale", locale);
      // TranslatedFilter may need this caching when populating multiple bitsets (one per IndexReader)
      q.setCacheable(true).setComment("TextFlowDAO.findIdsWithTranslations");

      ScrollableResults results = q.scroll(ScrollMode.FORWARD_ONLY);
      OpenBitSet resultBitSet = new OpenBitSet();
      while( results.next() )
      {
         resultBitSet.set( results.getLong(0) ); // results[0] => TextFlow id
      }
      results.close();

      return resultBitSet;
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

   @SuppressWarnings("unchecked")
   public List<HTextFlow> getNavigationByDocumentId(Long documentId, HLocale hLocale, ResultTransformer resultTransformer, FilterConstraints filterConstraints)
   {
      StringBuilder queryBuilder = new StringBuilder();
      // I can't write a HQL or criteria to achieve the same result. I gave up...
      // @formatter:off
      queryBuilder
            .append("SELECT tf.id, tft.state FROM HTextFlow tf ")
            .append(" LEFT JOIN HTextFlowTarget tft on tf.id = tft.tf_id AND locale = :locale")
            .append(" WHERE tf.document_id = :docId AND tf.obsolete = 0");
      queryBuilder
            .append(" AND ")
            .append(buildContentStateCondition(filterConstraints.isIncludeApproved(), filterConstraints.isIncludeFuzzy(), filterConstraints.isIncludeNew(), "tft"));
      boolean hasSearchString = !Strings.isNullOrEmpty(filterConstraints.getSearchString());
      if (hasSearchString)
      {
         queryBuilder
            .append(" AND (")
            .append(buildSearchCondition(filterConstraints.getSearchString(), "tf")) // search in source
            .append(" OR ")
            .append(buildSearchCondition(filterConstraints.getSearchString(), "tft")) // search in target
            .append(")");
      }
      queryBuilder
            .append(" ORDER BY tf.pos");

      // @formatter:on
      log.debug("get navigation SQL query: {}", queryBuilder);
      Query query = getSession().createSQLQuery(queryBuilder.toString())
            .addScalar("id", Hibernate.LONG)
            .addScalar("state")
            .setParameter("docId", documentId)
            .setParameter("locale", hLocale.getId());
      if (hasSearchString)
      {
         query.setParameter("searchstringlowercase", "%" + filterConstraints.getSearchString().toLowerCase() + "%");
      }
      query.setResultTransformer(resultTransformer);
      query.setComment("TextFlowDAO.getNavigationByDocumentId");

      return query.list();
   }

   /**
    * This will build a SQL query condition in where clause.
    * If all status are equal (i.e. all true or all false), it's treated as accept all and it will return '1'.
    *
    * @param acceptApproved accept approved status
    * @param acceptFuzzy accept fuzzy status
    * @param acceptUntranslated accept untranslated status
    * @param alias HTextFlowTarget alias
    * @return '1' if accept all status or a SQL condition clause with target content state conditions in parentheses '()' joined by 'or'
    */
   protected static String buildContentStateCondition(boolean acceptApproved, boolean acceptFuzzy, boolean acceptUntranslated, String alias)
   {
      if (acceptApproved == acceptFuzzy && acceptApproved == acceptUntranslated)
      {
         return "1";
      }
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      List<String> conditions = Lists.newArrayList();
      final String column = alias + ".state";
      if (acceptApproved)
      {
         conditions.add(column + "=2");
      }
      if (acceptFuzzy)
      {
         conditions.add(column + "=1");
      }
      if (acceptUntranslated)
      {
         conditions.add(column + "=0 or " + column + " is null");
      }
      Joiner joiner = Joiner.on(" or ");
      joiner.appendTo(builder, conditions);
      builder.append(")");
      return builder.toString();
   }

   /**
    * This will build a SQL query condition in where clause.
    * It can be used to search string in content0, content1 ... content5 in HTextFlow or HTextFlowTarget.
    * If search term is empty it will return '1'
    *
    * @param searchString search term
    * @param alias table name alias
    * @return '1' if searchString is empty or a SQL condition clause with lower(contentX) like '%searchString%' in parentheses '()' joined by 'or'
    */
   protected static String buildSearchCondition(String searchString, String alias)
   {
      if (Strings.isNullOrEmpty(searchString))
      {
         return "1";
      }
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      List<String> conditions = Lists.newArrayList();
      for (int i = 0; i < 6; i++)
      {
         conditions.add("lower(" + alias + ".content" + i + ") LIKE :searchstringlowercase");
      }
      Joiner joiner = Joiner.on(" or ");
      joiner.appendTo(builder, conditions);
      builder.append(")");
      return builder.toString();
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
   public List<HTextFlow> getTextFlows(DocumentId documentId, int startIndex, int maxSize)
   {
      Query q = getSession().createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos");
      q.setParameter("id", documentId.getId());
      q.setFirstResult(startIndex);
      q.setMaxResults(maxSize);
      q.setCacheable(true).setComment("TextFlowDAO.getTextFlows");
      return q.list();
   }


   /**
    * for a given locale, we can filter it by content state or search in source and target.
    *
    *
    * @param documentId document id (NOT the String type docId)
    * @param hLocale locale
    * @param constraints filter constraints
    * @param firstResult start index
    * @param maxResult max result
    * @return a list of HTextFlow that matches the constraint.
    * @see org.zanata.service.impl.TextFlowSearchServiceImpl#findTextFlows(org.zanata.webtrans.shared.model.WorkspaceId, org.zanata.search.FilterConstraints)
    */
   public List<HTextFlow> getTextFlowByDocumentIdWithConstraint(DocumentId documentId, HLocale hLocale, FilterConstraints constraints, int firstResult, int maxResult)
   {
      FilterConstraintToQuery constraintToQuery = FilterConstraintToQuery.filterInSingleDocument(constraints, documentId);
      String queryString = constraintToQuery.toHQL();
      log.debug("\n query {}\n", queryString);

      Query textFlowQuery = getSession().createQuery(queryString);
      constraintToQuery.setQueryParameters(textFlowQuery, hLocale);
      textFlowQuery.setFirstResult(firstResult).setMaxResults(maxResult);
      textFlowQuery.setCacheable(true).setComment("TextFlowDAO.getTextFlowByDocumentIdWithConstraint");

      @SuppressWarnings("unchecked")
      List<HTextFlow> result = textFlowQuery.list();
      log.debug("{} textFlow for locale {} filter by {}", new Object[] { result.size(), hLocale.getLocaleId(), constraints });
      return result;
   }
}
