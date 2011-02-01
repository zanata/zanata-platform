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
package net.openl10n.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.dao.TextFlowDAO;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsNavigation;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsNavigationResult;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

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
   private Session session;

   @In
   private LocaleService localeServiceImpl;

   @Override
   public GetTransUnitsNavigationResult execute(GetTransUnitsNavigation action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();
      List<Long> results = new ArrayList<Long>();
      HTextFlow tf = textFlowDAO.findById(action.getId(), false);
      if (action.getPhrase() != null && !action.getPhrase().isEmpty())
      {
         results = getNavigationUnitsByFilter(action);
      }
      else
      {
         List<HTextFlow> textFlows = new ArrayList<HTextFlow>();
         int count = 0;
         textFlows = textFlowDAO.getNavigationByDocumentId(tf.getDocument().getId(), tf.getPos(), action.isReverse());
         HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(action.getWorkspaceId().getLocaleId());
         Long step = 0L;
         for (HTextFlow textFlow : textFlows)
         {
            if (count < action.getCount())
            {
               step++;
               HTextFlowTarget textFlowTarget = textFlow.getTargets().get(hLocale);
               if (checkNewFuzzyState(textFlowTarget))
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

   private boolean checkNewFuzzyState(HTextFlowTarget textFlowTarget)
   {
      if (textFlowTarget == null)
      {
         return true;
      }
      else if (textFlowTarget.getState() == ContentState.New || textFlowTarget.getState() == ContentState.NeedReview)
      {
         return true;
      }
      return false;
   }

   @SuppressWarnings("unchecked")
   private List<Long> getNavigationUnitsByFilter(GetTransUnitsNavigation action)
   {
      List<Long> results = new ArrayList<Long>();
      int count = 0;
      HTextFlow tf = textFlowDAO.findById(action.getId(), false);
      log.info("find message:" + action.getPhrase());
      Query textFlowQuery;
      Query textFlowTargetQuery;
      Set<Object[]> idSet;
      if (action.isReverse())
      {
         textFlowQuery = session.createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content and tf.pos < :offset  order by tf.pos desc").setParameter("id", tf.getDocument().getId()).setParameter("content", "%" + action.getPhrase().toLowerCase() + "%");
         textFlowTargetQuery = session.createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId and tft.textFlow.pos < :offset order by tft.textFlow.pos desc").setParameter("id", tf.getDocument().getId()).setParameter("content", "%" + action.getPhrase().toLowerCase() + "%").setParameter("localeId", action.getWorkspaceId().getLocaleId());
         idSet = new TreeSet<Object[]>(new Comparator<Object[]>()
         {
            @Override
            public int compare(Object[] arg0, Object[] arg1)
            {
               return ((Integer) arg1[1]).compareTo((Integer) arg0[1]);
            }
         });
      }
      else
      {
         textFlowQuery = session.createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content and tf.pos > :offset  order by tf.pos").setParameter("id", tf.getDocument().getId()).setParameter("content", "%" + action.getPhrase().toLowerCase() + "%");
         textFlowTargetQuery = session.createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId and tft.textFlow.pos > :offset order by tft.textFlow.pos").setParameter("id", tf.getDocument().getId()).setParameter("content", "%" + action.getPhrase().toLowerCase() + "%").setParameter("localeId", action.getWorkspaceId().getLocaleId());
         idSet = new TreeSet<Object[]>(new Comparator<Object[]>()
         {
            @Override
            public int compare(Object[] arg0, Object[] arg1)
            {
               return ((Integer) arg0[1]).compareTo((Integer) arg1[1]);
            }
         });
      }
      textFlowQuery.setParameter("offset", tf.getPos());
      textFlowTargetQuery.setParameter("offset", tf.getPos());

      List<Object[]> ids1 = textFlowQuery.list();
      List<Object[]> ids2 = textFlowTargetQuery.list();
      idSet.addAll(ids1);
      idSet.addAll(ids2);
      log.info("size: " + idSet.size());
      Long step = 0L;

      for (Object[] id : idSet)
      {
         if (count < action.getCount())
         {
            Long textFlowId = (Long) id[0];
            step++;
            HTextFlow textFlow = textFlowDAO.findById(textFlowId, false);
            HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(action.getWorkspaceId().getLocaleId());
            HTextFlowTarget textFlowTarget = textFlow.getTargets().get(hLocale);
            if (checkNewFuzzyState(textFlowTarget))
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
      return results;
   }

}
