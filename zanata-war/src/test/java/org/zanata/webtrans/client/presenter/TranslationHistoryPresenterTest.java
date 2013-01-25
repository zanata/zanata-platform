package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;

import com.google.common.collect.Lists;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;

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
   private TransHistorySelectionModel selectionModel;
   @Mock
   private TransHistoryDataProvider dataProvider;

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

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TranslationHistoryPresenter(display, eventBus, dispatcher, messages, selectionModel, dataProvider);
      presenter.setCurrentValueHolder(targetContentsPresenter);
      
      verify(display).setDataProvider(dataProvider);
      verify(display).setSelectionModel(selectionModel);
      verify(display).addVersionSortHandler(sortHandlerCaptor.capture());

      when(dataProvider.getList()).thenReturn(Lists.<TransHistoryItem>newArrayList());
      doNothing().when(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
   }

   private static TransHistoryItem historyItem(String versionNum)
   {
      return new TransHistoryItem(versionNum, Lists.newArrayList("a"), ContentState.Approved, "admin", new Date());
   }

   @Test
   public void onSelectionChangeIfNotTwoSelectionWillDisableComparison()
   {
      // Given: no item is selected
      when(selectionModel.getSelectedSet()).thenReturn(Collections.<TransHistoryItem>emptySet());

      // When:
      presenter.onSelectionChange(selectionChangeEvent);

      // Then:
      verify(display).disableComparison();
   }

   @Test
   public void onSelectionChangeIfTwoAreSelectedWillEnableComparison()
   {
      // Given: two items are selected
      TransHistoryItem itemOne = historyItem("1");
      TransHistoryItem itemTwo = historyItem("2");
      // this is to get around arbitrary order in set (so that we can mock the method call)
      Iterator<TransHistoryItem> tempIterator = Lists.newArrayList(itemOne, itemTwo).iterator();
      Set<TransHistoryItem> mockHistoryItems = Mockito.mock(Set.class);
      when(selectionModel.getSelectedSet()).thenReturn(mockHistoryItems);
      when(mockHistoryItems.size()).thenReturn(2);
      when(mockHistoryItems.iterator()).thenReturn(tempIterator);
      when(messages.translationHistoryComparison("1", "2")).thenReturn("compare ver. 1 to 2");

      // When:
      presenter.onSelectionChange(selectionChangeEvent);

      // Then:
      verify(display).showDiff(itemOne, itemTwo, "compare ver. 1 to 2");
   }

   @Test
   public void willNotifyErrorAndHideTranslationHistoryOnFailure()
   {
      // Given:
      when(messages.translationHistory()).thenReturn("translation history");

      // When: request history for trans unit id 1
      presenter.showTranslationHistory(transUnitId);

      // Then:
      verify(dataProvider).setLoading(true);
      verify(display).setTitle("translation history");
      verify(selectionModel).clear();
      verify(display).resetView();
      verify(display).center();
      assertThat(actionCaptor.getValue().getTransUnitId(), Matchers.equalTo(transUnitId));

      // And on failure
      AsyncCallback<GetTranslationHistoryResult> result = resultCaptor.getValue();
      result.onFailure(new RuntimeException());

      verify(eventBus).fireEvent(isA(NotificationEvent.class));
      verify(display).hide();
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
      when(messages.latestVersion(latestVersion)).thenReturn("2 latest");

      // When: request history for trans unit id 1
      presenter.showTranslationHistory(transUnitId);

      // Then:on success
      verify(dataProvider).setLoading(true);
      verify(display).setTitle("translation history");
      verify(selectionModel).clear();
      verify(display).resetView();
      verify(display).center();
      AsyncCallback<GetTranslationHistoryResult> result = resultCaptor.getValue();
      result.onSuccess(createTranslationHistory(latest, historyItem));
      MatcherAssert.assertThat(dataProvider.getList(), Matchers.contains(latest, historyItem));
      verify(dataProvider).setLoading(false);
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
      when(messages.latestVersion(latestVersion)).thenReturn("2 latest");
      when(messages.unsaved()).thenReturn("unsaved");

      // When: request history for trans unit id 1
      presenter.showTranslationHistory(transUnitId);

      // Then: on success
      AsyncCallback<GetTranslationHistoryResult> result = resultCaptor.getValue();
      result.onSuccess(createTranslationHistory(latest, historyItem));
      MatcherAssert.assertThat(dataProvider.getList(), Matchers.hasSize(3));
      MatcherAssert.assertThat(dataProvider.getList().get(0).getVersionNum(), Matchers.equalTo("unsaved"));
   }

   private static GetTranslationHistoryResult createTranslationHistory(TransHistoryItem latest, TransHistoryItem... historyItems)
   {
      return new GetTranslationHistoryResult(Lists.newArrayList(historyItems), latest);
   }
}
