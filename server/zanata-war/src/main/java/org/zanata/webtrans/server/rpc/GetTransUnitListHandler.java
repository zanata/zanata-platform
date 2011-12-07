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
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;


@Name("webtrans.gwt.GetTransUnitListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitList.class)
public class GetTransUnitListHandler extends AbstractActionHandler<GetTransUnitList, GetTransUnitListResult>
{

   @Logger
   Log log;

   @In
   TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   private static SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat();

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

      int size = 0;
      List<HTextFlow> textFlows = new ArrayList<HTextFlow>();
      if (action.getPhrase() != null && !action.getPhrase().isEmpty())
      {
         log.info("find message:" + action.getPhrase());
         Set<Object[]> idSet = textFlowDAO.getIdsBySearch(action.getDocumentId().getValue(), action.getOffset(), action.getCount(), action.getPhrase(), action.getWorkspaceId().getLocaleId());
         size = idSet.size();
         log.info("size : {0}", size);
         log.info("action.getOffset() : {0}", action.getOffset());
         log.info("action.getCount() : {0}", action.getCount());

         List<Object[]> subIds = new ArrayList<Object[]>();
         if ((action.getOffset() + action.getCount()) < size)
         {
            subIds = new ArrayList<Object[]>(idSet).subList(action.getOffset(), action.getOffset() + action.getCount());
         }
         else if (action.getOffset() < size)
         {
            subIds = new ArrayList<Object[]>(idSet).subList(action.getOffset(), size);
         }
         List<Long> idList = new ArrayList<Long>();
         for (Object[] para : subIds)
         {
            idList.add((Long) para[0]);
         }

         textFlows = textFlowDAO.findByIdList(idList);
      }
      else
      {
         size = textFlowDAO.getByDocument(action.getDocumentId().getValue()).size();
         textFlows = textFlowDAO.getOffsetListByDocument(action.getDocumentId().getValue(), action.getOffset(), action.getCount());
      }

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (HTextFlow textFlow : textFlows)
      {

         TransUnitId tuId = new TransUnitId(textFlow.getId());

         String msgContext = null;
         if (textFlow.getPotEntryData() != null)
         {
            msgContext = textFlow.getPotEntryData().getContext();
         }

         TransUnit tu = new TransUnit(tuId, textFlow.getResId(), action.getWorkspaceId().getLocaleId(), textFlow.getContent(), CommentsUtil.toString(textFlow.getComment()), "", ContentState.New, "", "", msgContext);
         HTextFlowTarget target = textFlow.getTargets().get(hLocale);
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
         units.add(tu);
      }
      return new GetTransUnitListResult(action.getDocumentId(), units, size);
   }

   @Override
   public void rollback(GetTransUnitList action, GetTransUnitListResult result, ExecutionContext context) throws ActionException
   {
   }

}