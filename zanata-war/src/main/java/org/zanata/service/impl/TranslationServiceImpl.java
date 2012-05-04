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

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
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
import org.zanata.common.MergeType;
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
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.TranslationWorkspaceManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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


   @Override
   public Collection<TextFlowTarget> translateAll(String projectSlug, String iterationSlug, String docId, LocaleId locale,
                                                  TranslationsResource translations, Set<String> extensions, MergeType mergeType)
   {
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      if( hProjectIteration == null )
      {
         throw new ZanataServiceException("Version '" + iterationSlug + "' for project '" + projectSlug + "' ");
      }

      resourceUtils.validateExtensions(extensions);

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

   private void determineContentState(Long textFlowId, ContentState stateToSet, List<String> contentToSave, HTextFlowTarget target)
   {
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
         target.setState(stateToSet);
      }
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
