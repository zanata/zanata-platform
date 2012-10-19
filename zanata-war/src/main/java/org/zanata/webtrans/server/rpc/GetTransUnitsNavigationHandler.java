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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.hibernate.transform.ResultTransformer;
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
import org.zanata.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.util.HTextFlowPosComparator;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import com.google.common.base.Strings;

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

   @In
   ZanataIdentity identity;

   @In
   private TextFlowSearchService textFlowSearchServiceImpl;


   @Override
   public GetTransUnitsNavigationResult execute(GetTransUnitsNavigation action, ExecutionContext context) throws ActionException
   {

      identity.checkLoggedIn();
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

      if (Strings.isNullOrEmpty(action.getPhrase()))
      {
         textFlows = textFlowDAO.getNavigationByDocumentId(action.getId(), hLocale, new TextFlowResultTransformer(hLocale));
      }
      else
      {
         log.info("find message:" + action.getPhrase());
         textFlows = searchByPhrase(action);
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

   private List<HTextFlow> searchByPhrase(GetTransUnitsNavigation action)
   {
      FilterConstraints constraints = FilterConstraints.filterBy(action.getPhrase()).ignoreCase();

      List<HTextFlow> textFlows = textFlowSearchServiceImpl.findTextFlows(action.getWorkspaceId(), new DocumentId(action.getId()), constraints);
      Collections.sort(textFlows, HTextFlowPosComparator.INSTANCE);
      return textFlows;
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

   private static class SimpleHTextFlow extends HTextFlow
   {
      public SimpleHTextFlow(Long id, ContentState contentState, HLocale hLocale)
      {
         super();
         setId(id);
         HTextFlowTarget target = new HTextFlowTarget(this, hLocale);
         target.setState(contentState);
         getTargets().put(hLocale.getId(), target);
      }
   }

   public static class TextFlowResultTransformer implements ResultTransformer
   {
      public static final String ID = "id";
      public static final String CONTENT_STATE = "state";
      private final HLocale hLocale;

      public TextFlowResultTransformer(HLocale hLocale)
      {
         this.hLocale = hLocale;
      }

      @Override
      public SimpleHTextFlow transformTuple(Object[] tuple, String[] aliases)
      {
         Long id = null;
         ContentState state = null;
         for (int i = 0, aliasesLength = aliases.length; i < aliasesLength; i++)
         {
            String columnName = aliases[i];
            if (columnName.equals(ID))
            {
               id = (Long) tuple[i];
            }
            if (columnName.equals(CONTENT_STATE))
            {
               Integer index = (Integer) tuple[i];
               state = index == null ? ContentState.New : ContentState.values() [index];
            }
         }
         log.debug(" {} - {}", id, state);
         return new SimpleHTextFlow(id, state, hLocale);
      }

      @Override
      public List<HTextFlow> transformList(List collection)
      {
         return collection;
      }
   }

}
