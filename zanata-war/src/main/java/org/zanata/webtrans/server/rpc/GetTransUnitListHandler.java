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
import java.util.List;

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
import org.zanata.service.TextFlowSearchService;
import org.zanata.util.HTextFlowPosComparator;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

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
   private TextFlowSearchService textFlowSearchServiceImpl;

   @In
   private ZanataIdentity identity;

   @Override
   public GetTransUnitListResult execute(GetTransUnitList action, ExecutionContext context) throws ActionException
   {
      identity.checkLoggedIn();
      log.info("Fetching TransUnits for document {}", action.getDocumentId());
      log.debug("action: {}", action);

      final HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }

      List<HTextFlow> textFlows;

      if (hasSearchPhrase(action))
      {
         log.debug("Fetch TransUnits: {}", action.getPhrase());
         textFlows = searchByPhrase(action);
      }
      else if (action.isAcceptAllStatus())
      {
         log.debug("Fetch TransUnits:*");
         return getTransUnitsWithPage(action, hLocale);
      }
      else
      {
         log.debug("Fetch TransUnits filtered by status: {}", action);
         textFlows = textFlowDAO.getTextFlowsByStatus(action.getDocumentId(), hLocale, action.isFilterTranslated(), action.isFilterNeedReview(), action.isFilterUntranslated());
      }

      int gotoRow = -1;
      int size = textFlows.size();
      int startIndex = action.getOffset();
      int endIndex = Math.min(action.getOffset() + action.getCount(), size);
      log.debug("loading index from {} to {}", startIndex, endIndex);

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (int i = startIndex; i < endIndex; i++)
      {
         HTextFlow textFlow = textFlows.get(i);
         TransUnit tu = transUnitTransformer.transform(textFlow, hLocale);
         if (action.getTargetTransUnitId() != null && tu.getId().equals(action.getTargetTransUnitId()))
         {
            gotoRow = i;
         }
         units.add(tu);
      }
      log.debug("go to index {}", gotoRow);
      return new GetTransUnitListResult(action.getDocumentId(), units, gotoRow);
   }

   private boolean hasSearchPhrase(GetTransUnitList action)
   {
      return !Strings.isNullOrEmpty(action.getPhrase()) && action.getPhrase().trim().length() != 0;
   }

   private List<HTextFlow> searchByPhrase(GetTransUnitList action)
   {
      FilterConstraints constraints = FilterConstraints.filterBy(action.getPhrase()).ignoreCase();
      constraints.includeApproved().includeFuzzy().includeNew();
      if (!action.isAcceptAllStatus())
      {
         if (!action.isFilterNeedReview())
         {
            constraints.excludeFuzzy();
         }
         if (!action.isFilterTranslated())
         {
            constraints.excludeApproved();
         }
         if (!action.isFilterUntranslated())
         {
            constraints.excludeNew();
         }
      }

      List<HTextFlow> textFlows = textFlowSearchServiceImpl.findTextFlows(action.getWorkspaceId(), action.getDocumentId(), constraints);
      Collections.sort(textFlows, HTextFlowPosComparator.INSTANCE);
      return textFlows;
   }

   private GetTransUnitListResult getTransUnitsWithPage(GetTransUnitList action, final HLocale hLocale)
   {
      List<HTextFlow> textFlows = textFlowDAO.getTextFlows(action.getDocumentId(), action.getOffset(), action.getCount());
      List<TransUnit> units = Lists.transform(textFlows, new Function<HTextFlow, TransUnit>()
      {
         @Override
         public TransUnit apply(HTextFlow input)
         {
            return transUnitTransformer.transform(input, hLocale);
         }
      });

      int goToRow = -1;
      if (action.getTargetTransUnitId() != null)
      {
         goToRow = Iterables.indexOf(units, new FindByTransUnitIdPredicate(action.getTargetTransUnitId()));
      }
      return new GetTransUnitListResult(action.getDocumentId(), units, goToRow);
   }

   @Override
   public void rollback(GetTransUnitList action, GetTransUnitListResult result, ExecutionContext context) throws ActionException
   {
   }

}