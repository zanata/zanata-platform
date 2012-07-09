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
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.SecurityService;
import org.zanata.service.TranslationService;
import org.zanata.service.TranslationService.TranslationResult;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.UpdateType;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RevertTransUnitUpdatesHandler extends AbstractActionHandler<RevertTransUnitUpdates, UpdateTransUnitResult>
{
   @In
   TranslationService translationServiceImpl;

   @In
   TransUnitTransformer transUnitTransformer;

   @In
   SecurityService securityServiceImpl;

   @Override
   public UpdateTransUnitResult execute(RevertTransUnitUpdates action, ExecutionContext context) throws ActionException
   {
      SecurityService.SecurityCheckResult securityCheckResult = securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY);
      HLocale hLocale = securityCheckResult.getLocale();
      TranslationWorkspace workspace = securityCheckResult.getWorkspace();

      List<TranslationResult> revertResults = translationServiceImpl.revertTranslations(hLocale.getLocaleId(), action.getUpdatesToRevert());

      UpdateTransUnitResult results = new UpdateTransUnitResult();
      for (TranslationResult translationResult : revertResults)
      {
         HTextFlowTarget newTarget = translationResult.getTranslatedTextFlowTarget();
         HTextFlow hTextFlow = newTarget.getTextFlow();
         int wordCount = hTextFlow.getWordCount().intValue();
         TransUnit tu = transUnitTransformer.transform(hTextFlow, newTarget.getLocale());
         TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(translationResult.isTranslationSuccessful(), new DocumentId(hTextFlow.getDocument().getId()), tu, wordCount, translationResult.getBaseVersionNum(), translationResult.getBaseContentState());

         workspace.publish(new TransUnitUpdated(updateInfo, action.getEditorClientId(), UpdateType.Revert));
         results.addUpdateResult(updateInfo);
      }
      return results;
   }

   @Override
   public void rollback(RevertTransUnitUpdates action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
   }
}
