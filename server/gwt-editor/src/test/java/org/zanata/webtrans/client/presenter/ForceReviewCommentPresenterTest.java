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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.IssuePriority;
import org.zanata.webtrans.client.events.CommentBeforeSaveEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.view.ForceReviewCommentDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCriterionId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rest.dto.TransReviewCriteria;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.test.GWTTestData.userWorkspaceContext;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ForceReviewCommentPresenterTest {
    private ForceReviewCommentPresenter presenter;
    @Mock
    private ForceReviewCommentDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private CachingDispatchAsync dispatcher;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GetTransUnitActionContextHolder contextHolder;
    @Mock
    private CommentBeforeSaveEvent commentBeforeSaveEvent;
    @Mock
    private TransUnitSaveEvent saveEvent;
    @Mock
    private KeyShortcutPresenter keyShortcutPresenter;
    @Captor
    private ArgumentCaptor<KeyShortcut> shortcutCapture;
    @Captor
    private ArgumentCaptor<AsyncCallback<AddReviewCommentResult>> resultCaptor;
    @Mock
    private Provider<UserWorkspaceContext> userWorkspaceContext;
    private List<TransReviewCriteria>
            reviewCriteria = Lists.newArrayList(
                    new TransReviewCriteria(1L, IssuePriority.Critical,
                            "Bad grammar", false));

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(userWorkspaceContext.get()).thenReturn(userWorkspaceContext(
                reviewCriteria));
        presenter =
                new ForceReviewCommentPresenter(display, eventBus, dispatcher,
                        contextHolder, keyShortcutPresenter, userWorkspaceContext);

        verify(display).setListener(presenter);
        verify(eventBus).addHandler(CommentBeforeSaveEvent.TYPE, presenter);
        verify(keyShortcutPresenter).register(shortcutCapture.capture());

        KeyShortcut keyShortcut = shortcutCapture.getValue();
        assertThat(keyShortcut.getContext())
                .isEqualTo(ShortcutContext.RejectConfirmationPopup);
    }

    @Test
    public void testOnCommentBeforeSave() throws Exception {
        presenter.onCommentBeforeSave(commentBeforeSaveEvent);

        verify(display).center();
    }

    @Test
    public void testAddComment() throws Exception {
        when(commentBeforeSaveEvent.getSaveEvent()).thenReturn(saveEvent);
        presenter.onCommentBeforeSave(commentBeforeSaveEvent);

        when(contextHolder.getContext().getDocument().getId()).thenReturn(
                new DocumentId(1L, "doc"));
        ArgumentCaptor<AddReviewCommentAction> actionCaptor =
                ArgumentCaptor.forClass(AddReviewCommentAction.class);

        presenter.addComment("i hate this");

        verify(dispatcher).execute(actionCaptor.capture(),
                resultCaptor.capture());
        assertThat(actionCaptor.getValue().getContent())
                .isEqualTo("i hate this");

        AsyncCallback<AddReviewCommentResult> callback =
                resultCaptor.getValue();
        AddReviewCommentResult result =
                new AddReviewCommentResult(new ReviewComment());
        callback.onSuccess(result);

        verify(display).clearInput();
        verify(eventBus).fireEvent(saveEvent);
        verify(eventBus).fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
        verify(display).hide();
    }

    @Test
    public void noActionIfNoContentAndReviewCriteria() {
        presenter.addComment("");
        verifyZeroInteractions(dispatcher);
    }

    @Test
    public void testAddReviewCriteria() {
        when(commentBeforeSaveEvent.getSaveEvent()).thenReturn(saveEvent);
        ReviewCriterionId reviewCriterionId =
                new ReviewCriterionId(reviewCriteria.get(0).getId());
        presenter.selectReviewCriteria(
                reviewCriterionId);
        presenter.onCommentBeforeSave(commentBeforeSaveEvent);

        when(contextHolder.getContext().getDocument().getId()).thenReturn(
                new DocumentId(1L, "doc"));
        ArgumentCaptor<AddReviewCommentAction> actionCaptor =
                ArgumentCaptor.forClass(AddReviewCommentAction.class);

        presenter.addComment("");

        verify(dispatcher).execute(actionCaptor.capture(),
                resultCaptor.capture());
        assertThat(actionCaptor.getValue().getReviewId())
                .isEqualTo(reviewCriterionId);

        AsyncCallback<AddReviewCommentResult> callback =
                resultCaptor.getValue();
        AddReviewCommentResult result =
                new AddReviewCommentResult(new ReviewComment());
        callback.onSuccess(result);

        verify(display).clearInput();
        verify(eventBus).fireEvent(saveEvent);
        verify(eventBus).fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
        verify(display).hide();
    }
}
