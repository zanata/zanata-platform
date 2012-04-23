/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.common.MergeType;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;

import static org.zanata.util.StringUtil.allEmpty;
import static org.zanata.util.StringUtil.allNonEmpty;

@Name("translationServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TranslationServiceImpl implements TranslationService
{

   @Logger
   private Log log;

   @In
   private Session session;

   @In
   private ProjectDAO projectDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private PersonDAO personDAO;

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

   @In
   private ResourceUtils resourceUtils;

   @In
   private TranslationWorkspaceManager translationWorkspaceManager;

   @In
   private LocaleService localeServiceImpl;

   @In(value = JpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION)
   private HAccount authenticatedAccount;

   public TranslationServiceImpl()
   {
   }

   // for tests
   public TranslationServiceImpl(Session session,
                                 ProjectDAO projectDAO,
                                 TranslationWorkspaceManager translationWorkspaceManager,
                                 LocaleService localeService,
                                 HAccount authenticatedAccount,
                                 ProjectIterationDAO projectIterationDAO,
                                 DocumentDAO documentDAO,
                                 PersonDAO personDAO,
                                 TextFlowDAO textFlowDAO,
                                 TextFlowTargetDAO textFlowTargetDAO,
                                 TextFlowTargetHistoryDAO textFlowTargetHistoryDAO,
                                 ResourceUtils resourceUtils)
   {
      this.session = session;
      this.projectDAO = projectDAO;
      this.translationWorkspaceManager = translationWorkspaceManager;
      this.localeServiceImpl = localeService;
      this.authenticatedAccount = authenticatedAccount;
      this.projectIterationDAO = projectIterationDAO;
      this.documentDAO = documentDAO;
      this.personDAO = personDAO;
      this.textFlowDAO = textFlowDAO;
      this.textFlowTargetDAO = textFlowTargetDAO;
      this.textFlowTargetHistoryDAO = textFlowTargetHistoryDAO;
      this.resourceUtils = resourceUtils;
      this.log = Logging.getLog(TranslationResultImpl.class);
   }

   @Override
   public TranslationResult translate(Long textFlowId, LocaleId localeId, ContentState stateToSet, List<String> contentsToSave)
   {
      TranslationResultImpl result = new TranslationResultImpl();

      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, textFlowId);
      result.textFlow = hTextFlow;

      HProjectIteration projectIteration = hTextFlow.getDocument().getProjectIteration();
      HIterationProject project = projectIteration.getProject();
      HLocale hLocale = localeServiceImpl.validateLocaleByProjectIteration(localeId, project.getSlug(), projectIteration.getSlug());

      HTextFlowTarget hTextFlowTarget = hTextFlow.getTargets().get(hLocale);

      boolean targetChanged = false;

      if (hTextFlowTarget == null)
      {
         hTextFlowTarget = new HTextFlowTarget(hTextFlow, hLocale);
         hTextFlowTarget.setVersionNum(0); // this will be incremented when content is set (below)
         hTextFlow.getTargets().put(hLocale, hTextFlowTarget);
         targetChanged = true;
      }
      result.prevTextFlowTarget = hTextFlowTarget;

      //work on content state
      ContentState previousState = hTextFlowTarget.getState();
      determineContentState(textFlowId, stateToSet, contentsToSave, hTextFlowTarget);
      if (previousState != hTextFlowTarget.getState())
      {
         targetChanged = true;
      }

      if (!contentsToSave.equals(hTextFlowTarget.getContents()))
      {
         hTextFlowTarget.setContents(contentsToSave);
         targetChanged = true;
      }

      if (targetChanged)
      {
         hTextFlowTarget.setVersionNum(hTextFlowTarget.getVersionNum() + 1);
         hTextFlowTarget.setTextFlowRevision(hTextFlow.getRevision());
         log.debug("last modified by :" + authenticatedAccount.getPerson().getName());
         hTextFlowTarget.setLastModifiedBy(authenticatedAccount.getPerson());
      }

      //save the target
      session.flush();

      //return text flow and new text flow target
      result.newTextFlowTarget = hTextFlowTarget;
      return result;
   }


   public Collection<String> translateAll(String projectSlug, String iterationSlug, String docId, LocaleId locale, TranslationsResource translations, Set<String> extensions,
                               MergeType mergeType)
   {
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(projectSlug, iterationSlug, true);

      validateExtensions(extensions);

      log.debug("pass evaluate");
      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
      if (document.isObsolete())
      {
         throw new ZanataServiceException.EntityNotFoundException("A document was not found.");
      }


      log.debug("start put translations entity:{0}" , translations);

      boolean changed = false;

      HLocale hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, projectSlug, iterationSlug);
      // handle extensions
      changed |= resourceUtils.transferFromTranslationsResourceExtensions(translations.getExtensions(true), document, extensions, hLocale, mergeType);

      List<HPerson> newPeople = new ArrayList<HPerson>();
      // NB: removedTargets only applies for MergeType.IMPORT
      Collection<HTextFlowTarget> removedTargets = new HashSet<HTextFlowTarget>();
      Collection<String> unknownResIds = new LinkedHashSet<String>();

      if (mergeType == MergeType.IMPORT)
      {
         for (HTextFlow textFlow : document.getTextFlows())
         {
            HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
            if (hTarget != null)
            {
               removedTargets.add(hTarget);
            }
         }
      }

      for (TextFlowTarget incomingTarget : translations.getTextFlowTargets())
      {
         String resId = incomingTarget.getResId();
         HTextFlow textFlow = textFlowDAO.getById(document, resId);
         if (textFlow == null)
         {
            // return warning for unknown resId to REST client
            unknownResIds.add(resId);
            log.warn("skipping TextFlowTarget with unknown resId: {0}", resId);
            continue;
         }
         else
         {
            checkTargetState(incomingTarget.getResId(), incomingTarget.getState(), incomingTarget.getContents());
            HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
            boolean targetChanged = false;
            if (hTarget == null)
            {
               targetChanged = true;
               log.debug("locale: {0}", locale);
               hTarget = new HTextFlowTarget(textFlow, hLocale);
               hTarget.setVersionNum(0); // incremented when content is set
               textFlow.getTargets().put(hLocale, hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTarget(incomingTarget, hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
            }
            else
            {
               switch (mergeType)
               {
                  case AUTO:
                     if (incomingTarget.getState() != ContentState.New)
                     {
                        if (hTarget.getState() == ContentState.New)
                        {
                           targetChanged |= resourceUtils.transferFromTextFlowTarget(incomingTarget, hTarget);
                           targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
                        }
                        else if (incomingTarget.getState() == ContentState.Approved)
                        {
                           List<String> incomingContents = incomingTarget.getContents();
                           boolean oldContent = textFlowTargetHistoryDAO.findContentInHistory(hTarget, incomingContents);
                           if (!oldContent)
                           {
                              targetChanged |= resourceUtils.transferFromTextFlowTarget(incomingTarget, hTarget);
                              targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
                           }
                        }
                        else
                        {
                           // incomingTarget state = NeedReview
                           // hTarget state != New

                           // we don't overwrite the server's NeedReview or Approved value (business rule)
                        }
                     }
                     break;

                  case IMPORT:
                     removedTargets.remove(hTarget);
                     targetChanged |= resourceUtils.transferFromTextFlowTarget(incomingTarget, hTarget);
                     targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
                     break;

                  default:
                     throw new ZanataServiceException("unhandled merge type " + mergeType);
               }
            }

            // update translation information if applicable
            if (targetChanged)
            {
               changed = true;
               if (incomingTarget.getTranslator() != null)
               {
                  String email = incomingTarget.getTranslator().getEmail();
                  HPerson hPerson = personDAO.findByEmail(email);
                  if (hPerson == null)
                  {
                     hPerson = new HPerson();
                     hPerson.setEmail(email);
                     hPerson.setName(incomingTarget.getTranslator().getName());
                     newPeople.add(hPerson);
                  }
                  hTarget.setLastModifiedBy(hPerson);
               }
               else
               {
                  hTarget.setLastModifiedBy(null);
               }
               textFlowTargetDAO.makePersistent(hTarget);
            }
            incomingTarget = null;
         }
      }
      if (changed || !removedTargets.isEmpty())
      {
         for (HPerson person : newPeople)
         {
            personDAO.makePersistent(person);
         }
         personDAO.flush();

         for (HTextFlowTarget target : removedTargets)
         {
            target.clear();
         }
         textFlowTargetDAO.flush();

         documentDAO.flush();
      }

      return unknownResIds;
   }

   private boolean determineContentState(Long textFlowId, ContentState stateToSet, List<String> contentToSave, HTextFlowTarget target)
   {
      boolean targetChanged = false;
      Collection<String> emptyContents = getEmptyContents(contentToSave);

      if (stateToSet == ContentState.New && emptyContents.isEmpty())
      {
         log.warn("invalid ContentState New for TransUnit {} with content '{}', assuming NeedReview", textFlowId, contentToSave);
         target.setState(ContentState.NeedReview);
      }
      else if (stateToSet == ContentState.Approved && emptyContents.size() > 0)
      {
         log.warn("invalid ContentState {} for empty TransUnit {}, assuming New", stateToSet, textFlowId);
         target.setState(ContentState.New);
      }
      else
      {
         if (target.getState() != stateToSet)
         {
            targetChanged = true;
         }
         target.setState(stateToSet);
      }
      return targetChanged;
   }

   private static Collection<String> getEmptyContents(List<String> targetContents)
   {
      return Collections2.filter(targetContents, new Predicate<String>()
      {
         @Override
         public boolean apply(@Nullable String input)
         {
            return Strings.isNullOrEmpty(input);
         }
      });
   }

   private HProjectIteration retrieveAndCheckIteration(String projectSlug, String iterationSlug, boolean writeOperation)
   {
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HProject hProject = projectDAO.getBySlug(projectSlug);

      if (hProjectIteration == null)
      {
         throw new ZanataServiceException.EntityNotFoundException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
      }
      else if (hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE) || hProject.getStatus().equals(EntityStatus.OBSOLETE))
      {
         throw new ZanataServiceException.EntityNotFoundException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
      }
      else if (writeOperation)
      {
         if (hProjectIteration.getStatus().equals(EntityStatus.READONLY) || hProject.getStatus().equals(EntityStatus.READONLY))
         {
            throw new ZanataServiceException.EntityNotFoundException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' is read-only.");
         }
         else
         {
            return hProjectIteration;
         }
      }
      else
      {
         return hProjectIteration;
      }
   }

   private void checkTargetState(String resId, ContentState state, List<String> contents)
   {
      switch (state)
      {
         case NeedReview:
            if (allEmpty(contents))
            {
               String message = "ContentState NeedsReview is illegal for TextFlowTarget " + resId + " with no contents";
               throw new ZanataServiceException.InvalidParameterException(message);
            }
            break;
         case New:
            if (allNonEmpty(contents))
            {
               String message = "ContentState New is illegal for non-empty TextFlowTarget " + resId;
               throw new ZanataServiceException.InvalidParameterException(message);
            }
            break;
         case Approved:
            // FIXME what if plurals < nplurals ?
            if (!allNonEmpty(contents))
            {
               String message = "ContentState Approved is illegal for TextFlowTarget " + resId + " with one or more empty strings";
               throw new ZanataServiceException.InvalidParameterException(message);
            }
            break;
         default:
            throw new ZanataServiceException("unknown ContentState " + state);
      }
   }


   /**
    * Ensures that any extensions sent with the current query are valid for this
    * context.
    *
    * @param requestedExt Extensions to be validated
    * @throws ZanataServiceException if any unsupported extensions are present
    */
   public static void validateExtensions(Set<String> requestedExt)
   {
      Set<String> validExtensions = ExtensionType.asStringSet();

      if(!CollectionUtils.isSubCollection(requestedExt, validExtensions))
      {
         Collection<String> invalidExtensions = CollectionUtils.subtract(requestedExt, validExtensions);
         throw new ZanataServiceException.InvalidParameterException("Unsupported Extensions within this context: " + StringUtils.join(invalidExtensions, ","));
      }
   }

   public static class TranslationResultImpl implements TranslationResult
   {
      private HTextFlow textFlow;
      private HTextFlowTarget prevTextFlowTarget;
      private HTextFlowTarget newTextFlowTarget;

      @Override
      public HTextFlow getTextFlow()
      {
         return textFlow;
      }

      @Override
      public HTextFlowTarget getPreviousTextFlowTarget()
      {
         return prevTextFlowTarget;
      }

      @Override
      public HTextFlowTarget getNewTextFlowTarget()
      {
         return newTextFlowTarget;
      }
   }
}
