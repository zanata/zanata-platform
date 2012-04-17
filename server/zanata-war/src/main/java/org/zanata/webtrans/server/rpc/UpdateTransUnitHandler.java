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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.hibernate.Session;
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
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import static com.google.common.collect.Lists.*;

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
   TranslationService translationServiceImpl;

   @In
   private ResourceUtils resourceUtils;

   @In
   ZanataIdentity identity;
   
   @In(value = JpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION)
   HAccount authenticatedAccount;

   @In
   ProjectDAO projectDAO;

   @In
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

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
         TextFlowTargetHistoryDAO textFlowTargetHistoryDAO,
         TranslationWorkspaceManager translationWorkspaceManager,
         LocaleService localeServiceImpl,
         HAccount authenticatedAccount,
         TranslationService translationService)
   {
      this.translationServiceImpl = translationService;
      this.log = Logging.getLog(UpdateTransUnitHandler.class);
      this.identity = identity;
      this.projectDAO = projectDAO;
      this.textFlowTargetHistoryDAO = textFlowTargetHistoryDAO;
      this.translationWorkspaceManager = translationWorkspaceManager;
      this.localeServiceImpl = localeServiceImpl;
      this.authenticatedAccount = authenticatedAccount;
   }

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {
      LocaleId localeId = action.getWorkspaceId().getLocaleId();
      log.debug("Updating TransUnit {0}: locale {1}, state {2}, content '{3}'", action.getTransUnitId(), localeId, action.getContentState(), action.getContents());
      TranslationWorkspace workspace = checkSecurityAndGetWorkspace(action);

      TranslationService.TranslationResult translationResult;
      try
      {
         translationResult = translationServiceImpl.translate(action.getTransUnitId().getValue(), localeId, action.getContentState(), action.getContents());
      }
      catch (Exception e)
      {
         throw new ActionException(e.getMessage());
      }

      HTextFlow hTextFlow = translationResult.getTextFlow();
      HTextFlowTarget newTarget = translationResult.getNewTextFlowTarget();
      HTextFlowTarget prevTarget = translationResult.getPreviousTextFlowTarget();

      manageRedo(action, prevTarget);

      UpdateTransUnit previous = new UpdateTransUnit(action.getTransUnitId(), newArrayList(prevTarget.getContents()), prevTarget.getState());

      int wordCount = hTextFlow.getWordCount().intValue();
      // @formatter:off
      
      String msgContext = null;
      if(hTextFlow.getPotEntryData() != null) 
      {
         msgContext = hTextFlow.getPotEntryData().getContext();
      }

      ArrayList<String> sourceContents = GwtRpcUtil.getSourceContents(hTextFlow);
      TransUnit tu = new TransUnit(
            action.getTransUnitId(), 
            hTextFlow.getResId(),
            localeId,
            hTextFlow.isPlural(),
            sourceContents,
            CommentsUtil.toString(hTextFlow.getComment()),
            action.getContents(), 
            newTarget.getState(),
            authenticatedAccount.getPerson().getName(),
            new SimpleDateFormat().format(new Date()), msgContext, hTextFlow.getPos());
      // @formatter:on
      TransUnitUpdated event = new TransUnitUpdated(new DocumentId(hTextFlow.getDocument().getId()), wordCount, newTarget.getState(), tu, identity.getCredentials().getUsername());

      workspace.publish(event);

      UpdateTransUnitResult result = new UpdateTransUnitResult(true);
      result.setPrevious(previous);
      result.setCurrentVersionNum(newTarget.getVersionNum());

      return result;
   }

   private TranslationWorkspace checkSecurityAndGetWorkspace(UpdateTransUnit action) throws ActionException
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

   private void manageRedo(UpdateTransUnit action, HTextFlowTarget prevTarget) throws ActionException
   {
      if (action.isRedo())
      {
         if (prevTarget == null)
         {
            throw new ActionException("Redo Failure due to empty string.");
         }
         if (!prevTarget.getVersionNum().equals(action.getVerNum()))
         {
            if (!prevTarget.getLastModifiedBy().getAccount().getUsername().equals(authenticatedAccount.getUsername()) || textFlowTargetHistoryDAO.findConflictInHistory(prevTarget, action.getVerNum(), authenticatedAccount.getUsername()))
            {
               throw new ActionException("Find conflict, Redo Failure.");
            }
         }
      }
   }

   @Override
   public void rollback(UpdateTransUnit action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
      LocaleId localeId = action.getWorkspaceId().getLocaleId();
      log.debug("revert TransUnit {0}: locale {1}, state {2}, content '{3}'", action.getTransUnitId(), localeId, action.getContentState(), action.getContents());
      TranslationWorkspace workspace = checkSecurityAndGetWorkspace(action);

      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
      //TODO This part of the code is related to undo/redo and it's not functioning so will be commented out. IN fact the whole method seems not being used at the moment(except undo calls it).
//      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
//      HTextFlowTarget target = hTextFlow.getTargets().get(hLocale);
//
//      if (target == null)
//      {
//         throw new ActionException("Undo Failure due to empty string.");
//      }
//
//      if (!target.getVersionNum().equals(result.getCurrentVersionNum()))
//      {
//         if (!target.getLastModifiedBy().getAccount().getUsername().equals(authenticatedAccount.getUsername()) || textFlowTargetHistoryDAO.findConflictInHistory(target, result.getCurrentVersionNum(), authenticatedAccount.getUsername()))
//         {
//            throw new ActionException("Find conflict, Undo Failure.");
//         }
//      }

      TranslationService.TranslationResult translationResult = translationServiceImpl.translate(action.getTransUnitId().getValue(), localeId, result.getPrevious().getContentState(), result.getPrevious().getContents());
      HTextFlow hTextFlow = translationResult.getTextFlow();
      HTextFlowTarget prevTarget = translationResult.getPreviousTextFlowTarget();

      int wordCount = hTextFlow.getWordCount().intValue();
      String msgContext = null;
      if (hTextFlow.getPotEntryData() != null)
      {
         msgContext = hTextFlow.getPotEntryData().getContext();
      }

      int nPlurals = resourceUtils.getNumPlurals(hTextFlow.getDocument(), hLocale);
      ArrayList<String> sourceContents = GwtRpcUtil.getSourceContents(hTextFlow);
      ArrayList<String> targetContents = GwtRpcUtil.getTargetContentsWithPadding(hTextFlow, prevTarget, nPlurals);
      String modifiedBy = null;
      String lastChanged = null;
      if (prevTarget != null && prevTarget.getLastModifiedBy() != null && prevTarget.getLastChanged() != null)
      {
         modifiedBy = prevTarget.getLastModifiedBy().getName();
         lastChanged = new SimpleDateFormat().format(prevTarget.getLastChanged());
      }

      // @formatter:off
      TransUnit tu = new TransUnit(
            action.getTransUnitId(), 
            hTextFlow.getResId(),
            localeId,
            hTextFlow.isPlural(),
            sourceContents,
            CommentsUtil.toString(hTextFlow.getComment()),
            targetContents, 
            result.getPrevious().getContentState(),
            modifiedBy,
            lastChanged, msgContext, hTextFlow.getPos());
      // @formatter:on
      TransUnitUpdated event = new TransUnitUpdated(new DocumentId(hTextFlow.getDocument().getId()), wordCount, result.getPrevious().getContentState(), tu, identity.getCredentials().getUsername());
      workspace.publish(event);
   }

}