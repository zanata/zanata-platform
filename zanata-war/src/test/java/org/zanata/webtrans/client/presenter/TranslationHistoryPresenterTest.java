package org.zanata.webtrans.client.presenter;

import java.util.Date;
import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ReviewCommentEvent;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.ReviewCommentId;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;
import com.google.common.collect.Lists;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TranslationHistoryPresenterTest 
{
   private TranslationHistoryPresenter presenter;
   @Mock
   private TranslationHistoryDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private WebTransMessages messages;
   @Mock
   private TargetContentsPresenter targetContentsPresenter;
   @Mock
   private SelectionChangeEvent selectionChangeEvent;
   @Captor
   private ArgumentCaptor<ColumnSortEvent.ListHandler<TransHistoryItem>> sortHandlerCaptor;
   @Captor
   private ArgumentCaptor<GetTranslationHistoryAction> actionCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<GetTranslationHistoryResult>> resultCaptor;
   private final TransUnitId transUnitId = new TransUnitId(1L);
   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   private GetTransUnitActionContextHolder contextHolder;
   @Mock
   private KeyShortcutPresenter keyShortcutPresenter;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TranslationHistoryPresenter(display, eventBus, dispatcher, messages, contextHolder, keyShortcutPresenter);
      presenter.setCurrentValueHolder(targetContentsPresenter);
      
      doNothing().when(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
      verify(eventBus).addHandler(ReviewCommentEvent.TYPE, presenter);
   }

   private static TransHistoryItem historyItem(String versionNum)
   {
      return new TransHistoryItem(versionNum, Lists.newArrayList("a"), ContentState.Approved, "admin", new Date());
   }

   @Test
   public void willNotifyErrorAndHideTranslationHistoryOnFailure()
   {
      // Given:
      when(messages.translationHistory()).thenReturn("translation history");

      // When: request history for trans unit id 1
      presenter.showTranslationHistory(transUnitId);

      // Then:
      verify(display).setTitle("translation history");
      verify(display).resetView();
      verify(display).center();
      assertThat(actionCaptor.getValue().getTransUnitId(), Matchers.equalTo(transUnitId));

      // And on failure
      AsyncCallback<GetTranslationHistoryResult> result = resultCaptor.getValue();
      result.onFailure(new RuntimeException());

      verify(eventBus).fireEvent(isA(NotificationEvent.class));
      verify(display).hide();
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, true);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Popup, false);
   }

   @Test
   public void willShowTranslationHistoryOnSuccess()
   {
      // Given: text flow has one history item and one latest translation
      when(messages.translationHistory()).thenReturn("translation history");
      TransHistoryItem historyItem = historyItem("1");
      String latestVersion = "2";
      TransHistoryItem latest = historyItem(latestVersion);
      // latest contents and current contents are equal
      when(targetContentsPresenter.getNewTargets()).thenReturn(Lists.newArrayList(latest.getContents()));
      when(messages.latest()).thenReturn("latest");

      // When: request history for trans unit id 1
      presenter.showTranslationHistory(transUnitId);

      // Then:on success
      verify(display).setTitle("translation history");
      verify(display).resetView();
      verify(display).center();
      AsyncCallback<GetTranslationHistoryResult> result = resultCaptor.getValue();
      result.onSuccess(createTranslationHistory(latest, historyItem));
      verify(display).setData(Lists.<ComparableByDate>newArrayList(latest, historyItem));
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, false);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Popup, true);
   }

   @Test
   public void willShowTranslationHistoryWithUnsavedValueOnSuccess()
   {
      // Given: text flow has one history item and one latest translation
      TransHistoryItem historyItem = historyItem("1");
      String latestVersion = "2";
      TransHistoryItem latest = historyItem(latestVersion);
      // latest contents and current contents are NOT equal
      when(targetContentsPresenter.getNewTargets()).thenReturn(Lists.newArrayList("b"));
      when(messages.latest()).thenReturn("latest");
      when(messages.unsaved()).thenReturn("unsaved");

      // When: request history for trans unit id 1
      presenter.showTranslationHistory(transUnitId);

      // Then: on success
      AsyncCallback<GetTranslationHistoryResult> result = resultCaptor.getValue();
      result.onSuccess(createTranslationHistory(latest, historyItem));

      ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
      verify(display).setData(listArgumentCaptor.capture());
      assertThat((List<ComparableByDate>) listArgumentCaptor.getValue(), Matchers.<ComparableByDate>hasSize(3));
   }

   private static GetTranslationHistoryResult createTranslationHistory(TransHistoryItem latest, TransHistoryItem... historyItems)
   {
      return new GetTranslationHistoryResult(Lists.newArrayList(historyItems), latest, Lists.<ReviewComment>newArrayList());
   }

   @Test
   public void canCopyIntoEditor()
   {
      List<String> contents = Lists.newArrayList("a");

      presenter.copyIntoEditor(contents);

      ArgumentCaptor<CopyDataToEditorEvent> eventCaptor = ArgumentCaptor.forClass(CopyDataToEditorEvent.class);
      verify(eventBus).fireEvent(eventCaptor.capture());

      assertThat(eventCaptor.getValue().getTargetResult(), Matchers.equalTo(contents));
   }

   @Test
   public void testAddComment() throws Exception
   {
      when(contextHolder.getContext().getDocument().getId()).thenReturn(new DocumentId(1L, "doc"));
      ArgumentCaptor<AddReviewCommentAction> actionCaptor = ArgumentCaptor.forClass(AddReviewCommentAction.class);
      ArgumentCaptor<AsyncCallback> resultCaptor = ArgumentCaptor.forClass(AsyncCallback.class);

      presenter.addComment("some comment");

      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
      assertThat(actionCaptor.getValue().getContent(), Matchers.equalTo("some comment"));

      AsyncCallback<AddReviewCommentResult> callback = resultCaptor.getValue();
      AddReviewCommentResult result = new AddReviewCommentResult(new ReviewComment());
      callback.onSuccess(result);

      verify(display).addCommentToList(result.getComment());
      verify(display).clearInput();
   }

   @Test
   public void canDisplayEntriesInOrder()
   {
      // no unsaved content
      when(targetContentsPresenter.getNewTargets()).thenReturn(Lists.newArrayList("a"));
      long now = new Date().getTime();
      // items in time order
      TransHistoryItem latest = new TransHistoryItem("5", Lists.newArrayList("a"), ContentState.Approved, "admin",
            new Date(now - 1000));
      TransHistoryItem item = new TransHistoryItem("4", Lists.newArrayList("a"), ContentState.Approved, "admin",
            new Date(now - 2000));
      ReviewComment comment = new ReviewComment(new ReviewCommentId(1L), "comment", "admin", new Date(now), 5);

      presenter.displayEntries(latest, Lists.newArrayList(item), Lists.newArrayList(comment));

      ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
      verify(display).setData(listArgumentCaptor.capture());
      List<ComparableByDate> result = (List<ComparableByDate>) listArgumentCaptor.getValue();
      assertThat(result, Matchers.<ComparableByDate>contains(comment, latest, item));
   }

   @Test
   public void onCompareClickedWhenThePairIsNotFull()
   {
      // the pair is empty initially
      presenter.compareClicked(historyItem("5"));

      verify(display).disableComparison();
   }

   @Test
   public void onCompareClickedWhichMakesTwoItems()
   {
      when(messages.translationHistoryComparison("5", "4")).thenReturn("comparison of 5 and 4");
      TransHistoryItem one = historyItem("5");
      presenter.compareClicked(one);
      verify(display).disableComparison();

      TransHistoryItem two = historyItem("4");
      presenter.compareClicked(two);
      verify(display).showDiff(one, two, "comparison of 5 and 4");

   }

}
