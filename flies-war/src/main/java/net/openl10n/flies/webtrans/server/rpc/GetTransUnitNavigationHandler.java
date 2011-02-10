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
import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsNavigation;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsNavigationResult;

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
   Session session;


   @In
   private LocaleService localeServiceImpl;

   @Override
   public GetTransUnitsNavigationResult execute(GetTransUnitsNavigation action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();
      List<Long> results = getFuzzyOrUntranslated(action);

      return new GetTransUnitsNavigationResult(action.getDocumentId(), results);
   }

   @Override
   public void rollback(GetTransUnitsNavigation action, GetTransUnitsNavigationResult result, ExecutionContext context) throws ActionException
   {
   }

   private List<Long> getFuzzyOrUntranslated(GetTransUnitsNavigation action)
   {
      List<Long> results = new ArrayList<Long>();
      List<HTextFlow> textFlows = new ArrayList<HTextFlow>();
      int count = 0;
      if (action.isReverse())
      {
         textFlows = session.createQuery("from HTextFlow tf where tf.document.id = :id " + " and tf.pos < :offset " + " order by tf.pos desc").setParameter("offset", action.getOffset()).setParameter("id", action.getDocumentId().getValue()).list();
      }
      else
      {
         textFlows = session.createQuery("from HTextFlow tf where tf.document.id = :id " + " and tf.pos > :offset " + " order by tf.pos").setParameter("offset", action.getOffset()).setParameter("id", action.getDocumentId().getValue()).list();
      }

      HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(action.getWorkspaceId().getLocaleId());
      for (HTextFlow textFlow : textFlows)
      {
         if (count < action.getCount())
         {
            HTextFlowTarget textFlowTarget = textFlow.getTargets().get(hLocale);
            if (textFlowTarget == null)
            {
               // log.info("new :" + new Long(textFlow.getPos()));
               results.add(new Long(textFlow.getPos()));
               count++;
            }
            else if (textFlowTarget.getState() == ContentState.New || textFlowTarget.getState() == ContentState.NeedReview)
            {
               // log.info("new or fuzzy:" + new Long(textFlow.getPos()));
               results.add(new Long(textFlow.getPos()));
               count++;
            }
         }
      }
      return results;
   }

}
