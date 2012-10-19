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

import static org.zanata.common.ContentState.Approved;
import static org.zanata.common.ContentState.NeedReview;
import static org.zanata.common.ContentState.New;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.HibernateException;
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
import org.zanata.dao.DatabaseConstants;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
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

   @In
   private ProjectDAO projectDAO;
   
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
      this.copyTransForLocale(document, locale, new HCopyTransOptions());
   }

   public void copyTransForLocale(final HDocument document, final HLocale locale, final HCopyTransOptions options)
   {
      try
      {
         new Work<Void>() {
            @Override
            protected Void work() throws Exception
            {
               int copyCount = 0;

               // Determine the state of the copies for each pass
               boolean checkContext = true,
                       checkProject = true,
                       checkDocument = true;

               // First pass, very conservative
               // Every result will match context, document, and project
               copyCount += copyTransPass(document, locale, checkContext, checkProject, checkDocument, options);

               // Next passes, more relaxed and only needed when the options call for it
               if( options.getDocIdMismatchAction() != REJECT )
               {
                  // Relax doc Id restriction
                  checkDocument = false;
                  // Every result will match context, and project
                  // Assuming Phase 1 ran, results will have non-matching doc Ids
                  copyCount += copyTransPass(document, locale, checkContext, checkProject, checkDocument, options);
               }
               if( options.getProjectMismatchAction() != REJECT )
               {
                  // Relax project restriction
                  checkProject = false;
                  // Every result will match context
                  // Assuming above phases, results will have non-matching project
                  // Assuming above phase: either doc Id didn't match, or the user explicitly rejected non-matching documents
                  copyCount += copyTransPass(document, locale, checkContext, checkProject, checkDocument, options);
               }
               if( options.getContextMismatchAction() != REJECT )
               {
                  // Relax context restriction
                  checkContext = false;
                  // Assuming above phases:
                  // Context does not match
                  // either doc Id didn't match, or the user explicitly rejected non-matching documents
                  // and either Project didn't match, or the user explicitly rejected non-matching projects
                  copyCount += copyTransPass(document, locale, checkContext, checkProject, checkDocument, options);
               }
               if( options.getContextMismatchAction() != REJECT )
               {
                  // Relax context restriction
                  checkContext = false;
                  // Assuming above phases:
                  // Context does not match
                  // either doc Id didn't match, or the user explicitly rejected non-matching documents
                  // and either Project didn't match, or the user explicitly rejected non-matching projects
                  copyCount += copyTransPass(document, locale, checkContext, checkProject, checkDocument, options);
               }

               log.info("copyTrans: {0} {1} translations for document \"{2}{3}\" ", copyCount, locale.getLocaleId(), document.getPath(), document.getName());

               return null;
            }
         }.workInTransaction();
      }
      catch (Exception e)
      {
         log.warn("exception during copy trans", e);
      }
   }

   private int copyTransPass( HDocument document, HLocale locale, boolean checkContext, boolean checkProject, boolean checkDocument,
                              HCopyTransOptions options)
   {
      ScrollableResults results = null;

      int copyCount = 0;
      try
      {
         results = textFlowTargetDAO.findMatchingTranslations(document, locale, checkContext, checkDocument, checkProject);
         copyCount = 0;

         while( results.next() )
         {
            HTextFlowTarget matchingTarget = (HTextFlowTarget)results.get(0);
            HTextFlow originalTf = (HTextFlow)results.get(1);
            HTextFlowTarget hTarget = textFlowTargetDAO.getOrCreateTarget(originalTf, locale);
            ContentState copyState = determineContentState(
                  originalTf.getResId().equals(matchingTarget.getTextFlow().getResId()),
                  originalTf.getDocument().getProjectIteration().getProject().getId().equals( matchingTarget.getTextFlow().getDocument().getProjectIteration().getProject().getId() ),
                  originalTf.getDocument().getDocId().equals( matchingTarget.getTextFlow().getDocument().getDocId() ),
                  options);

            if( shouldOverwrite(hTarget, copyState) )
            {

               // NB we don't touch creationDate
               hTarget.setTextFlowRevision(originalTf.getRevision());
               hTarget.setLastChanged(matchingTarget.getLastChanged());
               hTarget.setLastModifiedBy(matchingTarget.getLastModifiedBy());
               hTarget.setContents(matchingTarget.getContents());
               hTarget.setState(copyState);
               HSimpleComment hcomment = hTarget.getComment();
               if (hcomment == null)
               {
                  hcomment = new HSimpleComment();
                  hTarget.setComment(hcomment);
               }
               hcomment.setComment(createComment(matchingTarget));
               ++copyCount;

               // manually flush
               if( copyCount % DatabaseConstants.BATCH_SIZE == 0 )
               {
                  entityManager.flush();
                  entityManager.clear();
               }
            }
         }

         // a final flush
         if( copyCount % DatabaseConstants.BATCH_SIZE != 0 )
         {
            entityManager.flush();
            entityManager.clear();
         }
      }
      catch (HibernateException e)
      {
         log.error("Copy trans error", e);
      }
      finally
      {
         if( results != null )
         {
            results.close();
         }
      }
      return copyCount;
   }

   private ContentState determineContentState(boolean contextMatches, boolean projectMatches, boolean docIdMatches,
                                              HCopyTransOptions options)
   {
      ContentState state = Approved;
      state = getExpectedContentState(contextMatches, options.getContextMismatchAction(), state);
      state = getExpectedContentState(projectMatches, options.getProjectMismatchAction(), state);
      state = getExpectedContentState(docIdMatches, options.getDocIdMismatchAction(), state);
      return state;
   }

   public ContentState getExpectedContentState( boolean match, HCopyTransOptions.ConditionRuleAction action,
                                                 ContentState currentState )
   {
      if( currentState == null )
      {
         return null;
      }
      else if( !match )
      {
         if( action == DOWNGRADE_TO_FUZZY )
         {
            return NeedReview;
         }
         else if( action == REJECT )
         {
            return null;
         }
      }
      return currentState;
   }
   
   @Override
   public void copyTransForDocument(HDocument document)
   {
      HCopyTransOptions copyTransOpts =
            document.getProjectIteration().getProject().getDefaultCopyTransOpts();
      if( copyTransOpts == null )
      {
         copyTransOpts = new HCopyTransOptions();
      }

      this.copyTransForDocument(document, copyTransOpts, null);
   }

   @Override
   public void copyTransForDocument(HDocument document, CopyTransProcessHandle processHandle)
   {
      // Set the max progress only if it hasn't been set yet
      if( processHandle != null && !processHandle.isMaxProgressSet() )
      {
         List<HLocale> localeList =
               localeServiceImpl.getSupportedLangugeByProjectIteration(document.getProjectIteration().getProject().getSlug(),
                     document.getProjectIteration().getSlug());

         processHandle.setMaxProgress(localeList.size());
      }

      HCopyTransOptions copyTransOpts = processHandle.getOptions();
      // use project level options
      if( copyTransOpts == null )
      {
         // NB: Need to reload the options from the db
         copyTransOpts = projectDAO.findById( document.getProjectIteration().getProject().getId(), false )
                                   .getDefaultCopyTransOpts();
      }
      // use the global default options
      if( copyTransOpts == null )
      {
         copyTransOpts = new HCopyTransOptions();
      }

      this.copyTransForDocument(document, copyTransOpts, processHandle);
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

      // TODO RunnableProcess handle may not be null
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
    * NB: The handle's options will be ignored. This is a convenience method to have the logic
    * in a single place.
    * @see CopyTransServiceImpl#copyTransForDocument(org.zanata.model.HDocument)
    */
   private void copyTransForDocument(HDocument document, HCopyTransOptions options, CopyTransProcessHandle processHandle)
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
    * @see CopyTransServiceImpl#copyTransForLocale(org.zanata.model.HDocument, org.zanata.model.HLocale, HCopyTransOptions, org.zanata.process.CopyTransProcessHandle)
    */
   private void copyTransForLocale(HDocument document, HLocale locale, HCopyTransOptions options, CopyTransProcessHandle procHandle)
   {
      this.copyTransForLocale(document, locale, options);

      if( procHandle != null )
      {
         procHandle.incrementProgress(1);
      }
   }

   /**
    * Indicates if a Copy Trans found match should overwrite the currently stored one based on their states.
    */
   private static boolean shouldOverwrite(HTextFlowTarget currentlyStored, ContentState matchState)
   {
      if( currentlyStored != null )
      {
         if( currentlyStored.getState() == NeedReview && matchState == Approved )
         {
            return true; // If it's fuzzy, replace only with approved ones
         }
         else if( currentlyStored.getState() == New )
         {
            return true; // If it's new, replace always
         }
         else
         {
            return false;
         }
      }
      return true;
   }
}
