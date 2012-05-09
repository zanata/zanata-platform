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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.LoggerFactory;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.util.TextFlowFilter;
import org.zanata.webtrans.shared.util.TextFlowFilterImpl;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Name("webtrans.gwt.GetTransUnitListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitList.class)
public class GetTransUnitListHandler extends AbstractActionHandler<GetTransUnitList, GetTransUnitListResult>
{
   private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GetTransUnitListHandler.class);

   @In
   private TransUnitTransformer transUnitTransformer;

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public GetTransUnitListResult execute(GetTransUnitList action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      LOGGER.info("Fetching TransUnits for document {}", action.getDocumentId());

      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (ZanataServiceException e)
      {
         throw new ActionException(e);
      }

      int gotoRow = -1;

      List<HTextFlow> textFlows;

      // FIXME use hibernate search instead of string comparison here
      TextFlowFilter filter;

      if ((action.getPhrase() != null && !action.getPhrase().isEmpty()) || (action.isFilterTranslated() || action.isFilterNeedReview() || action.isFilterUntranslated()))
      {
         LOGGER.info("Fetch TransUnits: {}", action.getPhrase());
         filter = new TextFlowFilterImpl(action.getPhrase(), action.isFilterTranslated(), action.isFilterNeedReview(), action.isFilterUntranslated());
         textFlows = textFlowDAO.getTransUnitList(action.getDocumentId().getValue());
      }
      else
      {
         LOGGER.info("Fetch TransUnits:*");
         filter = new TextFlowFilterImpl();
         textFlows = textFlowDAO.getTransUnitList(action.getDocumentId().getValue());
      }

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (HTextFlow textFlow : textFlows)
      {
         if (!filter.isFilterOut(textFlow, hLocale))
         {
            TransUnit tu = transUnitTransformer.transform(textFlow, hLocale);
            if (action.getTargetTransUnitId() != null && tu.getId().equals(action.getTargetTransUnitId()))
            {
               gotoRow = units.size();
            }
            units.add(tu);
         }
      }
      int size = units.size();

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

}