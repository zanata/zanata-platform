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

import java.text.SimpleDateFormat;
import java.util.Date;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HProject;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspace;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnit;
import net.openl10n.flies.webtrans.shared.rpc.TransUnitUpdated;
import net.openl10n.flies.webtrans.shared.rpc.UpdateTransUnit;
import net.openl10n.flies.webtrans.shared.rpc.UpdateTransUnitResult;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateTransUnit.class)
public class UpdateTransUnitHandler extends AbstractActionHandler<UpdateTransUnit, UpdateTransUnitResult>
{
   // security actions (to be implemented)
   // private static final String ACTION_ADD_TRANSLATION = "add-translation";
   private static final String ACTION_MODIFY_TRANSLATION = "modify-translation";
   // private static final String ACTION_REMOVE_TRANSLATION =
   // "remove-translation";
   // private static final String ACTION_APPROVE_TRANSLATION =
   // "approve-translation";

   @Logger
   Log log;

   @In
   Session session;

   @In
   Identity identity;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   private LocaleService localeServiceImpl;

   private static SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat();

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();
      log.info("Updating TransUnit {0}: locale {1}, state {2}, content '{3}'", action.getTransUnitId(), action.getWorkspaceId().getLocaleId(), action.getContentState(), action.getContent());

      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
      LocaleId locale = action.getWorkspaceId().getLocaleId();
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(action.getWorkspaceId().getLocaleId(), action.getWorkspaceId().getProjectIterationId().getProjectSlug(), action.getWorkspaceId().getProjectIterationId().getIterationSlug());
      }
      catch (FliesServiceException e)
      {
         throw new ActionException(e.getMessage());
      }
      HProject hProject = hTextFlow.getDocument().getProjectIteration().getProject();
      identity.checkPermission(hProject, ACTION_MODIFY_TRANSLATION);

      HTextFlowTarget target = hTextFlow.getTargets().get(hLocale);
      ContentState prevStatus = ContentState.New;
      if (target == null)
      {
         target = new HTextFlowTarget(hTextFlow, hLocale);
         target.setVersionNum(0); // this will be incremented when content is
                                  // set (below)
         hTextFlow.getTargets().put(hLocale, target);
      }
      else
      {
         prevStatus = target.getState();
      }

      if (action.getContentState() == ContentState.New && StringUtils.isNotEmpty(action.getContent()))
      {
         log.error("invalid ContentState New for TransUnit {0} with content '{1}', assuming NeedReview", action.getTransUnitId(), action.getContent());
         target.setState(ContentState.NeedReview);
      }
      else if (action.getContentState() != ContentState.New && StringUtils.isEmpty(action.getContent()))
      {
         log.error("invalid ContentState {0} for empty TransUnit {1}, assuming New", action.getContentState(), action.getTransUnitId());
         target.setState(ContentState.New);
      }
      else
      {
         target.setState(action.getContentState());
      }
      HAccount authenticatedAccount = (HAccount) Contexts.getSessionContext().get(JpaIdentityStore.AUTHENTICATED_USER);
      
      
      if (!StringUtils.equals(action.getContent(), target.getContent()))
      {
         target.setContent(action.getContent());
         target.setVersionNum(target.getVersionNum() + 1);
         log.info("last modified by :" + authenticatedAccount.getPerson().getName());
         target.setLastModifiedBy(authenticatedAccount.getPerson());
      }

      session.flush();

      int wordCount = hTextFlow.getWordCount().intValue();
      TransUnit tu = new TransUnit(action.getTransUnitId(), locale, hTextFlow.getContent(), "", action.getContent(), action.getContentState(), authenticatedAccount.getPerson().getName(), SIMPLE_FORMAT.format(new Date()));
      TransUnitUpdated event = new TransUnitUpdated(new DocumentId(hTextFlow.getDocument().getId()), wordCount, prevStatus, tu);

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());
      workspace.publish(event);

      return new UpdateTransUnitResult(true);
   }

   @Override
   public void rollback(UpdateTransUnit action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
   }

}