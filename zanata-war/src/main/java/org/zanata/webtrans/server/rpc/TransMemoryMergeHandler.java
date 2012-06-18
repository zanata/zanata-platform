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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.testng.collections.Lists;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import static com.google.common.collect.Collections2.*;
import static org.zanata.service.SecurityService.TranslationAction.*;

@Name("webtrans.gwt.TransMemoryMergeHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(TransMemoryMerge.class)
@Slf4j
public class TransMemoryMergeHandler extends AbstractActionHandler<TransMemoryMerge, UpdateTransUnitResult>
{

   @In(value = "webtrans.gwt.GetTransMemoryHandler", create = true)
   private GetTransMemoryHandler getTransMemoryHandler;

   @In(value = "webtrans.gwt.GetTransMemoryDetailsHandler", create = true)
   private GetTransMemoryDetailsHandler getTransMemoryDetailsHandler;

   @In(value = "webtrans.gwt.UpdateTransUnitHandler", create = true)
   private UpdateTransUnitHandler updateTransUnitHandler;
   
   @In
   private SecurityService securityServiceImpl;

   @In
   private TranslationWorkspaceManager translationWorkspaceManager;
   
   @In
   private TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   private ZanataIdentity identity;

   @Override
   public UpdateTransUnitResult execute(TransMemoryMerge action, ExecutionContext context) throws ActionException
   {
      //TODO all this routine check and get local, workspace etc is duplicated. Refactor it out to securityService?
      identity.checkLoggedIn();

      WorkspaceId workspaceId = action.getWorkspaceId();
      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(workspaceId);
      LocaleId localeId = workspaceId.getLocaleId();
      ProjectIterationId projectIterationId = workspaceId.getProjectIterationId();
      securityServiceImpl.checkPermissionForTranslation(workspace, projectIterationId.getProjectSlug(), localeId, MODIFY);

      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);

      Map<Long, TransUnitUpdateRequest> requestMap = transformToMap(action.getUpdateRequests());
      List<HTextFlow> hTextFlows = textFlowDAO.findByIdList(Lists.newArrayList(requestMap.keySet()));

      // FIXME this won't scale well (copy from GetTransMemoryHandler)
      List<Long> idsWithTranslations = textFlowDAO.findIdsWithTranslations(hLocale.getLocaleId());

      TransMemoryAboveThresholdPredicate predicate = new TransMemoryAboveThresholdPredicate(action.getThresholdPercent());

      List<TransUnitUpdateRequest> updateRequests = Lists.newArrayList();
      for (HTextFlow hTextFlow : hTextFlows)
      {
         ArrayList<TransMemoryResultItem> tmResults = getTransMemoryHandler.searchTransMemory(hLocale, new TransMemoryQuery(hTextFlow.getContents(), SearchType.FUZZY_PLURAL), idsWithTranslations);
         TransMemoryResultItem tmResult = findTMAboveThreshold(tmResults, predicate);
         if (tmResult != null)
         {
            TransMemoryDetails tmDetail = getTransMemoryDetailsHandler.getTransMemoryDetail(hLocale, hTextFlow);
            ContentState statusToSet = new TransMemoryMergeStatusResolver().workOutStatus(action, hTextFlow, tmDetail, tmResult);
            if (statusToSet != null)
            {
               TransUnitUpdateRequest unfilledRequest = requestMap.get(hTextFlow.getId());
               TransUnitUpdateRequest request = new TransUnitUpdateRequest(unfilledRequest.getTransUnitId(), tmResult.getTargetContents(), statusToSet, unfilledRequest.getBaseTranslationVersion());
               log.debug("auto translate from translation memory {}", request);
               updateRequests.add(request);
            }
         }
      }

      UpdateTransUnitResult result = updateTransUnitHandler.doTranslation(localeId, workspace, updateRequests, action.getEditorClientId(), TransUnitUpdated.UpdateType.ReplaceText);
      log.debug("TM merge result {}", result);
      return result;
   }

   private Map<Long, TransUnitUpdateRequest> transformToMap(List<TransUnitUpdateRequest> updateRequests)
   {
      ImmutableMap.Builder<Long, TransUnitUpdateRequest> mapBuilder = ImmutableMap.builder();
      for (TransUnitUpdateRequest updateRequest : updateRequests)
      {
         mapBuilder.put(updateRequest.getTransUnitId().getId(), updateRequest);
      }
      return mapBuilder.build();
   }

   private TransMemoryResultItem findTMAboveThreshold(ArrayList<TransMemoryResultItem> tmResults, TransMemoryAboveThresholdPredicate predicate)
   {
      Collection<TransMemoryResultItem> aboveThreshold = filter(tmResults, predicate);
      if (aboveThreshold.size() > 0)
      {
         return aboveThreshold.iterator().next();
      }
      else
      {
         return null;
      }
   }

   @Override
   public void rollback(TransMemoryMerge action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
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
