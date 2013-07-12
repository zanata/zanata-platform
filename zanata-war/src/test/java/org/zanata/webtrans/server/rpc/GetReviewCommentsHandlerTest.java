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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowTargetReviewCommentsDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsAction;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsResult;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class GetReviewCommentsHandlerTest
{
   private GetReviewCommentsHandler handler;
   @Mock
   private TextFlowTargetReviewCommentsDAO dao;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);

      handler = SeamAutowire.instance().reset().use("textFlowTargetReviewCommentsDAO", dao).autowire(GetReviewCommentsHandler.class);
   }

   @Test
   public void testExecute() throws Exception
   {
      GetReviewCommentsAction action = new GetReviewCommentsAction(new TransUnitId(1L));
      action.setWorkspaceId(TestFixture.workspaceId());
      LocaleId localeId = action.getWorkspaceId().getLocaleId();
      when(dao.getReviewComments(action.getTransUnitId(), localeId)).thenReturn(Lists.newArrayList(makeCommentEntity(localeId, "a comment"), makeCommentEntity(localeId, "another comment")));

      GetReviewCommentsResult result = handler.execute(action, null);

      assertThat(result.getComments(), Matchers.hasSize(2));
      assertThat(result.getComments().get(0).getComment(), Matchers.equalTo("a comment"));
      assertThat(result.getComments().get(1).getComment(), Matchers.equalTo("another comment"));

   }

   private static HTextFlowTargetReviewComment makeCommentEntity(LocaleId localeId, String comment)
   {
      HLocale hLocale = new HLocale(localeId);
      TestFixture.setId(2L, hLocale);

      HTextFlow textFlow = TestFixture.makeHTextFlow(1L, hLocale, ContentState.Rejected);

      HPerson commenter = new HPerson();
      TestFixture.setId(3L, commenter);

      return new HTextFlowTargetReviewComment(textFlow.getTargets().get(hLocale.getId()), comment, commenter);
   }
}
