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

import org.apache.commons.lang3.StringUtils;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ReviewCriteriaDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.ReviewCriteria;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.ReviewCriterionId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.AddReviewComment;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("webtrans.gwt.AddReviewCommentHandler")
@ActionHandlerFor(AddReviewCommentAction.class)
@RequestScoped
@Transactional
public class AddReviewCommentHandler extends
        AbstractActionHandler<AddReviewCommentAction, AddReviewCommentResult> {
    @Inject
    private SecurityService securityServiceImpl;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private TextFlowTargetReviewCommentsDAO textFlowTargetReviewCommentsDAO;
    @Inject
    private ReviewCriteriaDAO reviewCriteriaDAO;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Inject
    private ZanataIdentity identity;

    @Override
    public AddReviewCommentResult execute(AddReviewCommentAction action,
            ExecutionContext context) throws ActionException {
        throwExceptionIfCommentIsInvalid(action);
        WorkspaceId workspaceId = action.getWorkspaceId();
        HProject project =
                securityServiceImpl.checkWorkspaceStatus(workspaceId);
        HTextFlowTarget hTextFlowTarget = textFlowTargetDAO.getTextFlowTarget(
                action.getTransUnitId().getValue(), workspaceId.getLocaleId());
        if (hTextFlowTarget == null
                || hTextFlowTarget.getState().isUntranslated()) {
            throw new ActionException(
                    "comment on untranslated message is pointless!");
        }
        HLocale locale =
                localeServiceImpl.getByLocaleId(workspaceId.getLocaleId());
        identity.checkPermission("review-comment", locale, project);
        TranslationWorkspace workspace =
                translationWorkspaceManager.getOrRegisterWorkspace(workspaceId);
        HTextFlowTargetReviewComment hComment;
        if (action.hasReviewCriterion()) {
            ReviewCriterionId reviewId = action.getReviewId();
            ReviewCriteria reviewCriteria = reviewCriteriaDAO.findById(reviewId.getId());
            if (reviewCriteria == null) {
                throw new ActionException("can not find review criteria with id:" + reviewId);
            }
            hComment = hTextFlowTarget.addReview(authenticatedAccount.getPerson(), reviewCriteria, action.getContent());
        } else {
            hComment = hTextFlowTarget.addReviewComment(action.getContent(),
                    authenticatedAccount.getPerson());
        }
        textFlowTargetReviewCommentsDAO.makePersistent(hComment);
        textFlowTargetReviewCommentsDAO.flush();
        AddReviewComment commentEvent = new AddReviewComment(
                new TransUnitId(hTextFlowTarget.getTextFlow().getId()),
                hTextFlowTarget.getReviewComments().size());
        workspace.publish(commentEvent);
        return new AddReviewCommentResult(toDTO(hComment));
    }

    private void throwExceptionIfCommentIsInvalid(AddReviewCommentAction action)
            throws ActionException {
        if (!action.hasReviewCriterion() && StringUtils.isBlank(action.getContent())) {
            throw new ActionException("comment can not be blank");
        }
    }

    private static ReviewComment toDTO(HTextFlowTargetReviewComment hComment) {
        return new ReviewComment(new ReviewCommentId(hComment.getId()),
                hComment.getComment(), hComment.getCommenterUsername(),
                hComment.getCommenterName(),
                hComment.getCreationDate());
    }

    @Override
    public void rollback(AddReviewCommentAction action,
            AddReviewCommentResult result, ExecutionContext context)
            throws ActionException {
    }
}
