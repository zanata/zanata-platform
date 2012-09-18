/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.ScrollableResults;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Work;
import org.zanata.common.ContentState;
import org.zanata.common.CopyTransOptions;
import org.zanata.dao.DatabaseConstants;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.process.CopyTransProcessHandle;
import org.zanata.rest.service.TranslatedDocResourceService;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;

//TODO unit test suite for this class

@Name("copyTransServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class CopyTransServiceImpl implements CopyTransService
{
   @In
   private EntityManager entityManager;

   @In
   private LocaleService localeServiceImpl;
   
   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In
   private DocumentDAO documentDAO;
   
   @Logger
   Log log;


   /**
    * Internal helper class to keep track of copy trans matches.
    */
   private class CopyTransMatch
   {
      private CopyTransMatch(HTextFlowTarget matchingTarget, ContentState targetState)
      {
         this.matchingTarget = matchingTarget;
         this.targetState = targetState;
      }

      HTextFlowTarget matchingTarget;
      ContentState targetState;
   }

   @Observer(TranslatedDocResourceService.EVENT_COPY_TRANS)
   public void runCopyTrans(Long docId, String project, String iterationSlug)
   {
      HDocument document = documentDAO.findById(docId, true);
      log.info("copyTrans start: document \"{0}\"", document.getDocId());
      List<HLocale> localelist = localeServiceImpl.getSupportedLangugeByProjectIteration(project, iterationSlug);

      // TODO iterate over document's textflows, then call copyTransForTextFlow(textFlow, localeList)
      // refer patch from https://bugzilla.redhat.com/show_bug.cgi?id=746899
      for (HLocale locale : localelist)
      {
         copyTransForLocale(document, locale);
      }
      log.info("copyTrans finished: document \"{0}\"", document.getDocId());
   }

   private String createComment(HTextFlowTarget target)
   {
      String author;
      HDocument document = target.getTextFlow().getDocument();
      String projectname = document.getProjectIteration().getProject().getName();
      String version = document.getProjectIteration().getSlug();
      String documentid = document.getDocId();
      if (target.getLastModifiedBy() != null)
      {
         author = ", author " + target.getLastModifiedBy().getName();
      }
      else
      {
         author = "";
      }

      return "translation auto-copied from project " + projectname + ", version " + version + ", document " + documentid + author;
   }

   @Override
   public void copyTransForLocale(HDocument document, HLocale locale)
   {
      this.copyTransForLocale(document, locale, new CopyTransOptions());
   }


   /**
    * Copies matching translations into a document for a given locale.
    * Note: This method is executed within a single transaction.
    *
    * @param document Document to copy translations into.
    * @param locale The locale for which to find matching translations to copy.
    * @param options Options to configure the copy trans process.
    */
   // TODO unit testing for this method. Reduce complexity(pass cyclomatic complexity analysis)
   public void copyTransForLocale(final HDocument document, final HLocale locale, final CopyTransOptions options)
   {
      try
      {
         new Work<Void>() {
            @Override
            protected Void work() throws Exception
            {
               ScrollableResults results = null;

               try
               {
                  int copyCount = 0;

                  results = textFlowTargetDAO.findLatestEquivalentTranslations(document, locale);
                  HTextFlow originalTf = null;

                  while (results.next())
                  {
                     originalTf = (HTextFlow)results.get(1);
                     HTextFlowTarget hTarget = originalTf.getTargets().get( locale.getId() );

                     // Stop looking at this text flow altogether if there is an already approved translation
                     if (hTarget != null && hTarget.getState() == ContentState.Approved)
                     {
                        continue;
                     }

                     // Get the best match for the original text flow
                     CopyTransMatch bestMatch = CopyTransServiceImpl.this.findBestMatch(results, originalTf, options);

                     // Create the new translation (or overwrite non-approved ones)

                     if (hTarget == null)
                     {
                        hTarget = new HTextFlowTarget(originalTf, locale);
                        hTarget.setVersionNum(1);
                        originalTf.getTargets().put(locale.getId(), hTarget);
                     }
                     else
                     {
                        // increase the versionNum
                        hTarget.setVersionNum(hTarget.getVersionNum() + 1);
                     }

                     // if a best match is found
                     if( bestMatch != null )
                     {
                        // NB we don't touch creationDate
                        hTarget.setTextFlowRevision(originalTf.getRevision());
                        hTarget.setLastChanged(bestMatch.matchingTarget.getLastChanged());
                        hTarget.setLastModifiedBy(bestMatch.matchingTarget.getLastModifiedBy());
                        hTarget.setContents(bestMatch.matchingTarget.getContents());
                        hTarget.setState(bestMatch.targetState);
                        HSimpleComment hcomment = hTarget.getComment();
                        if (hcomment == null)
                        {
                           hcomment = new HSimpleComment();
                           hTarget.setComment(hcomment);
                        }
                        hcomment.setComment(createComment(bestMatch.matchingTarget));
                        ++copyCount;

                        // manually flush
                        if( copyCount % DatabaseConstants.BATCH_SIZE == 0 )
                        {
                           entityManager.flush();
                           entityManager.clear();
                        }
                     }
                  }

                  log.info("copyTrans: {0} {1} translations for document \"{2}{3}\" ", copyCount, locale.getLocaleId(), document.getPath(), document.getName());
               }
               finally
               {
                  if( results != null )
                  {
                     results.close();
                  }
               }

               return null;
            }
         }.workInTransaction();
      }
      catch (Exception e)
      {
         log.warn("exception during copy trans", e);
      }
   }

   /**
    * Finds the best match in a result set for the first TextFlow found.
    * This is just a convenience method to split functionality of the copyTransForLocale
    * method.
    *
    * @param results The results to analyze. The cursor will be left in the position of
    *                the last analyzed row of the results.
    *                The cursor should be placed at the first element that will be analyzed.
    * @param originalTf Text Flow to look for copy trans matches.
    * @param options
    * @return The best match for copy trans against the provided original text flow.
    */
   private CopyTransMatch findBestMatch( ScrollableResults results, HTextFlow originalTf, CopyTransOptions options )
   {
      CopyTransMatch bestMatch = null;

      // NB: using a do-while loop to process the current result first
      do
      {
         HTextFlowTarget currentTft = (HTextFlowTarget)results.get(0);
         HTextFlow currentTf = (HTextFlow)results.get(1);
         HProject currentProj = (HProject)results.get(2); // NB: Could also use: currentTft.getTextFlow().getDocument().getProjectIteration().getProject();

         // If there is a change in the text flow, scroll back and return the best match found so far
         if ( !currentTf.getId().equals(originalTf.getId()) )
         {
            results.previous();
            return bestMatch;
         }

         // Find out if it is a better match than the current one, only if the there is no best match or if
         // we have found a match that could be improved
         if( bestMatch == null || bestMatch.targetState != ContentState.Approved )
         {
            ContentState currentCandidateTargetState = ContentState.Approved;

            // Context
            boolean contextMatch = currentTft.getTextFlow().getResId().equals( originalTf.getResId() );
            if( options.getContextMismatchAction() == CopyTransOptions.ConditionRuleAction.REJECT )
            {
               if( !contextMatch )
               {
                  continue;// rejected, continue
               }
            }
            else if( options.getContextMismatchAction() == CopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY )
            {
               if( !contextMatch )
               {
                  currentCandidateTargetState = ContentState.NeedReview;
               }
            }
            // If the action is IGNORE, don't even check

            // Doc Id
            boolean docIdMatch = currentTft.getTextFlow().getDocument().getDocId().equals( originalTf.getDocument().getDocId() );
            if( options.getDocIdMismatchAction() == CopyTransOptions.ConditionRuleAction.REJECT )
            {
               if( !docIdMatch )
               {
                  continue;// rejected, continue
               }
            }
            else if( options.getDocIdMismatchAction() == CopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY )
            {
               if( !docIdMatch )
               {
                  currentCandidateTargetState = ContentState.NeedReview;
               }
            }
            // If the action is IGNORE, don't even check

            // Project
            boolean projectMatch = currentProj.getId().equals(originalTf.getDocument().getProjectIteration().getProject().getId());
            if( options.getProjectMismatchAction() == CopyTransOptions.ConditionRuleAction.REJECT )
            {
               if( !projectMatch )
               {
                  continue; // rejected, continue
               }
            }
            else if( options.getProjectMismatchAction() == CopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY )
            {
               if( !projectMatch )
               {
                  currentCandidateTargetState = ContentState.NeedReview;
               }
            }
            // If the action is IGNORE, don't even check

            // See if there is a better match at this point
            if( bestMatch == null ||
                (bestMatch.targetState != ContentState.Approved && currentCandidateTargetState == ContentState.Approved) )
            {
               bestMatch = new CopyTransMatch(currentTft, currentCandidateTargetState);
            }

         }
      }
      while( results.next() );

      return bestMatch;
   }
   
   @Override
   public void copyTransForDocument(HDocument document)
   {
      this.copyTransForDocument(document, new CopyTransOptions(), null);
   }

   /**
    * @see CopyTransServiceImpl#copyTransForIteration(org.zanata.model.HProjectIteration, org.zanata.process.CopyTransProcessHandle)
    */
   @Override
   public void copyTransForIteration(HProjectIteration iteration)
   {
      this.copyTransForIteration(iteration, null);
   }

   @Override
   public void copyTransForIteration(HProjectIteration iteration, CopyTransProcessHandle procHandle)
   {
      if( procHandle != null )
      {
         List<HLocale> localeList =
               localeServiceImpl.getSupportedLangugeByProjectIteration(iteration.getProject().getSlug(),
                     iteration.getSlug());

         procHandle.setMaxProgress( iteration.getDocuments().size() * localeList.size() );
         procHandle.setCurrentProgress(0);
      }

      for( HDocument doc : iteration.getDocuments().values() )
      {
         if( procHandle.shouldStop() )
         {
            return;
         }
         this.copyTransForDocument(doc, procHandle.getOptions(), procHandle);
      }
   }

   /**
    * @see CopyTransServiceImpl#copyTransForDocument(org.zanata.model.HDocument)
    */
   private void copyTransForDocument(HDocument document, CopyTransOptions options, CopyTransProcessHandle processHandle)
   {
      log.info("copyTrans start: document \"{0}\"", document.getDocId());
      List<HLocale> localeList =
            localeServiceImpl.getSupportedLangugeByProjectIteration(document.getProjectIteration().getProject().getSlug(),
                  document.getProjectIteration().getSlug());

      for (HLocale locale : localeList)
      {
         if( processHandle != null && processHandle.shouldStop() )
         {
            return;
         }
         copyTransForLocale(document, locale, options, processHandle);
      }

      if( processHandle != null )
      {
         processHandle.setDocumentsProcessed( processHandle.getDocumentsProcessed() + 1 );
      }
      log.info("copyTrans finished: document \"{0}\"", document.getDocId());
   }

   /**
    * @see CopyTransServiceImpl#copyTransForLocale(org.zanata.model.HDocument, org.zanata.model.HLocale, org.zanata.common.CopyTransOptions, org.zanata.process.CopyTransProcessHandle)
    */
   private void copyTransForLocale(HDocument document, HLocale locale, CopyTransOptions options, CopyTransProcessHandle procHandle)
   {
      this.copyTransForLocale(document, locale, options);

      if( procHandle != null )
      {
         procHandle.incrementProgress(1);
      }
   }
}
