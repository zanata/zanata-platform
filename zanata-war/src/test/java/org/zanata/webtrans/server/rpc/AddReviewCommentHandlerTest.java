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

import org.jglue.cdiunit.InRequestScope;
import org.zanata.ZanataTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LocaleService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewComment;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;

import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class AddReviewCommentHandlerTest extends ZanataTest {
    @Inject @Any
    private AddReviewCommentHandler handler;

    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private org.zanata.service.SecurityService securityServiceImpl;
    @Produces @Mock
    private org.zanata.dao.TextFlowTargetDAO textFlowTargetDAO;
    @Produces @Mock
    private org.zanata.dao.TextFlowTargetReviewCommentsDAO textFlowTargetReviewCommentsDAO;
    @Produces @Mock @Authenticated
    private org.zanata.model.HAccount authenticatedAccount;
    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HTextFlowTarget hTextFlowTarget;
    @Produces @Mock
    private HPerson hPerson;
    @Produces @Mock
    private HTextFlowTargetReviewComment hReviewComment;
    private DocumentId documentId = new DocumentId(1L, "my/doc");
    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HTextFlow hTextFlow;
    @Produces @Mock
    private LocaleService localeService;
    @Produces @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Produces @Mock
    private TranslationWorkspace workspace;
    @Produces @Mock
    private ZanataIdentity identity;
    @Mock
    private HProject hProject;

    @Test(expected = ActionException.class)
    @InRequestScope
    public void testExecuteWithBlankComment() throws ActionException {
        String blankComment = "   \t \n";
        AddReviewCommentAction action =
                new AddReviewCommentAction(new TransUnitId(1L), blankComment,
                        documentId);

        handler.execute(action, null);
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        // Given: we want to add comment to trans unit id 2 and locale id DE
        String commentContent = "new comment";
        TransUnitId transUnitId = new TransUnitId(2L);
        HLocale hLocale = new HLocale(LocaleId.DE);
        AddReviewCommentAction action =
                new AddReviewCommentAction(transUnitId, commentContent,
                        documentId);
        action.setWorkspaceId(TestFixture.workspaceId(LocaleId.DE));
        when(authenticatedAccount.getPerson()).thenReturn(hPerson);
        when(securityServiceImpl.checkWorkspaceStatus(action.getWorkspaceId()))
                .thenReturn(hProject);
        when(
                translationWorkspaceManager.getOrRegisterWorkspace(action
                        .getWorkspaceId())).thenReturn(workspace);
        when(localeService.getByLocaleId(action.getWorkspaceId().getLocaleId()))
                .thenReturn(hLocale);
        when(
                textFlowTargetDAO.getTextFlowTarget(transUnitId.getValue(),
                        hLocale.getLocaleId())).thenReturn(hTextFlowTarget);
        when(hTextFlowTarget.getState()).thenReturn(ContentState.Rejected);
        when(hTextFlowTarget.getTextFlow()).thenReturn(hTextFlow);
        when(hTextFlowTarget.addReviewComment(commentContent, hPerson))
                .thenReturn(hReviewComment);
        when(hReviewComment.getId()).thenReturn(1L);

        // When:
        AddReviewCommentResult result = handler.execute(action, null);

        // Then:
        InOrder inOrder =
                Mockito.inOrder(textFlowTargetDAO,
                        textFlowTargetReviewCommentsDAO,
                        hTextFlowTarget, workspace,
                        securityServiceImpl, identity);
        inOrder.verify(securityServiceImpl).checkWorkspaceStatus(
                action.getWorkspaceId());
        inOrder.verify(textFlowTargetDAO).getTextFlowTarget(
                transUnitId.getValue(), hLocale.getLocaleId());
        inOrder.verify(identity).checkPermission("review-comment", hLocale,
                hProject);
        inOrder.verify(hTextFlowTarget).addReviewComment(commentContent,
                hPerson);
        inOrder.verify(textFlowTargetReviewCommentsDAO).flush();
        inOrder.verify(workspace).publish(isA(AddReviewComment.class));

        assertThat(result.getComment().getId(),
                Matchers.equalTo(new ReviewCommentId(1L)));
    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void testExecuteWhenTargetIsNull() throws Exception {
        // Given: we want to add comment to trans unit id 1 and locale id DE but
        // target is null
        String commentContent = "new comment";
        AddReviewCommentAction action =
                new AddReviewCommentAction(new TransUnitId(1L), commentContent,
                        documentId);
        action.setWorkspaceId(TestFixture.workspaceId(LocaleId.DE));
        when(authenticatedAccount.getPerson()).thenReturn(hPerson);
        when(hPerson.getName()).thenReturn("John Smith");
        when(textFlowTargetDAO.getTextFlowTarget(1L, LocaleId.DE)).thenReturn(
                null);

        // When:
        handler.execute(action, null);
    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void testExecuteWhenTargetIsUntranslated() throws Exception {
        // Given: we want to add comment to trans unit id 1 and locale id DE but
        // target is new
        String commentContent = "new comment";
        AddReviewCommentAction action =
                new AddReviewCommentAction(new TransUnitId(1L), commentContent,
                        documentId);
        action.setWorkspaceId(TestFixture.workspaceId(LocaleId.DE));
        when(authenticatedAccount.getPerson()).thenReturn(hPerson);
        when(hPerson.getName()).thenReturn("John Smith");
        when(textFlowTargetDAO.getTextFlowTarget(1L, LocaleId.DE)).thenReturn(
                hTextFlowTarget);
        when(hTextFlowTarget.getState()).thenReturn(ContentState.New);

        // When:
        handler.execute(action, null);
    }
}
