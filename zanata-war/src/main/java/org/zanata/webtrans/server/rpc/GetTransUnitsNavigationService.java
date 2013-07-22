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
import java.util.Map;

import org.hibernate.transform.ResultTransformer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

import lombok.extern.slf4j.Slf4j;

@Name("webtrans.gwt.GetTransUnitsNavigationHandler")
@Scope(ScopeType.STATELESS)
@Slf4j
public class GetTransUnitsNavigationService
{

   @In
   private TextFlowDAO textFlowDAO;

   protected GetTransUnitsNavigationResult getNavigationIndexes(GetTransUnitsNavigation action, HLocale hLocale)
   {
      // @formatter:off
      FilterConstraints filterConstraints = FilterConstraints.builder()
            .filterBy(action.getPhrase())
            .checkInSource(true).checkInTarget(true)
            .includeStates(action.getActiveStates())
            .build();
      // @formatter:on

      List<TransUnitId> idIndexList = new ArrayList<TransUnitId>();
      Map<TransUnitId, ContentState> transIdStateMap = new HashMap<TransUnitId, ContentState>();

      List<HTextFlow> textFlows;
      TextFlowResultTransformer resultTransformer = new TextFlowResultTransformer(hLocale);

      textFlows = textFlowDAO.getNavigationByDocumentId(action.getId(), hLocale, resultTransformer, filterConstraints);
      for (HTextFlow textFlow : textFlows)
      {
         TransUnitId transUnitId = new TransUnitId(textFlow.getId());
         idIndexList.add(transUnitId);
         transIdStateMap.put(transUnitId, textFlow.getTargets().get(hLocale.getId()).getState());
      }

      log.debug("for action {} returned size: {}", action, idIndexList.size());
      return new GetTransUnitsNavigationResult(idIndexList, transIdStateMap);
   }

   /**
    * This class is just so we can set id (protected) and avoid hibernate proxies.
    */
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
         GetTransUnitsNavigationService.log.debug(" {} - {}", id, state);
         return new SimpleHTextFlow(id, state, hLocale);
      }

      @Override
      @SuppressWarnings("unchecked")
      public List<HTextFlow> transformList(List collection)
      {
         return collection;
      }
   }

}
