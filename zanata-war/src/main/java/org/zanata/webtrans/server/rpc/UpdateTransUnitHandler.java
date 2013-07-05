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
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;


@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateTransUnit.class)
public class UpdateTransUnitHandler extends AbstractActionHandler<UpdateTransUnit, UpdateTransUnitResult>
{
   @In
   TransUnitTransformer transUnitTransformer;

   @In
   TranslationService translationServiceImpl;

   @In
   SecurityService securityServiceImpl;

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {
      SecurityService.SecurityCheckResult securityCheckResult = securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY);
      HLocale hLocale = securityCheckResult.getLocale();
      TranslationWorkspace workspace = securityCheckResult.getWorkspace();

      return doTranslation(hLocale.getLocaleId(), workspace, action.getUpdateRequests(), action.getEditorClientId(), action.getUpdateType());
   }

   protected UpdateTransUnitResult doTranslation(LocaleId localeId, TranslationWorkspace workspace, List<TransUnitUpdateRequest> updateRequests, EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType)
   {
      UpdateTransUnitResult result = new UpdateTransUnitResult();
      List<TranslationResult> translationResults = translationServiceImpl.translate(localeId, updateRequests);
      for (TranslationResult translationResult : translationResults)
      {
         HTextFlowTarget newTarget = translationResult.getTranslatedTextFlowTarget();
         HTextFlow hTextFlow = newTarget.getTextFlow();
         int wordCount = hTextFlow.getWordCount().intValue();
         TransUnit tu = transUnitTransformer.transform(hTextFlow, newTarget.getLocale());
         TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(translationResult.isTranslationSuccessful(), translationResult.isTargetChanged(), new DocumentId(hTextFlow.getDocument().getId(), hTextFlow.getDocument().getDocId()), tu, wordCount, translationResult.getBaseVersionNum(), translationResult.getBaseContentState());
         workspace.publish(new TransUnitUpdated(updateInfo, editorClientId, updateType));

         result.addUpdateResult(updateInfo);
      }

      return result;
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