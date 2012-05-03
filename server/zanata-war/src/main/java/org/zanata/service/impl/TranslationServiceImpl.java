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

import java.text.MessageFormat;
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
import org.zanata.exception.ConcurrentTranslationException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
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
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

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
   public TranslationService.TranslationResult translate(LocaleId localeId, TransUnitUpdateRequest translateRequest) throws ConcurrentTranslationException
   {
      TranslationResultImpl result = new TranslationResultImpl();
      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, translateRequest.getTransUnitId().getValue());

      HProjectIteration projectIteration = hTextFlow.getDocument().getProjectIteration();
      String projectSlug = projectIteration.getProject().getSlug();
      HLocale hLocale = localeServiceImpl.validateLocaleByProjectIteration(localeId, projectSlug, projectIteration.getSlug());

      HTextFlowTarget hTextFlowTarget = getOrCreateTarget(hTextFlow, hLocale);

      if (translateRequest.getBaseTranslationVersion() != hTextFlowTarget.getVersionNum())
      {
         throw new ConcurrentTranslationException(MessageFormat.format("base translation version num {0} does not match current version num {1}, aborting", translateRequest.getBaseTranslationVersion(), hTextFlowTarget.getVersionNum()));
      }

      result.baseVersion = hTextFlowTarget.getVersionNum();
      result.baseContentState = hTextFlowTarget.getState();

      translate(hTextFlowTarget, translateRequest.getNewContentState(), translateRequest.getNewContents());

      result.translatedTextFlowTarget = hTextFlowTarget;
      result.isSuccess = true;
      return result;
   }

   @Override
   public List<TranslationResult> translate(LocaleId localeId, List<TransUnitUpdateRequest> translationRequests)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * Look up the {@link HTextFlowTarget} for the given hLocale in hTextFlow,
    * creating a new one if none is present.
    */
   private HTextFlowTarget getOrCreateTarget(HTextFlow hTextFlow, HLocale hLocale)
   {
      HTextFlowTarget hTextFlowTarget = hTextFlow.getTargets().get(hLocale);

      if (hTextFlowTarget == null)
      {
         hTextFlowTarget = new HTextFlowTarget(hTextFlow, hLocale);
         hTextFlowTarget.setVersionNum(0); // this will be incremented when content is set (below)
         hTextFlow.getTargets().put(hLocale, hTextFlowTarget);
      }
      return hTextFlowTarget;
   }

   private HTextFlowTarget translate(HTextFlowTarget hTextFlowTarget, ContentState requestedState, List<String> contentsToSave)
   {
      boolean targetChanged = false;
      targetChanged |= setContentStateIfChanged(requestedState, contentsToSave, hTextFlowTarget);
      targetChanged |= setContentIfChanged(hTextFlowTarget, contentsToSave);

      if (targetChanged || hTextFlowTarget.getVersionNum() == 0)
      {
         hTextFlowTarget.setVersionNum(hTextFlowTarget.getVersionNum() + 1);
         hTextFlowTarget.setTextFlowRevision(hTextFlowTarget.getTextFlow().getRevision());
         hTextFlowTarget.setLastModifiedBy(authenticatedAccount.getPerson());
         log.debug("last modified by :" + authenticatedAccount.getPerson().getName());
      }

      //save the target
      session.flush();

      return hTextFlowTarget;
   }

   /**
    * @return true if the content was changed, false otherwise
    */
   private boolean setContentIfChanged(HTextFlowTarget hTextFlowTarget, List<String> contentsToSave)
   {
      if (!contentsToSave.equals(hTextFlowTarget.getContents()))
      {
         hTextFlowTarget.setContents(contentsToSave);
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * Check that requestedState is valid for the given content, adjust if
    * necessary and set the new state if it has changed.
    * 
    * @return true if the content state was updated, false otherwise
    */
   private boolean setContentStateIfChanged(ContentState requestedState, List<String> newContent, HTextFlowTarget target)
   {
      ContentState previousState = target.getState();
      Collection<String> emptyContents = getEmptyContents(newContent);

      if (requestedState == ContentState.New && emptyContents.isEmpty())
      {
         log.warn("invalid ContentState New for Target with content '{}', assuming NeedReview", newContent);
         target.setState(ContentState.NeedReview);
      }
      else if (requestedState == ContentState.Approved && emptyContents.size() > 0)
      {
         log.warn("invalid ContentState Approved for empty TransUnit, assuming New");
         target.setState(ContentState.New);
      }
      else
      {
         target.setState(requestedState);
      }
      return target.getState() != previousState;
   }

   @Override
   public Collection<TextFlowTarget> translateAll(String projectSlug, String iterationSlug, String docId, LocaleId locale,
                                                  TranslationsResource translations, Set<String> extensions, MergeType mergeType)
   {
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(projectSlug, iterationSlug, true);

      validateExtensions(extensions);

      log.debug("pass evaluate");
      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, docId);
      if (document.isObsolete())
      {
         throw new ZanataServiceException("A document was not found.", 404);
      }


      log.debug("start put translations entity:{0}" , translations);

      boolean changed = false;

      HLocale hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, projectSlug, iterationSlug);
      // handle extensions
      changed |= resourceUtils.transferFromTranslationsResourceExtensions(translations.getExtensions(true), document, extensions, hLocale, mergeType);

      List<HPerson> newPeople = new ArrayList<HPerson>();
      // NB: removedTargets only applies for MergeType.IMPORT
      Collection<HTextFlowTarget> removedTargets = new HashSet<HTextFlowTarget>();
      Collection<TextFlowTarget> unknownResIds = new LinkedHashSet<TextFlowTarget>();

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
            // return warning for unknown resId to caller
            unknownResIds.add(incomingTarget);
            log.warn("skipping TextFlowTarget with unknown resId: {0}", resId);
//            continue;
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
         throw new ZanataServiceException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.", 404);
      }
      else if (hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE) || hProject.getStatus().equals(EntityStatus.OBSOLETE))
      {
         throw new ZanataServiceException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.", 404);
      }
      else if (writeOperation)
      {
         if (hProjectIteration.getStatus().equals(EntityStatus.READONLY) || hProject.getStatus().equals(EntityStatus.READONLY))
         {
            throw new ZanataServiceException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' is read-only.", 404);
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
               throw new ZanataServiceException(message, 400);
            }
            break;
         case New:
            if (allNonEmpty(contents))
            {
               String message = "ContentState New is illegal for non-empty TextFlowTarget " + resId;
               throw new ZanataServiceException(message, 400);
            }
            break;
         case Approved:
            // FIXME what if plurals < nplurals ?
            if (!allNonEmpty(contents))
            {
               String message = "ContentState Approved is illegal for TextFlowTarget " + resId + " with one or more empty strings";
               throw new ZanataServiceException(message, 400);
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
         @SuppressWarnings("unchecked")
         Collection<String> invalidExtensions = CollectionUtils.subtract(requestedExt, validExtensions);
         throw new ZanataServiceException("Unsupported Extensions within this context: " + StringUtils.join(invalidExtensions, ","), 400);
      }
   }

   public static class TranslationResultImpl implements TranslationResult
   {
      private HTextFlowTarget translatedTextFlowTarget;
      private boolean isSuccess;
      private int baseVersion;
      private ContentState baseContentState;

      @Override
      public boolean isTranslationSuccessful()
      {
         return isSuccess;
      }
      @Override
      public HTextFlowTarget getTranslatedTextFlowTarget()
      {
         return translatedTextFlowTarget;
      }
      @Override
      public int getBaseVersionNum()
      {
         return baseVersion;
      }
      @Override
      public ContentState getBaseContentState()
      {
         return baseContentState;
      }


   }

}
