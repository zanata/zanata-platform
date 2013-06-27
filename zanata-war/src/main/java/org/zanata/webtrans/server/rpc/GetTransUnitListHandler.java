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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Name("webtrans.gwt.GetTransUnitListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitList.class)
@Slf4j
public class GetTransUnitListHandler extends AbstractActionHandler<GetTransUnitList, GetTransUnitListResult>
{
   @In
   private TransUnitTransformer transUnitTransformer;

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   private ZanataIdentity identity;

   @In
   private ValidationService validationServiceImpl;

   @In(value = "webtrans.gwt.GetTransUnitsNavigationHandler", create = true)
   private GetTransUnitsNavigationService getTransUnitsNavigationService;

   @Override
   public GetTransUnitListResult execute(GetTransUnitList action, ExecutionContext context) throws ActionException
   {
      identity.checkLoggedIn();
      HLocale hLocale = validateAndGetLocale(action);

      log.info("action: {}", action);
      int targetOffset = action.getOffset();
      int targetPage = action.getOffset() / action.getCount();
      GetTransUnitsNavigationResult navigationResult = null;
      if (action.isNeedReloadIndex())
      {
         GetTransUnitsNavigation getTransUnitsNavigation = new GetTransUnitsNavigation(action.getDocumentId().getId(), action.getPhrase(), action.isFilterUntranslated(), action.isFilterNeedReview(), action.isFilterTranslated(), action.isFilterApproved(), action.isFilterRejected());
         log.debug("get trans unit navigation action: {}", getTransUnitsNavigation);
         navigationResult = getTransUnitsNavigationService.getNavigationIndexes(getTransUnitsNavigation, hLocale);

         int totalPageNumber = navigationResult.getIdIndexList().size() / action.getCount();
         if (targetPage > totalPageNumber)
         {
            targetPage = totalPageNumber;
            targetOffset = action.getCount() * targetPage;
         }
            
         if (action.getTargetTransUnitId() != null)
         {
            int targetIndexInDoc = navigationResult.getIdIndexList().indexOf(action.getTargetTransUnitId());
            targetPage = targetIndexInDoc / action.getCount();
            targetOffset = action.getCount() * targetPage;
         }
      }

      List<HTextFlow> textFlows = getTextFlows(action, hLocale, targetOffset);
      
      GetTransUnitListResult result = transformToTransUnits(action, hLocale, textFlows, targetOffset, targetPage);
      result.setNavigationIndex(navigationResult);
      return result;
   }

   private List<HTextFlow> getTextFlows(GetTransUnitList action, HLocale hLocale, int offset)
   {
      List<HTextFlow> textFlows;
      // no status and phrase filter
      if (!hasStatusAndPhaseFilter(action))
      {
         // no validation filter
         log.debug("Fetch TransUnits:*");
         if (!hasValidationFilter(action))
         {
            textFlows = textFlowDAO.getTextFlowsByDocumentId(action.getDocumentId(), offset, action.getCount());
         }
         // has validation filter
         else
         {
            textFlows = textFlowDAO.getAllTextFlowsByDocumentId(action.getDocumentId());
            textFlows = validationServiceImpl.filterHasErrorTexFlow(textFlows, action.getValidationIds(), hLocale.getLocaleId(), offset, action.getCount());
         }
      }
      // has status and phrase filter
      else
      {
         // @formatter:off
         FilterConstraints constraints = FilterConstraints
               .filterBy(action.getPhrase()).ignoreCase().filterSource().filterTarget()
               .filterByStatus(action.isFilterUntranslated(), action.isFilterNeedReview(), action.isFilterTranslated(), action.isFilterApproved(), action.isFilterRejected());
         // @formatter:on
         log.debug("Fetch TransUnits filtered by status and/or search: {}", constraints);
         if (!hasValidationFilter(action))
         {
            textFlows = textFlowDAO.getTextFlowByDocumentIdWithConstraint(action.getDocumentId(), hLocale, constraints, offset, action.getCount());
         }
         // has validation filter
         else
         {
            textFlows = textFlowDAO.getAllTextFlowByDocumentIdWithConstraint(action.getDocumentId(), hLocale, constraints);
            textFlows = validationServiceImpl.filterHasErrorTexFlow(textFlows, action.getValidationIds(), hLocale.getLocaleId(), offset, action.getCount());
         }
      }
      return textFlows;
   }

   private boolean hasStatusAndPhaseFilter(GetTransUnitList action)
   {
      return !action.isAcceptAllStatus() || StringUtils.isNotBlank(action.getPhrase());
   }

   private boolean hasValidationFilter(GetTransUnitList action)
   {
      return action.isFilterHasError() && action.getValidationIds() != null && !action.getValidationIds().isEmpty();
   }

   private HLocale validateAndGetLocale(GetTransUnitList action) throws ActionException
   {
      try
      {
         return localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }
   }

   private GetTransUnitListResult transformToTransUnits(GetTransUnitList action, HLocale hLocale, List<HTextFlow> textFlows, int targetOffset, int targetPage)
   {
      List<TransUnit> units = Lists.transform(textFlows, new HTextFlowToTransUnitFunction(hLocale, transUnitTransformer));

      int gotoRow = 0;
      if (action.getTargetTransUnitId() != null)
      {
         int row = Iterables.indexOf(units, new FindByTransUnitIdPredicate(action.getTargetTransUnitId()));
         if (row != -1)
         {
            gotoRow = row;
         }
      }
      // stupid GWT RPC can't handle com.google.common.collect.Lists$TransformingRandomAccessList
      return new GetTransUnitListResult(action.getDocumentId(), Lists.newArrayList(units), gotoRow, targetOffset, targetPage);
   }

   @Override
   public void rollback(GetTransUnitList action, GetTransUnitListResult result, ExecutionContext context) throws ActionException
   {
   }

   private static class HTextFlowToTransUnitFunction implements Function<HTextFlow, TransUnit>
   {
      private final HLocale hLocale;
      private final TransUnitTransformer transformer;

      public HTextFlowToTransUnitFunction(HLocale hLocale, TransUnitTransformer transformer)
      {
         this.hLocale = hLocale;
         this.transformer = transformer;
      }

      @Override
      public TransUnit apply(HTextFlow input)
      {
         return transformer.transform(input, hLocale);
      }
   }
}