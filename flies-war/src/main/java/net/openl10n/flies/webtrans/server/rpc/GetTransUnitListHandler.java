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
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.shared.model.TransUnit;
import net.openl10n.flies.webtrans.shared.model.TransUnitId;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitList;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitListResult;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetTransUnitsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTransUnitList.class)
public class GetTransUnitListHandler extends AbstractActionHandler<GetTransUnitList, GetTransUnitListResult>
{

   @Logger
   Log log;
   @In
   private Session session;

   @In
   private LocaleService localeServiceImpl;

   @SuppressWarnings("unchecked")
   @Override
   public GetTransUnitListResult execute(GetTransUnitList action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      log.info("Fetching Transunits for {0}", action.getDocumentId());

      if (action.getPhrase() != null && !action.getPhrase().isEmpty())
      {
         return getUnitsByFilter(action);
      }

      Query query = session.createQuery("from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id order by tf.pos").setParameter("id", action.getDocumentId().getValue());
      int size = query.list().size();

      List<HTextFlow> textFlows = query.setFirstResult(action.getOffset()).setMaxResults(action.getCount()).list();

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (HTextFlow textFlow : textFlows)
      {

         TransUnitId tuId = new TransUnitId(textFlow.getId());

         // EditState editstate = workspace.getTransUnitStatus(tuId);
         TransUnit tu = new TransUnit(tuId, action.getWorkspaceId().getLocaleId(), textFlow.getContent(), CommentsUtil.toString(textFlow.getComment()), "", ContentState.New);
         HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(action.getWorkspaceId().getLocaleId());
         HTextFlowTarget target = textFlow.getTargets().get(hLocale);
         if (target != null)
         {
            tu.setTarget(target.getContent());
            tu.setStatus(target.getState());
         }
         units.add(tu);
      }
      return new GetTransUnitListResult(action.getDocumentId(), units, size);
   }

   @Override
   public void rollback(GetTransUnitList action, GetTransUnitListResult result, ExecutionContext context) throws ActionException
   {
   }


   @SuppressWarnings("unchecked")
   private GetTransUnitListResult getUnitsByFilter(GetTransUnitList action)
   {
      log.info("find message:" + action.getPhrase());
      Query textFlowQuery = session.createQuery("select tf.id, tf.pos from HTextFlow tf where tf.obsolete=0 and tf.document.id = :id and lower(tf.content) like :content order by tf.pos").setParameter("id", action.getDocumentId().getValue()).setParameter("content", "%" + action.getPhrase().toLowerCase() + "%");
      List<Object[]> ids1 = textFlowQuery.list();
      Query textFlowTargetQuery = session.createQuery("select tft.textFlow.id, tft.textFlow.pos from HTextFlowTarget tft where tft.textFlow.obsolete=0 and tft.textFlow.document.id = :id and lower(tft.content) like :content and tft.locale.localeId = :localeId order by tft.textFlow.pos").setParameter("id", action.getDocumentId().getValue()).setParameter("content", "%" + action.getPhrase().toLowerCase() + "%").setParameter("localeId", action.getWorkspaceId().getLocaleId());
      List<Object[]> ids2 = textFlowTargetQuery.list();
      Set<Object[]> idSet = new TreeSet<Object[]>(new Comparator<Object[]>()
      {
         @Override
         public int compare(Object[] arg0, Object[] arg1)
         {
            return ((Integer) arg0[1]).compareTo((Integer) arg1[1]);
         }
      });
      idSet.addAll(ids1);
      idSet.addAll(ids2);
      int size = idSet.size();
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
         log.info("add textflow : {0}", para[0]);
         idList.add((Long) para[0]);
      }

      
      Query query = session.createQuery("FROM HTextFlow WHERE id in (:idList) order by pos");
      query.setParameterList("idList", idList);
      List<HTextFlow> textFlows = query.list();

      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (HTextFlow textFlow : textFlows)
      {

         TransUnitId tuId = new TransUnitId(textFlow.getId());

         // EditState editstate = workspace.getTransUnitStatus(tuId);
         TransUnit tu = new TransUnit(tuId, action.getWorkspaceId().getLocaleId(), textFlow.getContent(), CommentsUtil.toString(textFlow.getComment()), "", ContentState.New);
         HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(action.getWorkspaceId().getLocaleId());
         HTextFlowTarget target = textFlow.getTargets().get(hLocale);
         if (target != null)
         {
            tu.setTarget(target.getContent());
            tu.setStatus(target.getState());
         }
         units.add(tu);
      }
      return new GetTransUnitListResult(action.getDocumentId(), units, size);
   }

}