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
import java.util.List;
import java.util.Set;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
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

@Name("webtrans.gwt.GetTransUnitNavigationHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitsNavigation.class)
public class GetTransUnitNavigationHandler extends AbstractActionHandler<GetTransUnitsNavigation, GetTransUnitsNavigationResult>
{

   @Logger
   Log log;
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
         throw new ActionException(e.getMessage());
      }

      HTextFlow tf = textFlowDAO.findById(action.getId(), false);
      List<Long> results = new ArrayList<Long>();
      List<HTextFlow> textFlows = new ArrayList<HTextFlow>();
      if (action.getPhrase() != null && !action.getPhrase().isEmpty())
      {
         log.info("find message:" + action.getPhrase());
         Set<Object[]> idSet = textFlowDAO.getNavigationBy(tf.getDocument().getId(), action.getPhrase().toLowerCase(), tf.getPos(), action.getWorkspaceId().getLocaleId(), action.isReverse());
         log.info("size: " + idSet.size());
         Long step = 0L;
         int count = 0;

         for (Object[] id : idSet)
         {
            if (count < action.getCount())
            {
               Long textFlowId = (Long) id[0];
               step++;
               HTextFlow textFlow = textFlowDAO.findById(textFlowId, false);
               HTextFlowTarget textFlowTarget = textFlow.getTargets().get(hLocale);
               if (checkStateAndValidate(action.isNewState(), action.isFuzzyState(), textFlowTarget))
               {
                  results.add(step);
                  log.info("add navigation step: " + step);
                  count++;
               }
            }
            else
            {
               break;
            }
         }
      }
      else
      {
         textFlows = textFlowDAO.getNavigationByDocumentId(tf.getDocument().getId(), tf.getPos(), action.isReverse());
         int count = 0;
         Long step = 0L;
         for (HTextFlow textFlow : textFlows)
         {
            if (count < action.getCount())
            {
               step++;
               HTextFlowTarget textFlowTarget = textFlow.getTargets().get(hLocale);
               log.info(action.isNewState() + ":" + action.isFuzzyState() + ":" + checkStateAndValidate(action.isNewState(), action.isFuzzyState(), textFlowTarget));
               if (checkStateAndValidate(action.isNewState(), action.isFuzzyState(), textFlowTarget))
               {
                  results.add(step);
                  log.info("add navigation step: " + step);
                  count++;
               }
            }
            else
            {
               break;
            }
         }
      }

      return new GetTransUnitsNavigationResult(new DocumentId(tf.getDocument().getId()), results);
   }

   @Override
   public void rollback(GetTransUnitsNavigation action, GetTransUnitsNavigationResult result, ExecutionContext context) throws ActionException
   {
   }

   private boolean checkStateAndValidate(boolean isNewState, boolean isFuzzyState, HTextFlowTarget textFlowTarget)
   {
      if (isNewState && isFuzzyState)
      {
         return isNewFuzzyState(textFlowTarget);
      }
      else if (isFuzzyState)
      {
         return isFuzzyState(textFlowTarget);
      }
      else if (isNewState)
      {
         return isNewState(textFlowTarget);
      }
      return false;
   }

   private boolean isNewFuzzyState(HTextFlowTarget textFlowTarget)
   {
      return textFlowTarget == null || textFlowTarget.getState() == ContentState.New || textFlowTarget.getState() == ContentState.NeedReview;
   }

   private boolean isFuzzyState(HTextFlowTarget textFlowTarget)
   {
      return textFlowTarget != null && textFlowTarget.getState() == ContentState.NeedReview;
   }

   private boolean isNewState(HTextFlowTarget textFlowTarget)
   {
      return textFlowTarget == null || textFlowTarget.getState() == ContentState.New;
   }

}
