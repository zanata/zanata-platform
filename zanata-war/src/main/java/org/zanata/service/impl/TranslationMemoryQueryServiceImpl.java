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
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationMemoryQueryService;
import org.zanata.service.TranslationStateCache;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

@Name("translationMemoryQueryService")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TranslationMemoryQueryServiceImpl implements TranslationMemoryQueryService
{
   private static final Version LUCENE_VERSION = Version.LUCENE_29;

   @In
   private FullTextEntityManager entityManager;

   @In
   private TranslationStateCache translationStateCacheImpl;

   @Override
   public List<Object[]> getSearchResult(TransMemoryQuery query, LocaleId sourceLocale,
         LocaleId targetLocale, final int maxResult, boolean useTargetIndex) throws ParseException
   {
      String queryText = null;
      String[] multiQueryText = null;

      switch (query.getSearchType())
      {
      // 'Lucene' in the editor
      case RAW:
         queryText = query.getQueries().get(0);
         if (StringUtils.isBlank(queryText))
         {
            return new ArrayList<Object[]>();
         }
         break;

      // 'Fuzzy' in the editor
      case FUZZY:
         queryText = QueryParser.escape(query.getQueries().get(0));
         if (StringUtils.isBlank(queryText))
         {
            return new ArrayList<Object[]>();
         }
         break;

      // 'Phrase' in the editor
      case EXACT:
         queryText = "\"" + QueryParser.escape(query.getQueries().get(0)) + "\"";
         if (StringUtils.isBlank(queryText))
         {
            return new ArrayList<Object[]>();
         }
         break;

      // 'Fuzzy' in the editor, plus it is a plural entry
      case FUZZY_PLURAL:
         multiQueryText = new String[query.getQueries().size()];
         for (int i = 0; i < query.getQueries().size(); i++)
         {
            multiQueryText[i] = QueryParser.escape(query.getQueries().get(i));
            if (StringUtils.isBlank(multiQueryText[i]))
            {
               return new ArrayList<Object[]>();
            }
         }
         break;
      default:
         throw new RuntimeException("Unknown query type: " + query.getSearchType());
      }

      FullTextQuery ftQuery;
      if (useTargetIndex)
      {
         org.apache.lucene.search.Query textQuery = generateQuery(query, sourceLocale, targetLocale, queryText, multiQueryText, IndexFieldLabels.TF_CONTENT_FIELDS, useTargetIndex);
         ftQuery = entityManager.createFullTextQuery(textQuery, HTextFlowTarget.class);
      }
      else
      {
         org.apache.lucene.search.Query textQuery = generateQuery(query, sourceLocale, targetLocale, queryText, multiQueryText, IndexFieldLabels.CONTENT_FIELDS, useTargetIndex);
         ftQuery = entityManager.createFullTextQuery(textQuery, HTextFlow.class);
         Filter filter = translationStateCacheImpl.getFilter(targetLocale);
         ftQuery.setFilter(filter);
      }

      ftQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
      @SuppressWarnings("unchecked")
      List<Object[]> matches = ftQuery.setMaxResults(maxResult).getResultList();
      return matches;
   }

   /**
    * Generate HTextFlowTarget query with matching HTextFlow contents,
    * HTextFlowTarget locale, HTextFlowTarget state = Approved
    * 
    * @param query
    * @param sourceLocale
    * @param targetLocale
    * @param queryText
    * @param multiQueryText
    * @param contentFields
    * @return
    * @throws ParseException
    */
   private org.apache.lucene.search.Query generateQuery(TransMemoryQuery query, LocaleId sourceLocale, LocaleId targetLocale, String queryText, String[] multiQueryText, String contentFields[], boolean useTargetIndex) throws ParseException
   {
      org.apache.lucene.search.Query contentQuery;
      // Analyzer determined by the language
      String analyzerDefName = TextContainerAnalyzerDiscriminator.getAnalyzerDefinitionName(sourceLocale.getId());
      Analyzer analyzer = entityManager.getSearchFactory().getAnalyzer(analyzerDefName);

      if (query.getSearchType() == SearchType.FUZZY_PLURAL)
      {
         int queriesSize = multiQueryText.length;
         if (queriesSize > contentFields.length)
         {
            log.warn("query contains {} fields, but we only index {}", queriesSize, contentFields.length);
         }
         String[] searchFields = new String[queriesSize];
         System.arraycopy(contentFields, 0, searchFields, 0, queriesSize);

         contentQuery = MultiFieldQueryParser.parse(LUCENE_VERSION, multiQueryText, searchFields, analyzer);
      }
      else
      {
         MultiFieldQueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, contentFields, analyzer);
         contentQuery = parser.parse(queryText);
      }

      if (useTargetIndex)
      {
         TermQuery localeQuery = new TermQuery(new Term(IndexFieldLabels.LOCALE_ID_FIELD, targetLocale.getId()));
         
         TermQuery newStateQuery = new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.New.toString()));
         TermQuery needReviewStateQuery = new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.NeedReview.toString()));
         TermQuery rejectedReviewStateQuery = new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.Rejected.toString()));
         
         BooleanQuery targetQuery = new BooleanQuery();
         targetQuery.add(contentQuery, Occur.MUST);
         targetQuery.add(localeQuery, Occur.MUST);
         
         targetQuery.add(newStateQuery, Occur.MUST_NOT);
         targetQuery.add(needReviewStateQuery, Occur.MUST_NOT);
         targetQuery.add(rejectedReviewStateQuery, Occur.MUST_NOT);

         return targetQuery;
      }
      else
      {
         return contentQuery;
      }
   }

}
