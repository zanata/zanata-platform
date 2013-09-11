/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("webtrans.gwt.AddReviewCommentHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(AddReviewCommentAction.class)
@Slf4j
public class AddReviewCommentHandler extends AbstractActionHandler<AddReviewCommentAction, AddReviewCommentResult>
{
   @In
   private SecurityService securityServiceImpl;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In(value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @In
   TransUnitTransformer transUnitTransformer;

   @In
   private LocaleService localeServiceImpl;

   @In
   private TranslationWorkspaceManager translationWorkspaceManager;

   @In
   private ZanataIdentity identity;

   @Override
   public AddReviewCommentResult execute(AddReviewCommentAction action, ExecutionContext context) throws ActionException
   {
      throwExceptionIfCommentIsInvalid(action);

      WorkspaceId workspaceId = action.getWorkspaceId();
      HProject project = securityServiceImpl.checkWorkspaceStatus(workspaceId);

      HTextFlowTarget hTextFlowTarget = textFlowTargetDAO.getTextFlowTarget(action.getTransUnitId().getValue(), workspaceId.getLocaleId());
      if (hTextFlowTarget == null || hTextFlowTarget.getState().isUntranslated())
      {
         throw new ActionException("comment on untranslated message is pointless!");
      }

      HLocale locale = localeServiceImpl.getByLocaleId(workspaceId.getLocaleId());

      identity.checkPermission("review-comment", locale, project);

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(workspaceId);

      HTextFlowTargetReviewComment hComment = hTextFlowTarget.addReviewComment(action.getContent(), authenticatedAccount.getPerson());
      textFlowTargetDAO.makePersistent(hTextFlowTarget);
      textFlowTargetDAO.flush();

      publishTransUnitUpdatedEvent(action, locale, hTextFlowTarget, workspace);

      return new AddReviewCommentResult(toDTO(hComment));
   }

   private void throwExceptionIfCommentIsInvalid(AddReviewCommentAction action) throws ActionException
   {
      if (StringUtils.isBlank(action.getContent()))
      {
         throw new ActionException("comment can not be blank");
      }
   }

   private void publishTransUnitUpdatedEvent(AddReviewCommentAction action, HLocale hLocale, HTextFlowTarget hTextFlowTarget, TranslationWorkspace workspace)
   {
      HTextFlow textFlow = hTextFlowTarget.getTextFlow();
      TransUnit transUnit = transUnitTransformer.transform(textFlow, hTextFlowTarget, hLocale);
      TransUnitUpdated transUnitUpdated = new TransUnitUpdated(new TransUnitUpdateInfo(true, false, action.getDocumentId(), transUnit, textFlow.getWordCount().intValue(), transUnit.getVerNum(), transUnit.getStatus()), action.getEditorClientId(), TransUnitUpdated.UpdateType.AddComment);
      workspace.publish(transUnitUpdated);
   }

   private static ReviewComment toDTO(HTextFlowTargetReviewComment hComment)
   {
      return new ReviewComment(new ReviewCommentId(hComment.getId()), hComment.getComment(), hComment.getCommenterName(), hComment.getCreationDate(), hComment.getTargetVersion());
   }

   @Override
   public void rollback(AddReviewCommentAction action, AddReviewCommentResult result, ExecutionContext context) throws ActionException
   {

   }
}
