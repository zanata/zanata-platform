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
import org.zanata.rest.service.ResourceUtils;
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

   @In
   private ResourceUtils resourceUtils;

   @Override
   public GetProjectTransUnitListsResult execute(GetProjectTransUnitLists action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      log.info("Searching all targets for workspace {0}", action.getWorkspaceId().toString());

      HashMap<Long, List<TransUnit>> matchingTUs = new HashMap<Long, List<TransUnit>>();
      HashMap<Long, String> docPaths = new HashMap<Long, String>();
      if ((action.getSearchString() == null || action.getSearchString().isEmpty()))
      {
         // TODO empty searches shouldn't be requested, consider replacing this
         // with an error, or making behaviour return all targets for the
         // project (consider performance).
         return new GetProjectTransUnitListsResult(docPaths, matchingTUs);
      }

      // TODO handle exception thrown by search service
      List<HTextFlowTarget> matchingFlows = textFlowSearchServiceImpl.findTextFlowTargets(action.getWorkspaceId(), FilterConstraints.filterBy(action.getSearchString()).ignoreSource().excludeNew().caseSensitive(action.isCaseSensitive()));
      log.info("Returned {0} results for search", matchingFlows.size());

      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e.getMessage());
      }

      for (HTextFlowTarget htft : matchingFlows)
      {
         HTextFlow htf = htft.getTextFlow();
         List<TransUnit> listForDoc = matchingTUs.get(htf.getDocument().getId());
         if (listForDoc == null)
         {
            listForDoc = new ArrayList<TransUnit>();
         }
         // FIXME add a check for leading and trailing whitespace to compensate
         // for NGramAnalyzer trimming strings before tokenization. This should
         // be removed when updating to a lucene version with the whitespace
         // issue resolved

         // TODO cache this rather than looking up repeatedly
         int nPlurals = resourceUtils.getNumPlurals(htf.getDocument(), hLocale);
         listForDoc.add(initTransUnit(htf, hLocale, nPlurals));
         matchingTUs.put(htf.getDocument().getId(), listForDoc);
         docPaths.put(htf.getDocument().getId(), htf.getDocument().getDocId());
      }

      return new GetProjectTransUnitListsResult(docPaths, matchingTUs);
   }

   @Override
   public void rollback(GetProjectTransUnitLists action, GetProjectTransUnitListsResult result, ExecutionContext context) throws ActionException
   {
   }

   private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

   // TODO move to shared location with other search code
   private TransUnit initTransUnit(HTextFlow textFlow, HLocale hLocale, int nPlurals)
   {
      String msgContext = null;
      if (textFlow.getPotEntryData() != null)
      {
         msgContext = textFlow.getPotEntryData().getContext();
      }
      HTextFlowTarget target = textFlow.getTargets().get(hLocale);

      ArrayList<String> sourceContents = GwtRpcUtil.getSourceContents(textFlow);
      ArrayList<String> targetContents = GwtRpcUtil.getTargetContentsWithPadding(textFlow, target, nPlurals);

      TransUnit.Builder builder = TransUnit.Builder.newTransUnitBuilder()
            .setId(textFlow.getId())
            .setResId(textFlow.getResId())
            .setLocaleId(hLocale.getLocaleId())
            .setPlural(textFlow.isPlural())
            .setSources(sourceContents)
            .setSourceComment(CommentsUtil.toString(textFlow.getComment()))
            .setTargets(targetContents)
            .setMsgContext(msgContext)
            .setRowIndex(textFlow.getPos());

      if (target == null)
      {
         builder.setStatus(ContentState.New);
      }
      else
      {
         builder.setStatus(target.getState());
         if (target.getLastModifiedBy() != null)
         {
            builder.setLastModifiedBy(target.getLastModifiedBy().getName());
         }
         builder.setLastModifiedTime(simpleDateFormat.format(target.getLastChanged()));
      }
      return builder.build();
   }

}