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
import java.util.Collection;
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
import org.hibernate.search.FullTextSession;
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
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.hibernate.search.IndexFieldLabels;
import org.zanata.hibernate.search.TextContainerAnalyzerDiscriminator;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;

import lombok.extern.slf4j.Slf4j;

import static org.zanata.util.QueryBuilder.select;
import static org.zanata.util.QueryBuilder.and;
import static org.zanata.util.QueryBuilder.or;

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
   private LocaleService localeServiceImpl;

   @In
   private DocumentDAO documentDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private FullTextEntityManager entityManager;

   @In
   private FullTextSession session;

   // Disabled for now, due to the need for a left join
   private static final boolean ENABLE_HQL_SEARCH = false;

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

      if (!constraints.isIncludeNew() && !constraints.isIncludeFuzzy() && !constraints.isIncludeApproved())
      {
         // including nothing
         return Collections.emptyList();
      }

      // FIXME this switch is provided for easy comparison of options before a final
      // decisions is made on which option to use. Remove before signing off on this.
      if (ENABLE_HQL_SEARCH)
      {
         return findTextFlowsWithDatabaseSearch(projectSlug, iterationSlug, localeId, documentPaths, constraints);
      }
      else
      {
         return findTextFlowsWithHibernateSearch(projectSlug, iterationSlug, localeId, documentPaths, constraints);
      }
   }

   /**
    *
    * @see org.zanata.dao.TextFlowDAO#getTextFlowByDocumentIdWithConstraint(org.zanata.webtrans.shared.model.DocumentId, org.zanata.model.HLocale, org.zanata.search.FilterConstraints, int, int)
    */
   private List<HTextFlow> findTextFlowsWithDatabaseSearch(String projectSlug, String iterationSlug,
         LocaleId validatedLocaleId, List<String> documentPaths, FilterConstraints constraints)
   {
      // TODO wrap in method for batching list of documents
      // assuming doclist has already been batched before this method call

      // FIXME this query needs to use a left join, at least when searching
      // source strings, because there may not be HTextFlowTarget for the
      // HTextFlow.

      HLocale loc = localeServiceImpl.getByLocaleId(validatedLocaleId);
      Long locId = loc.getId();

      ArrayList<String> projectDpcStateConstraints = new ArrayList<String>();
      projectDpcStateConstraints.add("tf.document.projectIteration.project.slug = :project");
      projectDpcStateConstraints.add("tf.document.projectIteration.slug = :iteration");

      boolean hasDocumentPaths = documentPaths != null && !documentPaths.isEmpty();
      if (hasDocumentPaths)
      {
         projectDpcStateConstraints.add("tf.document.docId in ( :doclist )");
      }

      ArrayList<ContentState> stateList = new ArrayList<ContentState>(2);
      boolean includeAllStates = constraints.isIncludeNew() && constraints.isIncludeFuzzy() && constraints.isIncludeApproved();
      if (!includeAllStates)
      {
         // a different approach is required to ensure that text flows with no
         // target are returned iff new state is included.
         if (constraints.isIncludeNew())
         {
            // exclude non-matching states (so that flows with no target will match)
            projectDpcStateConstraints.add("tf.targets[" + locId + "].state not in ( :statelist )");
            if (!constraints.isIncludeFuzzy())
            {
               stateList.add(ContentState.NeedReview);
            }
            if (!constraints.isIncludeApproved())
            {
               stateList.add(ContentState.Approved);
            }
         }
         else
         {
            // include matching states (so that flows with no target will not match)
            projectDpcStateConstraints.add("tf.targets[" + locId + "].state in ( :statelist )");
            if (constraints.isIncludeFuzzy())
            {
               stateList.add(ContentState.NeedReview);
            }
            if (constraints.isIncludeApproved())
            {
               stateList.add(ContentState.Approved);
            }
         }
      }

      ArrayList<String> contentCheckList = new ArrayList<String>(12);
      if (constraints.isSearchInSource())
      {
         for (int i = 0; i < 6; i++)
         {
            contentCheckList.add("tf.content" + i + " like :searchString");
         }
      }
      if (constraints.isSearchInTarget())
      {
         String contentPrefix = "tf.targets[" + locId + "].content";
         for (int i = 0; i < 6; i++)
         {
            contentCheckList.add(contentPrefix + i + " like :searchString");
         }
      }

      String[] contentChecks = contentCheckList.toArray(new String[contentCheckList.size()]);
      String[] projectDocStateChecks = projectDpcStateConstraints.toArray(new String[projectDpcStateConstraints.size()]);

      String queryStr = select("tf").from("HTextFlow tf")
            .where(and(and(projectDocStateChecks), or(contentChecks)))
            .toQueryString();

      String searchString = constraints.getSearchString();
      searchString = "%" + searchString + "%";

      org.hibernate.Query query = session.createQuery(queryStr)
            .setParameter("searchString", searchString)
            .setParameter("project", projectSlug)
            .setParameter("iteration", iterationSlug);
      if (hasDocumentPaths)
      {
         query.setParameterList("doclist", documentPaths);
      }
      if (!includeAllStates)
      {
         query.setParameterList("statelist", stateList);
      }

      @SuppressWarnings("unchecked")
      List<HTextFlow> results = query.list();
      if (constraints.isCaseSensitive())
      {
         results = filterCaseSensitive(results, constraints, locId);
      }
      return results;
   }

   /**
    * Filter a list of text flows to include only those that have a case
    * sensitive match of the search string in the contents of interest.
    * 
    * @param results the list to filter
    * @param constraints describing search term and whether to match in source, target or both
    * @param localeId used to look up targets if target content is checked
    * @return filtered list
    */
   private List<HTextFlow> filterCaseSensitive(List<HTextFlow> results, FilterConstraints constraints, Long localeId)
   {
      List<HTextFlow> matchingTextFlows = new ArrayList<HTextFlow>();
      String search = constraints.getSearchString();

      scanning_text_flows: for (HTextFlow tf : results)
      {
         if (constraints.isSearchInSource())
         {
            for (String content : tf.getContents())
            {
               if (content.contains(search))
               {
                  matchingTextFlows.add(tf);
                  continue scanning_text_flows;
               }
            }
         }
         if (constraints.isSearchInTarget())
         {
            HTextFlowTarget tft = tf.getTargets().get(localeId);
            if (tft != null)
            {
               for (String content : tft.getContents())
               {
                  if (content.contains(search))
                  {
                     matchingTextFlows.add(tf);
                     continue scanning_text_flows;
                  }
               }
            }
         }
      }

      return matchingTextFlows;
   }

   /**
    * @param projectSlug
    * @param iterationSlug
    * @param localeId validated locale id
    * @param documentPaths
    * @param constraints
    * @return
    */
   private List<HTextFlow> findTextFlowsWithHibernateSearch(String projectSlug, String iterationSlug, LocaleId localeId, List<String> documentPaths, FilterConstraints constraints)
   {
      // Common query terms between source and targets
      TermQuery projectQuery = new TermQuery(new Term(IndexFieldLabels.PROJECT_FIELD, projectSlug));
      TermQuery iterationQuery = new TermQuery(new Term(IndexFieldLabels.ITERATION_FIELD, iterationSlug));
      TermQuery localeQuery = new TermQuery(new Term(IndexFieldLabels.LOCALE_ID_FIELD, localeId.getId()));

      MultiPhraseQuery documentsQuery = new MultiPhraseQuery();
      if (documentPaths != null && !documentPaths.isEmpty())
      {
         ArrayList<Term> docPathTerms = new ArrayList<Term>();
         for (String s : documentPaths)
         {
            docPathTerms.add(new Term(IndexFieldLabels.DOCUMENT_ID_FIELD, s));
         }
         documentsQuery.add(docPathTerms.toArray(new Term[docPathTerms.size()]));
      }

      List<HTextFlow> resultList = new ArrayList<HTextFlow>();
      if (constraints.isSearchInTarget())
      {
         // Content query for target
         String targetAnalyzerName = TextContainerAnalyzerDiscriminator.getAnalyzerDefinitionName( localeId.getId() );
         Analyzer targetAnalyzer = entityManager.getSearchFactory().getAnalyzer( targetAnalyzerName );

         Query tgtContentPhraseQuery;
         QueryParser contentQueryParser = new MultiFieldQueryParser(Version.LUCENE_29, IndexFieldLabels.CONTENT_FIELDS, targetAnalyzer);
         try
         {
            tgtContentPhraseQuery = contentQueryParser.parse("\"" + QueryParser.escape(constraints.getSearchString()) + "\"");
         }
         catch (ParseException e)
         {
            throw new ZanataServiceException("Failed to parse query", e);
         }

         // Target Query
         BooleanQuery targetQuery = new BooleanQuery();
         targetQuery.add(projectQuery, Occur.MUST);
         targetQuery.add(iterationQuery, Occur.MUST);
         targetQuery.add(tgtContentPhraseQuery, Occur.MUST);
         if( documentsQuery.getTermArrays().size() > 0 )
         {
            targetQuery.add(documentsQuery, Occur.MUST);
         }
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
            // manually check for case sensitive matches
            if( !constraints.isCaseSensitive() || (constraints.isCaseSensitive() && contentIsValid(htft.getContents(), constraints)) )
            {
               resultList.add(htft.getTextFlow());
            }
         }
      }

      if (constraints.isSearchInSource())
      {
         // Source locale
         // NB: Assume the first document's locale, or the same target locale if there are no documents
         // TODO Move source locale to the Project iteration level
         LocaleId sourceLocaleId = localeId;
         HProjectIteration projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
         if( !projectIteration.getDocuments().isEmpty() )
         {
            sourceLocaleId = projectIteration.getDocuments().values().iterator().next().getLocale().getLocaleId();
         }

         // Content query for source
         String sourceAnalyzerName = TextContainerAnalyzerDiscriminator.getAnalyzerDefinitionName( sourceLocaleId.getId() );
         Analyzer sourceAnalyzer = entityManager.getSearchFactory().getAnalyzer( sourceAnalyzerName );

         Query srcContentPhraseQuery;
         QueryParser srcContentQueryParser = new MultiFieldQueryParser(Version.LUCENE_29, IndexFieldLabels.CONTENT_FIELDS, sourceAnalyzer);
         try
         {
            srcContentPhraseQuery = srcContentQueryParser.parse("\"" + QueryParser.escape(constraints.getSearchString()) + "\"");
         }
         catch (ParseException e)
         {
            throw new ZanataServiceException("Failed to parse query", e);
         }

         // Source Query
         BooleanQuery sourceQuery = new BooleanQuery();
         sourceQuery.add(projectQuery, Occur.MUST);
         sourceQuery.add(iterationQuery, Occur.MUST);
         sourceQuery.add(srcContentPhraseQuery, Occur.MUST);
         if( documentsQuery.getTermArrays().size() > 0 )
         {
            sourceQuery.add(documentsQuery, Occur.MUST);
         }

         FullTextQuery ftQuery = entityManager.createFullTextQuery(sourceQuery, HTextFlow.class);
         @SuppressWarnings("unchecked")
         List<HTextFlow> matchedSources = (List<HTextFlow>) ftQuery.getResultList();
         log.info("got {} HTextFLow results", matchedSources.size());
         for (HTextFlow htf : matchedSources)
         {
            if (!resultList.contains(htf))
            {
               // manually check for case sensitive matches
               if( !constraints.isCaseSensitive() || (constraints.isCaseSensitive() && contentIsValid(htf.getContents(), constraints)) )
               {
                  resultList.add(htf);
               }
            }
         }
      }

      return resultList;
   }

   @Override
   public List<HTextFlow> findTextFlows(WorkspaceId workspace, DocumentId doc, FilterConstraints constraints)
   {
      List<String> documentPaths = new ArrayList<String>(1);
      HDocument document = documentDAO.getById( doc.getId() );
      documentPaths.add( document.getDocId() );

      return this.findTextFlows(workspace, documentPaths, constraints);
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

   private static boolean contentIsValid(Collection<String> contents, FilterConstraints constraints)
   {
      boolean valid = false;
      if( constraints.isSearchInSource() )
      {
         for( String content : contents )
         {
            // make sure contents are EXACTLY the same (they should already be the same case insensitively)
            if( constraints.isCaseSensitive() && content.contains( constraints.getSearchString() ) )
            {
               valid = true;
               break;
            }
         }
      }
      if( constraints.isSearchInTarget() )
      {
         for( String content : contents )
         {
            // make sure contents are EXACTLY the same (they should already be the same case insensitively)
            if( constraints.isCaseSensitive() && content.contains( constraints.getSearchString() ) )
            {
               valid = true;
               break;
            }
         }
      }

      return valid;
   }

}
