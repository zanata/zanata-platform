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
package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.PrefillTranslation;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import static org.zanata.service.SecurityService.TranslationAction.*;

@Name("webtrans.gwt.PrefillTranslationHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(PrefillTranslation.class)
@Slf4j
public class PrefillTranslationHandler extends AbstractActionHandler<PrefillTranslation, NoOpResult>
{

   @In(value = "webtrans.gwt.GetTransMemoryHandler", create = true)
   private GetTransMemoryHandler getTransMemoryHandler;
   
   @In
   private SecurityService securityServiceImpl;

   @In
   private TranslationWorkspaceManager translationWorkspaceManager;
   
   @In
   private TextFlowDAO textFlowDAO;

   @In
   private TranslationService translationServiceImpl;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public NoOpResult execute(PrefillTranslation action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      WorkspaceId workspaceId = action.getWorkspaceId();
      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(workspaceId);
      LocaleId localeId = workspaceId.getLocaleId();
      ProjectIterationId projectIterationId = workspaceId.getProjectIterationId();
      securityServiceImpl.checkPermissionForTranslation(workspace, projectIterationId.getProjectSlug(), localeId, MODIFY);

      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);

      List<HTextFlow> hTextFlows = textFlowDAO.getAllUntranslatedTextFlowByDocId(action.getDocId().getId(), hLocale);

      log.info("about to prefill #{} textflow for action {}", hTextFlows.size(), action);

      TransMemoryAboveThresholdPredicate predicate = new TransMemoryAboveThresholdPredicate(action.getApprovedThreshold());
      for (HTextFlow hTextFlow : hTextFlows)
      {
         ArrayList<TransMemoryResultItem> tmResults = getTransMemoryHandler.searchTransMemory(hLocale, new TransMemoryQuery(hTextFlow.getContents(), SearchType.FUZZY_PLURAL));
         Collection<TransMemoryResultItem> aboveThreshold = Collections2.filter(tmResults, predicate);
         if (aboveThreshold.size() > 0)
         {
            TransMemoryResultItem mostSimilarTM = aboveThreshold.iterator().next();
            log.info("auto translation from translation memory for textFlow id {} with contents {}", hTextFlow.getId(), mostSimilarTM);

         }
      }
      //TODO best send out event and let all clients to refresh table if they are editing this document
      return new NoOpResult();
   }

   @Override
   public void rollback(PrefillTranslation action, NoOpResult result, ExecutionContext context) throws ActionException
   {
   }

   private static class TransMemoryAboveThresholdPredicate implements Predicate<TransMemoryResultItem>
   {
      private final int approvedThreshold;

      public TransMemoryAboveThresholdPredicate(int approvedThreshold)
      {
         this.approvedThreshold = approvedThreshold;
      }

      @Override
      public boolean apply(TransMemoryResultItem tmResult)
      {
         return tmResult.getSimilarityPercent() >= approvedThreshold;
      }
   }
}
