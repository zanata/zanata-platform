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

import java.util.*;

import org.jboss.seam.*;
import org.jboss.seam.annotations.*;
import org.zanata.common.*;
import org.zanata.model.*;
import org.zanata.service.*;
import org.zanata.service.TranslationService.*;
import org.zanata.webtrans.server.*;
import org.zanata.webtrans.shared.auth.*;
import org.zanata.webtrans.shared.model.*;
import org.zanata.webtrans.shared.rpc.*;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.*;

import net.customware.gwt.dispatch.server.*;
import net.customware.gwt.dispatch.shared.*;

@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateTransUnit.class)
public class UpdateTransUnitHandler extends AbstractActionHandler<UpdateTransUnit, UpdateTransUnitResult>
{
   @In(value = "webtrans.gwt.TransUnitUpdateHelper", create = true)
   private TransUnitUpdateHelper transUnitUpdateHelper;

   @In
   private TranslationService translationServiceImpl;

   @In
   private SecurityService securityServiceImpl;

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {
      SecurityService.SecurityCheckResult securityCheckResult;

      if (action.getUpdateType() == UpdateType.WebEditorSaveReview)
      {
         securityCheckResult = securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.REVIEW);
      }
      else
      {
         securityCheckResult = securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY);
      }

      HLocale hLocale = securityCheckResult.getLocale();
      TranslationWorkspace workspace = securityCheckResult.getWorkspace();

      return doTranslation(hLocale.getLocaleId(), workspace, action.getUpdateRequests(), action.getEditorClientId(),
            action.getUpdateType());
   }

   protected UpdateTransUnitResult doTranslation(LocaleId localeId, TranslationWorkspace workspace,
         List<TransUnitUpdateRequest> updateRequests, EditorClientId editorClientId,
         TransUnitUpdated.UpdateType updateType)
   {
      List<TranslationResult> translationResults = translationServiceImpl.translate(localeId, updateRequests);
      return transUnitUpdateHelper.generateUpdateTransUnitResult(translationResults, editorClientId, updateType, workspace);
   }

   @Override
   public void rollback(UpdateTransUnit action, UpdateTransUnitResult result, ExecutionContext context)
         throws ActionException
   {
      // TODO implement rollback by checking result for success
      // if success, looking up base revision from action and set values back to that
      // only if concurrent change conditions are satisfied
      // conditions: no new translations after this one

      // this should just use calls to a service to replace with previous version
      // by version num (fail if previousVersion != latestVersion-1)
   }
}
