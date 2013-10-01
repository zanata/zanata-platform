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

import org.hamcrest.Matchers;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
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

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        presenter =
                new ForceReviewCommentPresenter(display, eventBus, dispatcher,
                        contextHolder, keyShortcutPresenter);

        verify(display).setListener(presenter);
        verify(eventBus).addHandler(CommentBeforeSaveEvent.TYPE, presenter);
        verify(keyShortcutPresenter).register(shortcutCapture.capture());

        KeyShortcut keyShortcut = shortcutCapture.getValue();
        assertThat(keyShortcut.getContext(),
                Matchers.equalTo(ShortcutContext.RejectConfirmationPopup));
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
        ArgumentCaptor<AsyncCallback> resultCaptor =
                ArgumentCaptor.forClass(AsyncCallback.class);

        presenter.addComment("i hate this");

        verify(dispatcher).execute(actionCaptor.capture(),
                resultCaptor.capture());
        assertThat(actionCaptor.getValue().getContent(),
                Matchers.equalTo("i hate this"));

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
