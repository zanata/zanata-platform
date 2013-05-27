/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeOption;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.model.TestFixture.makeTransUnit;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransMemoryMergePresenterTest
{
   private TransMemoryMergePresenter presenter;
   @Mock
   private TransMemoryMergePopupPanelDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private UiMessages messages;
   @Mock
   private Provider<UndoLink> undoLinkProvider;
   @Captor
   private ArgumentCaptor<NotificationEvent> notificationEventCaptor;
   @Captor
   private ArgumentCaptor<TransMemoryMerge> transMemoryMergeCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<UpdateTransUnitResult>> callbackCaptor;
   @Mock
   private NavigationService navigationService;
   @Mock
   private WebTransMessages webTranMessages;

   @BeforeMethod
   public void setUp()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransMemoryMergePresenter(display, eventBus, dispatcher, navigationService, messages, undoLinkProvider);

      verify(display).setListener(presenter);
   }

   @Test
   public void willShowFormOnPrepareTMMerge()
   {
      presenter.prepareTMMerge();

      verify(display).showForm();
   }

   @Test
   public void willHideFormOnCancel()
   {
      presenter.cancelMergeTM();

      verify(display).hide();
   }

   @Test
   public void willIgnoreTranslatedTextFlow() {
      // Given:
      // current table page has a list of trans units but all of them are approved
      // @formatter:off
      List<TransUnit> currentPageRows = ImmutableList.<TransUnit>builder()
            .add(makeTransUnit(2, ContentState.Approved))
            .add(makeTransUnit(3, ContentState.Translated))
            .add(makeTransUnit(6, ContentState.Approved))
            .build();
      // @formatter:on
      mockCurrentPageToReturn(currentPageRows);

      // When:
      presenter.proceedToMergeTM(80, MergeOption.IGNORE_CHECK, MergeOption.REJECT, MergeOption.FUZZY);

      // Then:
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      verify(messages).noTranslationToMerge();
      verify(display).hide();
      verifyZeroInteractions(dispatcher, undoLinkProvider);
   }

   private void mockCurrentPageToReturn(List<TransUnit> allRowValues)
   {
      when(navigationService.getCurrentPageValues()).thenReturn(allRowValues);
   }

   @Test
   public void canRequestTMMerge()
   {
      // Given:
      // current table page has a list of trans units and 3 of them are in NEW status
      // @formatter:off
      List<TransUnit> currentPageRows = ImmutableList.<TransUnit>builder()
            .add(makeTransUnit(1, ContentState.New))
            .add(makeTransUnit(2, ContentState.Approved))
            .add(makeTransUnit(3, ContentState.NeedReview))
            .add(makeTransUnit(4, ContentState.New))
            .add(makeTransUnit(5, ContentState.New))
            .add(makeTransUnit(6, ContentState.NeedReview))
            .build();
      // @formatter:on
      mockCurrentPageToReturn(currentPageRows);

      // When:
      presenter.proceedToMergeTM(80, MergeOption.IGNORE_CHECK, MergeOption.REJECT, MergeOption.FUZZY);

      // Then:
      InOrder inOrder = inOrder(display, dispatcher);
      inOrder.verify(display).showProcessing();
      inOrder.verify(dispatcher).execute(transMemoryMergeCaptor.capture(), callbackCaptor.capture());

      TransMemoryMerge action = transMemoryMergeCaptor.getValue();
      List<TransUnitUpdateRequest> updateRequests = action.getUpdateRequests();
      assertThat(updateRequests, Matchers.hasSize(5));
      assertThat(getIds(updateRequests), Matchers.contains(1L, 3L, 4L, 5L, 6L));
      assertThat(action.getDifferentProjectOption(), Matchers.equalTo(MergeOption.IGNORE_CHECK));
      assertThat(action.getDifferentDocumentOption(), Matchers.equalTo(MergeOption.REJECT));
      assertThat(action.getDifferentContextOption(), Matchers.equalTo(MergeOption.FUZZY));
   }

   @Test
   public void onRequestTMMergeFailureWillHideFormAndNotify() {
      // Given:
      // there is untranslated text flow on page
      List<TransUnit> currentPageRows = Lists.newArrayList(makeTransUnit(1, ContentState.New));
      mockCurrentPageToReturn(currentPageRows);

      // When:
      presenter.proceedToMergeTM(100, MergeOption.REJECT, MergeOption.IGNORE_CHECK, MergeOption.FUZZY);
      verify(dispatcher).execute(transMemoryMergeCaptor.capture(), callbackCaptor.capture());
      AsyncCallback<UpdateTransUnitResult> callback = callbackCaptor.getValue();
      // rpc call failed
      callback.onFailure(new RuntimeException("Die!!!!!"));

      // Then:
      verify(messages).mergeTMFailed();
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      verify(display).hide();
      NotificationEvent event = notificationEventCaptor.getValue();
      assertThat(event.getSeverity(), Matchers.equalTo(NotificationEvent.Severity.Error));
      assertThat(event.getMessage(), Matchers.sameInstance(messages.mergeTMFailed()));
   }

   @Test
   public void onRequestTMMergeSuccessWithTranslationWillCreateUndoLinkAndNotify() {
      // Given:
      // there is untranslated text flow on page
      List<TransUnit> currentPageRows = Lists.newArrayList(
            makeTransUnit(1, ContentState.New),
            makeTransUnit(2, ContentState.NeedReview),
            makeTransUnit(3, ContentState.New),
            makeTransUnit(4, ContentState.NeedReview));
      mockCurrentPageToReturn(currentPageRows);
      UndoLink undoLink = mock(UndoLink.class);
      when(undoLinkProvider.get()).thenReturn(undoLink);

      // When:
      presenter.proceedToMergeTM(100, MergeOption.REJECT, MergeOption.IGNORE_CHECK, MergeOption.FUZZY);
      verify(dispatcher).execute(transMemoryMergeCaptor.capture(), callbackCaptor.capture());
      AsyncCallback<UpdateTransUnitResult> callback = callbackCaptor.getValue();
      // rpc call success and result has some updated info
      UpdateTransUnitResult result = new UpdateTransUnitResult();
      result.addUpdateResult(new TransUnitUpdateInfo(true, true, null, currentPageRows.get(0), 0, 0, ContentState.Approved));
      // add an unsuccessful result
      result.addUpdateResult(new TransUnitUpdateInfo(false, true, null, currentPageRows.get(1), 0, 0, ContentState.Approved));
      result.addUpdateResult(new TransUnitUpdateInfo(true, true, null, currentPageRows.get(2), 0, 0, ContentState.Approved));
      result.addUpdateResult(new TransUnitUpdateInfo(true, true, null, currentPageRows.get(3), 0, 0, ContentState.Approved));
      callback.onSuccess(result);

      // Then:
      verify(messages).mergeTMSuccess(Lists.newArrayList("1", "3", "4"));
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      verify(display).hide();
      NotificationEvent event = notificationEventCaptor.getValue();
      assertThat(event.getSeverity(), Matchers.equalTo(NotificationEvent.Severity.Info));
      assertThat(event.getMessage(), Matchers.sameInstance(messages.mergeTMSuccess(Lists.newArrayList("1", "3", "4"))));
      assertThat(event.getInlineLink(), Matchers.<InlineLink>sameInstance(undoLink));
      verify(undoLink).prepareUndoFor(result);
   }

   @Test
   public void onRequestTMMergeSuccessWithoutTranslationWillJustNotify() {
      // Given:
      // there is untranslated text flow on page
      List<TransUnit> currentPageRows = Lists.newArrayList(makeTransUnit(1, ContentState.New));
      mockCurrentPageToReturn(currentPageRows);
      UndoLink undoLink = mock(UndoLink.class);
      when(undoLinkProvider.get()).thenReturn(undoLink);

      // When:
      presenter.proceedToMergeTM(100, MergeOption.REJECT, MergeOption.IGNORE_CHECK, MergeOption.FUZZY);
      verify(dispatcher).execute(transMemoryMergeCaptor.capture(), callbackCaptor.capture());
      AsyncCallback<UpdateTransUnitResult> callback = callbackCaptor.getValue();
      // rpc call success but result has no updated info
      UpdateTransUnitResult result = new UpdateTransUnitResult();
      callback.onSuccess(result);

      // Then:
      verify(messages).noTranslationToMerge();
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      verify(display).hide();
      NotificationEvent event = notificationEventCaptor.getValue();
      assertThat(event.getSeverity(), Matchers.equalTo(NotificationEvent.Severity.Info));
      assertThat(event.getMessage(), Matchers.sameInstance(messages.noTranslationToMerge()));
      assertThat(event.getInlineLink(), Matchers.nullValue());
   }

   private static List<Long> getIds(List<TransUnitUpdateRequest> updateRequests)
   {
      return Lists.transform(updateRequests, new Function<TransUnitUpdateRequest, Long>()
      {
         @Override
         public Long apply(TransUnitUpdateRequest from)
         {
            return from.getTransUnitId().getId();
         }
      });
   }
}
