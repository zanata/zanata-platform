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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.util.TextFlowFilter;
import org.zanata.webtrans.shared.util.TextFlowFilterImpl;

@Name("webtrans.gwt.GetTransUnitListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitList.class)
public class GetTransUnitListHandler extends AbstractActionHandler<GetTransUnitList, GetTransUnitListResult>
{

   @Logger
   Log log;

   @In
   private ResourceUtils resourceUtils;

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public GetTransUnitListResult execute(GetTransUnitList action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      log.info("Fetching Transunits for document {0}", action.getDocumentId());

      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e.getMessage());
      }

      int gotoRow = -1, size = 0;

      List<HTextFlow> textFlows;
      TextFlowFilter filter;

      if ((action.getPhrase() != null && !action.getPhrase().isEmpty()) || (action.isFilterTranslated() || action.isFilterNeedReview() || action.isFilterUntranslated()))
      {
         log.info("Fetch TransUnits:" + action.getPhrase());
         filter = new TextFlowFilterImpl(action.getPhrase(), action.isFilterTranslated(), action.isFilterNeedReview(), action.isFilterUntranslated());
         textFlows = textFlowDAO.getTransUnitList(action.getDocumentId().getValue());
      }
      else
      {
         log.info("Fetch TransUnits:*");
         filter = new TextFlowFilterImpl();
         textFlows = textFlowDAO.getTransUnitList(action.getDocumentId().getValue());
         // result =
         // textFlowDAO.getTransUnitList(action.getDocumentId().getValue(),
         // action.getOffset(), action.getCount());
         // size =
         // textFlowDAO.getCountByDocument(action.getDocumentId().getValue());
      }

      HDocument document = documentDAO.getById(action.getDocumentId().getId());
      int nPlurals = resourceUtils.getNumPlurals(document, hLocale);

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (HTextFlow textFlow : textFlows)
      {
         if (!filter.isFilterOut(textFlow, hLocale))
         {
            TransUnit tu = initTransUnit(textFlow, hLocale, nPlurals);
            if (action.getTargetTransUnitId() != null && tu.getId().equals(action.getTargetTransUnitId()))
            {
               gotoRow = units.size();
            }
            units.add(tu);
         }
      }
      size = units.size();

      if ((action.getOffset() + action.getCount()) < units.size())
      {
         units.subList(action.getOffset() + action.getCount(), units.size()).clear();
         units.subList(0, action.getOffset()).clear();
      }
      else if (action.getOffset() < units.size())
      {
         units.subList(0, action.getOffset()).clear();
      }

      return new GetTransUnitListResult(action.getDocumentId(), units, size, gotoRow);
   }

   @Override
   public void rollback(GetTransUnitList action, GetTransUnitListResult result, ExecutionContext context) throws ActionException
   {
   }

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
         builder.setLastModifiedTime(new SimpleDateFormat().format(target.getLastChanged()));
      }
      return builder.build();
   }

}