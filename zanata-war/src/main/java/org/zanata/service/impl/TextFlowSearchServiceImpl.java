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
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
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
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.hibernate.search.ConfigurableNgramAnalyzer;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
@Name("textFlowSearchServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TextFlowSearchServiceImpl implements TextFlowSearchService
{

   @In
   TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   DocumentDAO documentDAO;

   @In
   private FullTextEntityManager entityManager;

   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, FilterConstraints constraints)
   {
      return findTextFlowsByDocumentPaths(workspace, null, constraints);
   }

   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, List<String> documents, FilterConstraints constraints)
   {
      return findTextFlowsByDocumentPaths(workspace, documents, constraints);
   }

   /**
    * @param workspace
    * @param documentPaths null or empty to search entire project, otherwise
    *           only results for the given document paths will be returned
    * @param constraints
    * @return
    */
   private List<HTextFlow> findTextFlowsByDocumentPaths(WorkspaceId workspace, List<String> documentPaths, FilterConstraints constraints)
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
         return Collections.emptyList();
      }

      // FIXME remove .trim() and zero-length check when ngram analyzer is updated to respect leading and trailing whitespace
      int searchLength = Math.min(3, constraints.getSearchString().trim().length());
      if (searchLength == 0)
      {
         return Collections.emptyList();
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

      if (documentPaths != null && !documentPaths.isEmpty())
      {
         ArrayList<Term> docPathTerms = new ArrayList<Term>();
         for (String s : documentPaths)
         {
            docPathTerms.add(new Term(IndexFieldLabels.DOCUMENT_ID_FIELD, s));
         }
         MultiPhraseQuery documentsQuery = new MultiPhraseQuery();
         documentsQuery.add(docPathTerms.toArray(new Term[docPathTerms.size()]));
         sourceQuery.add(documentsQuery, Occur.MUST);
      }

      List<HTextFlow> resultList = new ArrayList<HTextFlow>();
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
         log.info("got {} HTextFLowTarget results", matchedTargets.size());
         for (HTextFlowTarget htft : matchedTargets)
         {
            resultList.add(htft.getTextFlow());
         }
      }

      if (constraints.isSearchInSource())
      {
         FullTextQuery ftQuery = entityManager.createFullTextQuery(sourceQuery, HTextFlow.class);
         @SuppressWarnings("unchecked")
         List<HTextFlow> matchedSources = (List<HTextFlow>) ftQuery.getResultList();
         log.info("got {} HTextFLow results", matchedSources.size());
         for (HTextFlow htf : matchedSources)
         {
            if (!resultList.contains(htf))
            {
               resultList.add(htf);
            }
         }
      }

      return resultList;
   }

   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, DocumentId doc, FilterConstraints constraints)
   {
      //TODO this method has high percentage of duplication from the above method. Refactor.
      LocaleId localeId = workspace.getLocaleId();
      String projectSlug = workspace.getProjectIterationId().getProjectSlug();
      String iterationSlug = workspace.getProjectIterationId().getIterationSlug();

      localeServiceImpl.validateLocaleByProjectIteration(localeId, projectSlug, iterationSlug);

      if (!constraints.isSearchInSource() && !constraints.isSearchInTarget())
      {
         //searching nowhere
         return Collections.emptyList();
      }

      // FIXME remove .trim() and zero-length check when ngram analyzer is updated to respect leading and trailing whitespace
      int searchLength = Math.min(3, constraints.getSearchString().trim().length());
      if (searchLength == 0)
      {
         return Collections.emptyList();
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
      HDocument hDocument = documentDAO.getById(doc.getId());
      TermQuery documentQuery = new TermQuery(new Term(IndexFieldLabels.DOCUMENT_ID_FIELD, hDocument.getDocId()));

      BooleanQuery sourceQuery = new BooleanQuery();
      sourceQuery.add(projectQuery, Occur.MUST);
      sourceQuery.add(iterationQuery, Occur.MUST);
      sourceQuery.add(searchPhraseQuery, Occur.MUST);
      sourceQuery.add(documentQuery, Occur.MUST);

      List<HTextFlow> resultList = Lists.newArrayList();
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
         log.info("got {} HTextFLowTarget results", matchedTargets.size());
         List<HTextFlow> hTextFlows = Lists.transform(matchedTargets, new Function<HTextFlowTarget, HTextFlow>()
         {
            @Override
            public HTextFlow apply(HTextFlowTarget target)
            {
               return target.getTextFlow();
            }
         });
         resultList.addAll(hTextFlows);
      }

      if (constraints.isSearchInSource())
      {
         FullTextQuery ftQuery = entityManager.createFullTextQuery(sourceQuery, HTextFlow.class);
         @SuppressWarnings("unchecked")
         List<HTextFlow> matchedSources = (List<HTextFlow>) ftQuery.getResultList();
         log.info("got {} HTextFLow results", matchedSources.size());
         HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
         for (HTextFlow hTextFlow : matchedSources)
         {
            if (!resultList.contains(hTextFlow))
            {
               HTextFlowTarget hTextFlowTarget = hTextFlow.getTargets().get(hLocale.getId());
               if (isContentStateValid(hTextFlowTarget, constraints))
               {
                  resultList.add(hTextFlow);
               }
            }
         }
      }

      return resultList;
   }

   private static boolean isContentStateValid(HTextFlowTarget hTextFlowTarget, FilterConstraints constraints)
   {
      if (hTextFlowTarget == null)
      {
         return constraints.isIncludeNew();
      }
      else
      {
         ContentState state = hTextFlowTarget.getState();
         return (constraints.isIncludeApproved() && state == ContentState.Approved) ||
               (constraints.isIncludeFuzzy() && state == ContentState.NeedReview) ||
               (constraints.isIncludeNew() && state == ContentState.New);
      }
   }

}
