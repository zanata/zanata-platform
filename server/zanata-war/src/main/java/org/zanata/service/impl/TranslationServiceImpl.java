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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

@Name("translationServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class TranslationServiceImpl implements TranslationService
{
   private static final Logger LOGGER = LoggerFactory.getLogger(TranslationServiceImpl.class);

   @In
   Session session;

   @In
   TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

   @In
   ProjectDAO projectDAO;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   private LocaleService localeServiceImpl;

   @In(value = JpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION)
   HAccount authenticatedAccount;

   @Override
   public Map.Entry<HTextFlow, HTextFlowTarget> translate(Long textFlowId, LocaleId localeId, ContentState stateToSet, List<String> contentsToSave)
   {
      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, textFlowId);
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

      //TODO detection of target change can be done by using hibernate interceptor
      if (targetChanged)
      {
         hTextFlowTarget.setVersionNum(hTextFlowTarget.getVersionNum() + 1);
         hTextFlowTarget.setTextFlowRevision(hTextFlow.getRevision());
         LOGGER.debug("last modified by :" + authenticatedAccount.getPerson().getName());
         hTextFlowTarget.setLastModifiedBy(authenticatedAccount.getPerson());
      }

      //save the target
      session.flush();

      //return text flow and new text flow target
      return new AbstractMap.SimpleEntry<org.zanata.model.HTextFlow, org.zanata.model.HTextFlowTarget>(hTextFlow, hTextFlowTarget);
   }

   private boolean determineContentState(Long textFlowId, ContentState stateToSet, List<String> contentToSave, HTextFlowTarget target)
   {
      boolean targetChanged = false;
      Collection<String> emptyContents = getEmptyContents(contentToSave);

      if (stateToSet == ContentState.New && emptyContents.isEmpty())
      {
         LOGGER.warn("invalid ContentState New for TransUnit {} with content '{}', assuming NeedReview", textFlowId, contentToSave);
         target.setState(ContentState.NeedReview);
      }
      else if (stateToSet == ContentState.Approved && emptyContents.size() > 0)
      {
         LOGGER.warn("invalid ContentState {} for empty TransUnit {}, assuming New", stateToSet, textFlowId);
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
}
