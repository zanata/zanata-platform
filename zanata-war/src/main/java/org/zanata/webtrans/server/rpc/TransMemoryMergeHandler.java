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
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import static org.zanata.service.SecurityService.TranslationAction.*;

@Name("webtrans.gwt.TransMemoryMergeHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(TransMemoryMerge.class)
@Slf4j
public class TransMemoryMergeHandler extends AbstractActionHandler<TransMemoryMerge, NoOpResult>
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
   public NoOpResult execute(TransMemoryMerge action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      WorkspaceId workspaceId = action.getWorkspaceId();
      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(workspaceId);
      LocaleId localeId = workspaceId.getLocaleId();
      ProjectIterationId projectIterationId = workspaceId.getProjectIterationId();
      securityServiceImpl.checkPermissionForTranslation(workspace, projectIterationId.getProjectSlug(), localeId, MODIFY);

      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);

      List<HTextFlow> hTextFlows = textFlowDAO.getAllUntranslatedTextFlowByDocumentId(action.getDocumentId().getId(), hLocale);
      TransMemoryMergeHandler.log.info("#{} untranslated text flow in doc id {}", hTextFlows.size(), action.getDocumentId());

      TransMemoryAboveThresholdPredicate predicate = new TransMemoryAboveThresholdPredicate(action.getApprovedThreshold());
      for (HTextFlow hTextFlow : hTextFlows)
      {
         ArrayList<TransMemoryResultItem> tmResults = getTransMemoryHandler.searchTransMemory(hLocale, new TransMemoryQuery(hTextFlow.getContents(), SearchType.FUZZY_PLURAL));
         Collection<TransMemoryResultItem> aboveThreshold = Collections2.filter(tmResults, predicate);
         if (aboveThreshold.size() > 0)
         {
            TransMemoryResultItem mostSimilarTM = aboveThreshold.iterator().next();
            TransMemoryMergeHandler.log.info("auto translation from translation memory for textFlow id {} with contents {}", hTextFlow.getId(), mostSimilarTM.getTargetContents());

            //TODO we may want to wrap it in try/catch block and ignore any exceptions. i.e. we may have concurrent editing conflict and do we give it a damn?
            translationServiceImpl.translate(hTextFlow, hLocale, mostSimilarTM.getTargetContents(), ContentState.Approved);
         }
      }
      //TODO best send out event and let all clients to refresh table if they are editing this document
      return new NoOpResult();
   }

   @Override
   public void rollback(TransMemoryMerge action, NoOpResult result, ExecutionContext context) throws ActionException
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
