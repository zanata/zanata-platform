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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.ContentState;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;

/**
 * @see GetProjectTransUnitLists
 * @author David Mason, damason@redhat.com
 */
@Name("webtrans.gwt.GetProjectTransUnitListsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetProjectTransUnitLists.class)
public class GetProjectTransUnitListsHandler extends AbstractActionHandler<GetProjectTransUnitLists, GetProjectTransUnitListsResult>
{

   @Logger
   Log log;

   @In
   TextFlowDAO textFlowDAO;

   @In
   DocumentDAO documentDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   private TextFlowSearchService textFlowSearchServiceImpl;

   private static SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat();

   @Override
   public GetProjectTransUnitListsResult execute(GetProjectTransUnitLists action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      log.info("Searching all targets for workspace {0}", action.getWorkspaceId().toString());

      HashMap<Long, List<TransUnit>> matchingTUs = new HashMap<Long, List<TransUnit>>();
      HashMap<Long, String> docPaths = new HashMap<Long, String>();
      if ((action.getSearchString() == null || action.getSearchString().isEmpty()))
      {
         //TODO empty searches shouldn't be requested, consider replacing this with an error.
         return new GetProjectTransUnitListsResult(docPaths, matchingTUs);
      }

      //TODO handle exception thrown by search service
      List<HTextFlow> matchingFlows = textFlowSearchServiceImpl.findTextFlows(action.getWorkspaceId(), FilterConstraints.filterBy(action.getSearchString()).ignoreSource().excludeNew());


      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e.getMessage());
      }

      for (HTextFlow htf : matchingFlows)
      {
         List<TransUnit> listForDoc = matchingTUs.get(htf.getDocument().getId());
         if (listForDoc == null)
         {
            listForDoc = new ArrayList<TransUnit>();
         }
         listForDoc.add(initTransUnit(htf, hLocale));
         matchingTUs.put(htf.getDocument().getId(), listForDoc);
         docPaths.put(htf.getDocument().getId(), htf.getDocument().getDocId());
      }

      return new GetProjectTransUnitListsResult(docPaths, matchingTUs);
   }

   @Override
   public void rollback(GetProjectTransUnitLists action, GetProjectTransUnitListsResult result, ExecutionContext context) throws ActionException
   {
   }

   private TransUnit initTransUnit(HTextFlow textFlow, HLocale hLocale)
   {
      String msgContext = null;
      if (textFlow.getPotEntryData() != null)
      {
         msgContext = textFlow.getPotEntryData().getContext();
      }
      HTextFlowTarget target = textFlow.getTargets().get(hLocale);
      TransUnit tu = new TransUnit(new TransUnitId(textFlow.getId()), textFlow.getResId(), hLocale.getLocaleId(), textFlow.getContent(), CommentsUtil.toString(textFlow.getComment()), "", ContentState.New, "", "", msgContext, textFlow.getPos());
      if (target != null)
      {
         tu.setTarget(target.getContent());
         tu.setStatus(target.getState());
         if (target.getLastModifiedBy() != null)
         {
            tu.setLastModifiedBy(target.getLastModifiedBy().getName());
         }
         tu.setLastModifiedTime(SIMPLE_FORMAT.format(target.getLastChanged()));
      }
      return tu;
   }

}