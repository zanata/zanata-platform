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

package org.zanata.webtrans.client.presenter;

import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.ReviewCommentEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.view.ReviewCommentDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsAction;
import org.zanata.webtrans.shared.rpc.GetReviewCommentsResult;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ReviewCommentPresenterTest
{
   private ReviewCommentPresenter presenter;
   @Mock
   private ReviewCommentDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispather;
   @Mock
   private ReviewCommentDataProvider dataProvider;
   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   private GetTransUnitActionContextHolder contextHolder;


   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

      presenter = new ReviewCommentPresenter(display, eventBus, dispather, dataProvider, contextHolder);

      verify(display).setDataProvider(dataProvider);
      verify(display).setListener(presenter);
   }
   @Test
   public void testDisplayCommentView() throws Exception
   {
      TransUnitId transUnitId = new TransUnitId(1L);
      ArgumentCaptor<GetReviewCommentsAction> actionCaptor = ArgumentCaptor.forClass(GetReviewCommentsAction.class);
      ArgumentCaptor<AsyncCallback> resultCaptor = ArgumentCaptor.forClass(AsyncCallback.class);

      presenter.onShowReviewComment(new ReviewCommentEvent(transUnitId));

      verify(dataProvider).setLoading(true);
      verify(dispather).execute(actionCaptor.capture(), resultCaptor.capture());
      assertThat(actionCaptor.getValue().getTransUnitId(), Matchers.equalTo(transUnitId));

      AsyncCallback<GetReviewCommentsResult> callback = resultCaptor.getValue();
      GetReviewCommentsResult result = new GetReviewCommentsResult(Lists.newArrayList(new ReviewComment()));
      callback.onSuccess(result);

      verify(dataProvider).setLoading(false);
      verify(dataProvider).setList(result.getComments());
   }

   @Test
   public void testAddComment() throws Exception
   {
      when(contextHolder.getContext().getDocument().getId()).thenReturn(new DocumentId(1L, "doc"));
      ArgumentCaptor<AddReviewCommentAction> actionCaptor = ArgumentCaptor.forClass(AddReviewCommentAction.class);
      ArgumentCaptor<AsyncCallback> resultCaptor = ArgumentCaptor.forClass(AsyncCallback.class);
      List<ReviewComment> mockList = mock(List.class);
      when(dataProvider.getList()).thenReturn(mockList);

      presenter.addComment("some comment");

      verify(dispather).execute(actionCaptor.capture(), resultCaptor.capture());
      assertThat(actionCaptor.getValue().getContent(), Matchers.equalTo("some comment"));

      AsyncCallback<AddReviewCommentResult> callback = resultCaptor.getValue();
      AddReviewCommentResult result = new AddReviewCommentResult(new ReviewComment());
      callback.onSuccess(result);

      verify(dataProvider).getList();
      verify(mockList).add(result.getComment());
      verify(display).clearInput();
   }
}
