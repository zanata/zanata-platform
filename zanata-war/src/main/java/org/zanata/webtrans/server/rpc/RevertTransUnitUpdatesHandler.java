/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.dao.ProjectDAO;
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
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author David Mason, damason@redhat.com
 *
 * @see RevertTransUnitUpdates
 */
@Name("webtrans.gwt.RevertTransUnitUpdatesHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(RevertTransUnitUpdates.class)
public class RevertTransUnitUpdatesHandler extends AbstractActionHandler<RevertTransUnitUpdates, UpdateTransUnitResult>
{

   // TODO may want to add a "revert-translation" permission, if it would add any value
   private static final String ACTION_MODIFY_TRANSLATION = "modify-translation";

   @Logger
   Log log;

   @In
   TranslationService translationServiceImpl;

   @In
   TransUnitTransformer transUnitTransformer;

   @In
   ZanataIdentity identity;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   ProjectDAO projectDAO;

   @In
   LocaleService localeServiceImpl;


   @Override
   public UpdateTransUnitResult execute(RevertTransUnitUpdates action, ExecutionContext context) throws ActionException
   {
      List<TranslationResult> revertResults = translationServiceImpl.revertTranslations(action.getWorkspaceId().getLocaleId(), action.getUpdatesToRevert());

      UpdateTransUnitResult results = new UpdateTransUnitResult();
      for (TranslationResult translationResult : revertResults)
      {
         HTextFlowTarget newTarget = translationResult.getTranslatedTextFlowTarget();
         HTextFlow hTextFlow = newTarget.getTextFlow();
         int wordCount = hTextFlow.getWordCount().intValue();
         TransUnit tu = transUnitTransformer.transform(hTextFlow, newTarget.getLocale());
         TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(translationResult.isTranslationSuccessful(), new DocumentId(hTextFlow.getDocument().getId()), tu, wordCount, translationResult.getBaseVersionNum(), translationResult.getBaseContentState());

         TranslationWorkspace workspace = checkSecurityAndGetWorkspace(action);
         workspace.publish(new TransUnitUpdated(updateInfo, action.getSessionId()));
         results.addUpdateResult(updateInfo);
      }
      return results;
   }

   @Override
   public void rollback(RevertTransUnitUpdates action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
      // TODO Auto-generated method stub
   }


   // FIXME duplicated exactly from UpdateTransUnitHandler, so should probably be moved to a common service
   // or make a common supertype that can provide this common utility
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

}
