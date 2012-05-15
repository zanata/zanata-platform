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

import java.util.List;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;


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
   TransUnitTransformer transUnitTransformer;

   @In
   TranslationService translationServiceImpl;

   @In
   ZanataIdentity identity;

   @In(value = JpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION)
   HAccount authenticatedAccount;

   @In
   ProjectDAO projectDAO;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   private LocaleService localeServiceImpl;

   /**
    * Used by Seam
    */
   public UpdateTransUnitHandler()
   {
   }

   /**
    * Used for tests
    */
   public UpdateTransUnitHandler(
         ZanataIdentity identity,
         ProjectDAO projectDAO,
         TranslationWorkspaceManager translationWorkspaceManager,
         LocaleService localeServiceImpl,
         HAccount authenticatedAccount,
         TranslationService translationService,
         TransUnitTransformer transUnitTransformer)
   {
      this.transUnitTransformer = transUnitTransformer;
      this.translationServiceImpl = translationService;
      this.log = Logging.getLog(UpdateTransUnitHandler.class);
      this.identity = identity;
      this.projectDAO = projectDAO;
      this.translationWorkspaceManager = translationWorkspaceManager;
      this.localeServiceImpl = localeServiceImpl;
      this.authenticatedAccount = authenticatedAccount;
   }

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {
      UpdateTransUnitResult result = new UpdateTransUnitResult();
      LocaleId localeId = action.getWorkspaceId().getLocaleId();
      log.debug("Updating {0} TransUnits for loacle {1}", action.getUpdateRequests().size(), localeId);
      TranslationWorkspace workspace = checkSecurityAndGetWorkspace(action);

      List<TranslationResult> translationResults = translationServiceImpl.translate(localeId, action.getUpdateRequests());

      for (TranslationResult translationResult : translationResults)
      {
         HTextFlowTarget newTarget = translationResult.getTranslatedTextFlowTarget();
         HTextFlow hTextFlow = newTarget.getTextFlow();
         int wordCount = hTextFlow.getWordCount().intValue();
         TransUnit tu = transUnitTransformer.transform(hTextFlow, newTarget.getLocale());
         TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(translationResult.isTranslationSuccessful(), new DocumentId(hTextFlow.getDocument().getId()), tu, wordCount, translationResult.getBaseVersionNum(), translationResult.getBaseContentState());
         workspace.publish(new TransUnitUpdated(updateInfo, action.getSessionId()));

         result.addUpdateResult(updateInfo);
      }

      return result;
   }

   private TranslationWorkspace checkSecurityAndGetWorkspace(AbstractWorkspaceAction<?> action) throws ActionException
   {
      identity.checkLoggedIn();
      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());
      if (workspace.getWorkspaceContext().isReadOnly())
      {
         throw new ActionException("Project or version is read-only");
      }

      HProject hProject = projectDAO.getBySlug( action.getWorkspaceId().getProjectIterationId().getProjectSlug() );
      HLocale hLocale = localeServiceImpl.getByLocaleId(action.getWorkspaceId().getLocaleId());
      identity.checkPermission(ACTION_MODIFY_TRANSLATION, hLocale, hProject);

      return workspace;
   }

   @Override
   public void rollback(UpdateTransUnit action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
      // TODO implement rollback by checking result for success
      // if success, looking up base revision from action and set values back to that
      // only if concurrent change conditions are satisfied
      // conditions: no new translations after this one

      // this should just use calls to a service to replace with previous version
      // by version num (fail if previousVersion != latestVersion-1)
   }

}