/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.hibernate.search.ConfigurableNgramAnalyzer;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.util.TextFlowFilter;

/**
 * @author David Mason, damason@redhat.com
 */
@Name("textFlowSearchServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TextFlowSearchServiceImpl implements TextFlowSearchService
{

   @Logger
   Log log;

   @In
   TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   DocumentDAO documentDAO;

   @In
   TextFlowFilter textFlowFilterImpl;

   @In
   private FullTextEntityManager entityManager;

   @Override
   public List<HTextFlowTarget> findTextFlowTargets(WorkspaceId workspace, FilterConstraints constraints)
   {
      LocaleId localeId = workspace.getLocaleId();
      String projectSlug = workspace.getProjectIterationId().getProjectSlug();
      String iterationSlug = workspace.getProjectIterationId().getIterationSlug();

      // TODO consider whether to allow null and empty search strings.
      // May want to fork to use a different method to retrieve all targets if
      // empty targets are required.

      // check that locale is valid for the workspace
      try
      {
         localeServiceImpl.validateLocaleByProjectIteration(localeId, projectSlug, iterationSlug);
      }
      catch (ZanataServiceException e)
      {
         throw new ZanataServiceException("Failed to validate locale", e);
      }

      if (!constraints.isSearchInSource() && !constraints.isSearchInTarget())
      {
         //searching nowhere
         return new ArrayList<HTextFlowTarget>();
      }

      // FIXME remove .trim() and zero-length check when ngram analyzer is updated to respect leading and trailing whitespace
      int searchLength = Math.min(3, constraints.getSearchString().trim().length());
      if (searchLength == 0)
      {
         return new ArrayList<HTextFlowTarget>();
      }
      Analyzer ngramAnalyzer = new ConfigurableNgramAnalyzer(searchLength, !constraints.isCaseSensitive());

      String[] searchFields = (constraints.isCaseSensitive() ? IndexFieldLabels.CONTENT_FIELDS_CASE_PRESERVED : IndexFieldLabels.CONTENT_FIELDS_CASE_FOLDED);

      Query searchPhraseQuery;
      QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, searchFields, ngramAnalyzer);
      try
      {
         searchPhraseQuery = parser.parse("\"" + QueryParser.escape(constraints.getSearchString()) + "\"");
      }
      catch (ParseException e)
      {
         throw new ZanataServiceException("Failed to parse query", e);
      }

      TermQuery projectQuery = new TermQuery(new Term(IndexFieldLabels.PROJECT_FIELD, projectSlug));
      TermQuery iterationQuery = new TermQuery(new Term(IndexFieldLabels.ITERATION_FIELD, iterationSlug));
      TermQuery localeQuery = new TermQuery(new Term(IndexFieldLabels.LOCALE_ID_FIELD, localeId.getId()));

      BooleanQuery sourceQuery = new BooleanQuery();
      sourceQuery.add(projectQuery, Occur.MUST);
      sourceQuery.add(iterationQuery, Occur.MUST);
      sourceQuery.add(searchPhraseQuery, Occur.MUST);

      List<HTextFlowTarget> resultList = new ArrayList<HTextFlowTarget>();
      if (constraints.isSearchInTarget())
      {
         BooleanQuery targetQuery = (BooleanQuery) sourceQuery.clone();
         targetQuery.add(localeQuery, Occur.MUST);
         if (!constraints.isIncludeApproved())
         {
            TermQuery approvedStateQuery = new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.Approved.toString()));
            targetQuery.add(approvedStateQuery, Occur.MUST_NOT);
         }

         if (!constraints.isIncludeFuzzy())
         {
            TermQuery approvedStateQuery = new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.NeedReview.toString()));
            targetQuery.add(approvedStateQuery, Occur.MUST_NOT);
         }

         if (!constraints.isIncludeNew())
         {
            TermQuery approvedStateQuery = new TermQuery(new Term(IndexFieldLabels.CONTENT_STATE_FIELD, ContentState.New.toString()));
            targetQuery.add(approvedStateQuery, Occur.MUST_NOT);
         }

         FullTextQuery ftQuery = entityManager.createFullTextQuery(targetQuery, HTextFlowTarget.class);
         @SuppressWarnings("unchecked")
         List<HTextFlowTarget> matchedTargets = (List<HTextFlowTarget>) ftQuery.getResultList();
         log.info("got {0} HTextFLowTarget results", matchedTargets.size());
         resultList.addAll(matchedTargets);
      }

      if (constraints.isSearchInSource())
      {
         FullTextQuery ftQuery = entityManager.createFullTextQuery(sourceQuery, HTextFlow.class);
         @SuppressWarnings("unchecked")
         List<HTextFlow> matchedSources = (List<HTextFlow>) ftQuery.getResultList();
         log.info("got {0} HTextFLowTarget results", matchedSources.size());
         HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
         for (HTextFlow htf : matchedSources)
         {
            HTextFlowTarget htft = htf.getTargets().get(hLocale);
            if (htft != null && htft.getState() != ContentState.New)
            {
               // TODO filter other states?
               if (!resultList.contains(htft))
               {
                  resultList.add(htf.getTargets().get(hLocale));
               }
            }
         }
      }

      return resultList;
   }

   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, DocumentId doc, FilterConstraints constraints)
   {
      // TODO Implement findTextFlows within document
      return null;
   }
}
