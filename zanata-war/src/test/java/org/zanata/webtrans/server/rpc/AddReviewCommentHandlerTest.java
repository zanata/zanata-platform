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

import org.hamcrest.Matchers;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;

import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class AddReviewCommentHandlerTest
{
   private AddReviewCommentHandler handler;

   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   private org.zanata.service.SecurityService securityServiceImpl;
   @Mock
   private org.zanata.dao.TextFlowTargetDAO textFlowTargetDAO;
   @Mock
   private org.zanata.model.HAccount authenticatedAccount;
   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   private HTextFlowTarget hTextFlowTarget;
   @Mock
   private HPerson hPerson;
   @Mock
   private HTextFlowTargetReviewComment hReviewComment;
   private DocumentId documentId = new DocumentId(1L, "my/doc");
   @Mock
   private TransUnitTransformer transUnitTransformer;
   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   private HTextFlow hTextFlow;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance().reset()
            .use("securityServiceImpl", securityServiceImpl)
            .use("textFlowTargetDAO", textFlowTargetDAO)
            .use(JpaIdentityStore.AUTHENTICATED_USER, authenticatedAccount)
            .use("transUnitTransformer", transUnitTransformer)
            .autowire(AddReviewCommentHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws Exception
   {
      // Given: we want to add comment to trans unit id 2 and locale id DE
      String commentContent = "new comment";
      TransUnitId transUnitId = new TransUnitId(2L);
      HLocale hLocale = new HLocale(LocaleId.DE);
      AddReviewCommentAction action = new AddReviewCommentAction(transUnitId, commentContent, documentId);
      when(authenticatedAccount.getPerson()).thenReturn(hPerson);
      when(securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY).getLocale()).thenReturn(hLocale);
      when(textFlowTargetDAO.getTextFlowTarget(transUnitId.getValue(), hLocale.getLocaleId())).thenReturn(hTextFlowTarget);
      when(hTextFlowTarget.getState()).thenReturn(ContentState.Rejected);
      when(hTextFlowTarget.getTextFlow()).thenReturn(hTextFlow);
      when(transUnitTransformer.transform(hTextFlow, hTextFlowTarget, hLocale)).thenReturn(TestFixture.makeTransUnit(transUnitId.getId()));
      when(hTextFlowTarget.addReviewComment(commentContent, hPerson)).thenReturn(hReviewComment);
      when(hReviewComment.getId()).thenReturn(1L);

      // When:
      AddReviewCommentResult result = handler.execute(action, null);

      // Then:
      InOrder inOrder = Mockito.inOrder(textFlowTargetDAO, hTextFlowTarget);
      inOrder.verify(textFlowTargetDAO).getTextFlowTarget(transUnitId.getValue(), hLocale.getLocaleId());
      inOrder.verify(hTextFlowTarget).addReviewComment(commentContent, hPerson);
      inOrder.verify(textFlowTargetDAO).makePersistent(hTextFlowTarget);
      inOrder.verify(textFlowTargetDAO).flush();

      assertThat(result.getComment().getId(), Matchers.equalTo(new ReviewCommentId(1L)));
   }

   @Test(expectedExceptions = ActionException.class)
   public void testExecuteWhenTargetIsNull() throws Exception
   {
      // Given: we want to add comment to trans unit id 1 and locale id DE but target is null
      String commentContent = "new comment";
      AddReviewCommentAction action = new AddReviewCommentAction(new TransUnitId(1L), commentContent, documentId);
      when(authenticatedAccount.getPerson()).thenReturn(hPerson);
      when(hPerson.getName()).thenReturn("John Smith");
      when(securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY).getLocale()).thenReturn(new HLocale(LocaleId.DE));
      when(textFlowTargetDAO.getTextFlowTarget(1L, LocaleId.DE)).thenReturn(null);

      // When:
      handler.execute(action, null);
   }

   @Test(expectedExceptions = ActionException.class)
   public void testExecuteWhenTargetIsUntranslated() throws Exception
   {
      // Given: we want to add comment to trans unit id 1 and locale id DE but target is new
      String commentContent = "new comment";
      AddReviewCommentAction action = new AddReviewCommentAction(new TransUnitId(1L), commentContent, documentId);
      when(authenticatedAccount.getPerson()).thenReturn(hPerson);
      when(hPerson.getName()).thenReturn("John Smith");
      when(securityServiceImpl.checkPermission(action, SecurityService.TranslationAction.MODIFY).getLocale()).thenReturn(new HLocale(LocaleId.DE));
      when(textFlowTargetDAO.getTextFlowTarget(1L, LocaleId.DE)).thenReturn(hTextFlowTarget);
      when(hTextFlowTarget.getState()).thenReturn(ContentState.New);

      // When:
      handler.execute(action, null);
   }
}
