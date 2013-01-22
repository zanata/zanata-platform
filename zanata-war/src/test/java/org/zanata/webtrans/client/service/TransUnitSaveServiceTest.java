package org.zanata.webtrans.client.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.presenter.TargetContentsPresenter;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.GoToRowLink;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransUnitSaveServiceTest
{
   private TransUnitSaveService service;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private Provider<UndoLink> undoProvider;
   @Mock
   private TargetContentsPresenter targetContentsPresenter;
   @Mock
   private TableEditorMessages messages;
   @Mock
   private NavigationService navigationService;
   @Mock
   private Provider<GoToRowLink> goToRowProvider;
   @Captor
   private ArgumentCaptor<UpdateTransUnit> actionCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<UpdateTransUnitResult>> resultCaptor;
   private static final TransUnitId TRANS_UNIT_ID = new TransUnitId(1);
   private static final int VER_NUM = 9;
   @Mock
   private UndoLink undoLink;
   @Mock
   private GoToRowLink goToLink;
   private SaveEventQueue queue;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      queue = new SaveEventQueue();
      service = new TransUnitSaveService(eventBus, dispatcher, undoProvider, targetContentsPresenter, messages, navigationService, goToRowProvider, queue);
      when(goToRowProvider.get()).thenReturn(goToLink);
   }

   private static TransUnitSaveEvent event(String newContent, ContentState status, TransUnitId transUnitId, int verNum, String oldContent)
   {
      return new TransUnitSaveEvent(Lists.newArrayList(newContent), status, transUnitId, verNum, Lists.newArrayList(oldContent));
   }

   @Test
   public void onSaveDoNothingIfStateHasNotChanged()
   {
      // Given: old state and content are equal to saving state and content
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.Approved, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);


      // When:
      service.onTransUnitSave(event("old content", ContentState.Approved, TRANS_UNIT_ID, VER_NUM, "old content"));

      // Then:
      verify(navigationService).getByIdOrNull(TRANS_UNIT_ID);
      verifyNoMoreInteractions(navigationService);
      verifyZeroInteractions(dispatcher, eventBus, targetContentsPresenter);
   }

   @Test
   public void willSaveIfSomethingHasChanged()
   {
      // Given:
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);

      // When: save as approved
      service.onTransUnitSave(event("new content", ContentState.Approved, TRANS_UNIT_ID, VER_NUM, "old content"));

      // Then:
      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());

      UpdateTransUnit updateTransUnit = actionCaptor.getValue();
      assertThat(updateTransUnit.getUpdateRequests(), hasSize(1));
      assertThat(updateTransUnit.getUpdateType(), equalTo(TransUnitUpdated.UpdateType.WebEditorSave));

      TransUnitUpdateRequest request = updateTransUnit.getUpdateRequests().get(0);
      assertThat(request.getTransUnitId(), equalTo(TRANS_UNIT_ID));
      assertThat(request.getNewContents(), Matchers.contains("new content"));
      assertThat(request.getBaseTranslationVersion(), equalTo(VER_NUM));
      assertThat(request.getNewContentState(), equalTo(ContentState.Approved));
   }

   @Test
   public void willSaveToQueueIfItsSavingSameRow()
   {
      // Given:
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);

      // When: save twice
      service.onTransUnitSave(event("new content", ContentState.Approved, TRANS_UNIT_ID, VER_NUM, "old content"));
      service.onTransUnitSave(event("newer content", ContentState.NeedReview, TRANS_UNIT_ID, VER_NUM, "new content"));

      // Then:
      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());

      UpdateTransUnit updateTransUnit = actionCaptor.getValue();
      assertThat(updateTransUnit.getUpdateRequests(), hasSize(1));
      assertThat(updateTransUnit.getUpdateType(), equalTo(TransUnitUpdated.UpdateType.WebEditorSave));

      TransUnitUpdateRequest request = updateTransUnit.getUpdateRequests().get(0);
      assertThat(request.getTransUnitId(), equalTo(TRANS_UNIT_ID));
      assertThat(request.getNewContents(), Matchers.contains("new content"));
      assertThat(request.getBaseTranslationVersion(), equalTo(VER_NUM));
      assertThat(request.getNewContentState(), equalTo(ContentState.Approved));

      assertThat(queue.hasPending(), Matchers.is(true));
   }

   @Test
   public void onRPCSuccessAndSaveReturnSuccess()
   {
      // Given:
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);

      // When: save as fuzzy
      service.onTransUnitSave(event("new content", ContentState.NeedReview, TRANS_UNIT_ID, VER_NUM, "old content"));

      // Then:
      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
      assertThat(actionCaptor.getValue().getUpdateType(), equalTo(TransUnitUpdated.UpdateType.WebEditorSaveFuzzy));

      // on save success
      // Given: result comes back with saving successful
      int rowIndex = 1;
      when(messages.notifyUpdateSaved(rowIndex, TRANS_UNIT_ID.toString())).thenReturn("saved row 1, id 1");
      when(navigationService.findRowIndexById(TRANS_UNIT_ID)).thenReturn(rowIndex);
      when(undoProvider.get()).thenReturn(undoLink);

      AsyncCallback<UpdateTransUnitResult> callback = resultCaptor.getValue();
      TransUnit updatedTU = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "new content");
      UpdateTransUnitResult result = result(true, updatedTU, ContentState.NeedReview);

      // When:
      callback.onSuccess(result);

      // Then:
      ArgumentCaptor<NotificationEvent> notificationEventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      NotificationEvent event = notificationEventCaptor.getValue();
      assertThat(event.getSeverity(), is(NotificationEvent.Severity.Info));
      assertThat(event.getMessage(), equalTo("saved row 1, id 1"));
      verify(undoLink).prepareUndoFor(result);
      verify(targetContentsPresenter).addUndoLink(rowIndex, undoLink);
      verify(navigationService).updateDataModel(updatedTU);
      verify(targetContentsPresenter).confirmSaved(updatedTU);
      verify(targetContentsPresenter).setFocus();
   }

   @Test
   public void onRPCSuccessAndThereIsPendingSave()
   {
      // Given:
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);

      // When: save twice and one will be pending
      service.onTransUnitSave(event("new content", ContentState.NeedReview, TRANS_UNIT_ID, VER_NUM, "old content"));
      service.onTransUnitSave(event("newer content", ContentState.NeedReview, TRANS_UNIT_ID, VER_NUM, "new content"));

      // Then: dispatcher will be call twice
      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());

      // on save success
      // Given: result comes back with saving successful
      int rowIndex = 1;
      when(messages.notifyUpdateSaved(rowIndex, TRANS_UNIT_ID.toString())).thenReturn("saved row 1, id 1");
      when(navigationService.findRowIndexById(TRANS_UNIT_ID)).thenReturn(rowIndex);
      when(undoProvider.get()).thenReturn(undoLink);

      AsyncCallback<UpdateTransUnitResult> callback = resultCaptor.getValue();
      TransUnit updatedTU = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "new content");
      UpdateTransUnitResult result = result(true, updatedTU, ContentState.NeedReview);

      // When:
      callback.onSuccess(result);
      verify(dispatcher, times(2)).execute(actionCaptor.capture(), resultCaptor.capture());

      // Then: we have 3 action here just because we verify dispatcher twice
      assertThat(actionCaptor.getAllValues(), Matchers.hasSize(3));
      UpdateTransUnit secondRequest = actionCaptor.getAllValues().get(2);
      assertThat(secondRequest.getUpdateRequests().get(0).getNewContents(), Matchers.contains("newer content"));
   }

   @Test
   public void onPRCSuccessButSaveUnsuccessfulInResult()
   {
      // Given:
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);

      // When: save as fuzzy
      service.onTransUnitSave(event("new content", ContentState.NeedReview, TRANS_UNIT_ID, VER_NUM, "old content"));

      // Then:
      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());

      // on save success
      // Given: result comes back but saving operation failed
      when(messages.notifyUpdateFailed("id " + TRANS_UNIT_ID)).thenReturn("update failed");

      AsyncCallback<UpdateTransUnitResult> callback = resultCaptor.getValue();
      TransUnit updatedTU = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "new content");
      UpdateTransUnitResult result = result(false, updatedTU, ContentState.NeedReview);

      // When:
      callback.onSuccess(result);

      // Then:
      verify(goToLink).prepare("", TRANS_UNIT_ID);
      ArgumentCaptor<NotificationEvent> notificationEventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
      verify(targetContentsPresenter).setEditingState(TRANS_UNIT_ID, TargetContentsDisplay.EditingState.UNSAVED);
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      NotificationEvent event = notificationEventCaptor.getValue();
      assertThat(event.getSeverity(), is(NotificationEvent.Severity.Error));
      assertThat(event.getMessage(), equalTo("update failed"));
      assertThat(event.getInlineLink(), Matchers.<InlineLink>sameInstance(goToLink));
   }

   @Test
   public void onPRCFailure()
   {
      // Given:
      TransUnit old = TestFixture.makeTransUnit(TRANS_UNIT_ID.getId(), ContentState.NeedReview, "old content");
      when(navigationService.getByIdOrNull(TRANS_UNIT_ID)).thenReturn(old);

      // When: save as fuzzy
      TransUnitSaveEvent saveEvent = event("new content", ContentState.NeedReview, TRANS_UNIT_ID, VER_NUM, "old content");
      service.onTransUnitSave(saveEvent);
      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
      // on rpc failure:

      // Then: will reset value back
      AsyncCallback<UpdateTransUnitResult> callback = resultCaptor.getValue();
      when(messages.notifyUpdateFailed("id " + TRANS_UNIT_ID)).thenReturn("update failed");
      callback.onFailure(new RuntimeException("doh"));
      verify(targetContentsPresenter).setEditingState(saveEvent.getTransUnitId(), TargetContentsDisplay.EditingState.UNSAVED);
      ArgumentCaptor<NotificationEvent> notificationEventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
      verify(targetContentsPresenter).setEditingState(TRANS_UNIT_ID, TargetContentsDisplay.EditingState.UNSAVED);
      verify(eventBus).fireEvent(notificationEventCaptor.capture());
      NotificationEvent event = notificationEventCaptor.getValue();
      assertThat(event.getSeverity(), is(NotificationEvent.Severity.Error));
      assertThat(event.getMessage(), equalTo("update failed"));
      assertThat(event.getInlineLink(), Matchers.<InlineLink>sameInstance(goToLink));
   }

   private static UpdateTransUnitResult result(boolean success, TransUnit transUnit, ContentState previousState)
   {
      return new UpdateTransUnitResult(new TransUnitUpdateInfo(success, true, new DocumentId(1, ""), transUnit, 9, VER_NUM, previousState));
   }
}
