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
package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

@Name("webtrans.gwt.GetTransUnitsNavigationHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitsNavigation.class)
@Slf4j
public class GetTransUnitsNavigationHandler extends AbstractActionHandler<GetTransUnitsNavigation, GetTransUnitsNavigationResult>
{

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public GetTransUnitsNavigationResult execute(GetTransUnitsNavigation action, ExecutionContext context) throws ActionException
   {

      ZanataIdentity.instance().checkLoggedIn();
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }

      ArrayList<Long> idIndexList = new ArrayList<Long>();
      HashMap<Long, ContentState> transIdStateList = new HashMap<Long, ContentState>();

      List<HTextFlow> textFlows;

      if (action.getPhrase() != null && !action.getPhrase().isEmpty())
      {
         log.info("find message:" + action.getPhrase());
         List<Long> idList = textFlowDAO.getNavigationBy(action.getId(), action.getPhrase().toLowerCase(), action.getWorkspaceId().getLocaleId());

         textFlows = textFlowDAO.findByIdList(idList);
      }
      else
      {
         textFlows = textFlowDAO.getNavigationByDocumentId(action.getId());
      }

      for (HTextFlow textFlow : textFlows)
      {
         HTextFlowTarget textFlowTarget = textFlow.getTargets().get(hLocale.getId());
         if (checkStateAndValidate(action.isNewState(), action.isFuzzyState(), action.isApprovedState(), textFlowTarget))
         {
            if (textFlowTarget == null)
            {
               transIdStateList.put(textFlow.getId(), ContentState.New);
            }
            else
            {
               transIdStateList.put(textFlow.getId(), textFlowTarget.getState());
            }
            idIndexList.add(textFlow.getId());
         }
      }
      log.info("returned size: " + idIndexList.size());

      return new GetTransUnitsNavigationResult(new DocumentId(action.getId()), idIndexList, transIdStateList);
   }

   @Override
   public void rollback(GetTransUnitsNavigation action, GetTransUnitsNavigationResult result, ExecutionContext context) throws ActionException
   {
   }

   private boolean checkStateAndValidate(boolean isNewState, boolean isFuzzyState, boolean isApprovedState, HTextFlowTarget textFlowTarget)
   {
      if ((isNewState && isFuzzyState && isApprovedState) || (!isNewState && !isFuzzyState && !isApprovedState))
      {
         return true;
      }

      if (isNewState && isFuzzyState)
      {
         return isNewState(textFlowTarget) || isFuzzyState(textFlowTarget);
      }
      else if (isNewState && isApprovedState)
      {
         return isNewState(textFlowTarget) || isApprovedState(textFlowTarget);
      }
      else if (isFuzzyState && isApprovedState)
      {
         return isFuzzyState(textFlowTarget) || isApprovedState(textFlowTarget);
      }
      else if (isFuzzyState)
      {
         return isFuzzyState(textFlowTarget);
      }
      else if (isNewState)
      {
         return isNewState(textFlowTarget);
      }
      else if (isApprovedState)
      {
         return isApprovedState(textFlowTarget);
      }
      return false;
   }

   private boolean isFuzzyState(HTextFlowTarget textFlowTarget)
   {
      return textFlowTarget != null && textFlowTarget.getState() == ContentState.NeedReview;
   }

   private boolean isNewState(HTextFlowTarget textFlowTarget)
   {
      return textFlowTarget == null || textFlowTarget.getState() == ContentState.New;
   }

   private boolean isApprovedState(HTextFlowTarget textFlowTarget)
   {
      return textFlowTarget != null && textFlowTarget.getState() == ContentState.Approved;
   }

}
