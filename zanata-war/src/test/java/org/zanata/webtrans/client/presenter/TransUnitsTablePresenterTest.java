package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.service.TranslatorInteractionService;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.GoToRowLink;
import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.client.view.TransUnitsTableDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransUnitsTablePresenterTest
{
   private TransUnitsTablePresenter presenter;
   @Mock
   private TransUnitsTableDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private NavigationService navigationService;
   @Mock
   private SourceContentsPresenter sourceContentsPresenter;
   @Mock
   private TargetContentsPresenter targetContentsPresenter;
   @Mock
   private TranslatorInteractionService translatorService;
   @Mock
   private WebTransMessages messages;
   @Mock
   private TranslationHistoryPresenter translationHistoryPresenter;
   @Mock
   private UserOptionsService userOptionsService;


   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransUnitsTablePresenter(display, eventBus, navigationService, sourceContentsPresenter, targetContentsPresenter, translatorService, translationHistoryPresenter, messages, userOptionsService);

      verify(display).setRowSelectionListener(presenter);
      verify(display).addFilterConfirmationHandler(presenter);
      verify(navigationService).addPageDataChangeListener(presenter);
   }

   @Test
   public void onBind()
   {
      when(userOptionsService.getConfigHolder()).thenReturn(new UserConfigHolder());
      presenter.onBind();

      verify(eventBus).addHandler(FilterViewEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitSelectionEvent.getType(), presenter);
      verify(eventBus).addHandler(TableRowSelectedEvent.TYPE, presenter);
      verify(eventBus).addHandler(LoadingEvent.TYPE, presenter);
   }

   @Test
   public void onTransUnitSelected()
   {
      // Given:
      TransUnit selection = TestFixture.makeTransUnit(1);
      TransUnitSelectionEvent transUnitSelectionEvent = new TransUnitSelectionEvent(selection);

      // When:
      presenter.onTransUnitSelected(transUnitSelectionEvent);

      // Then:
      verify(sourceContentsPresenter).setSelectedSource(selection.getId());
      verify(targetContentsPresenter).setSelected(selection.getId());
      verify(translatorService).transUnitSelected(selection);
      verify(display).ensureVisible(targetContentsPresenter.getCurrentDisplay());
   }

   @Test
   public void onGoToPage()
   {
      presenter.goToPage(2);

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(navigationService).gotoPage(1);
   }

   @Test
   public void onFilterViewEventDoNothingIfItsCancel()
   {
      presenter.onFilterView(new FilterViewEvent(true, true, true, true, true, false, true, null));

      verifyNoMoreInteractions(eventBus, display, targetContentsPresenter);
   }

   @Test
   public void onFilterViewEventWillShowConfirmationIfHasUnsavedContent()
   {
      // Given: current editor content has changed
      when(targetContentsPresenter.currentEditorContentHasChanged()).thenReturn(true);

      // When: not a cancel event
      presenter.onFilterView(new FilterViewEvent(true, false, true, false, false, false, false, null));

      // Then:
      verify(display).showFilterConfirmation();
   }

   @Test
   public void onFilterViewEventWillHideConfirmationAndDoFilter()
   {
      // Given: current edtior hasn't changed
      when(targetContentsPresenter.currentEditorContentHasChanged()).thenReturn(false);
      FilterViewEvent event = new FilterViewEvent(true, false, true, false, false, false, false, null);

      // When:
      presenter.onFilterView(event);

      // Then:
      verify(display).hideFilterConfirmation();
      verify(navigationService).execute(event);
   }

   @Test
   public void onSaveChangeAndFilter()
   {
      when(targetContentsPresenter.getCurrentTransUnitIdOrNull()).thenReturn(new TransUnitId(1));

      presenter.saveAsTranslatedAndFilter();

      verify(targetContentsPresenter).saveCurrent(ContentState.Translated);
      verify(display).hideFilterConfirmation();
      verify(navigationService).execute(Mockito.isA(FilterViewEvent.class));
   }

   @Test
   public void onSaveAsFuzzyAndFilter()
   {
      when(targetContentsPresenter.getCurrentTransUnitIdOrNull()).thenReturn(new TransUnitId(1));

      presenter.saveAsFuzzyAndFilter();

      verify(targetContentsPresenter).saveCurrent(ContentState.NeedReview);
      verify(display).hideFilterConfirmation();
      verify(navigationService).execute(Mockito.isA(FilterViewEvent.class));
   }

   @Test
   public void onDiscardChangeAndFilter()
   {
      TransUnitId currentId = new TransUnitId(1);
      when(targetContentsPresenter.getCurrentTransUnitIdOrNull()).thenReturn(currentId);

      presenter.discardChangesAndFilter();

      verify(targetContentsPresenter).onCancel(currentId);
      verify(display).hideFilterConfirmation();
      verify(navigationService).execute(Mockito.isA(FilterViewEvent.class));
   }

   @Test
   public void onCancelFilter()
   {
      presenter.cancelFilter();

      ArgumentCaptor<FilterViewEvent> filterViewEventCaptor = ArgumentCaptor.forClass(FilterViewEvent.class);
      verify(eventBus).fireEvent(filterViewEventCaptor.capture());
      assertThat(filterViewEventCaptor.getValue().isCancelFilter(), Matchers.equalTo(true));
      verify(display).hideFilterConfirmation();
   }

   @Test
   public void canShowDataOnCurrentPage()
   {
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1));
      List<TargetContentsDisplay> targetContentsDisplays = Lists.newArrayList();
      List<SourceContentsDisplay> sourceContentsDisplays = Lists.newArrayList();
      when(targetContentsPresenter.getDisplays()).thenReturn(targetContentsDisplays);
      when(sourceContentsPresenter.getDisplays()).thenReturn(sourceContentsDisplays);

      presenter.showDataForCurrentPage(transUnits);

      verify(sourceContentsPresenter).showData(transUnits);
      verify(targetContentsPresenter).showData(transUnits);
      verify(display).buildTable(sourceContentsDisplays, targetContentsDisplays);
   }

   @Test
   public void canRefreshRowIfNotOnCurrentSelection()
   {
      // Given: coming updated ID is NOT equal to current selected id and is from another user
      EditorClientId editorClientId = new EditorClientId("session", 1);
      when(translatorService.getCurrentEditorClientId()).thenReturn(editorClientId);
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(1);
      presenter.setStateForTesting(new TransUnitId(99));

      // When: update type is save and done by different user
      presenter.refreshRow(updatedTransUnit, new EditorClientId("session", 2), TransUnitUpdated.UpdateType.WebEditorSave);

      // Then:
      verify(targetContentsPresenter).updateRow(updatedTransUnit);
      verifyZeroInteractions(eventBus);
      verifyNoMoreInteractions(targetContentsPresenter);
   }

   @Test
   public void refreshRowFromCurrentUserWillGetIgnored()
   {
      // Given: coming client id is the same as current user
      EditorClientId editorClientId = new EditorClientId("session", 1);
      when(translatorService.getCurrentEditorClientId()).thenReturn(editorClientId);
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(1);
      presenter.setStateForTesting(updatedTransUnit.getId());

      // When: refreshRow from same user
      presenter.refreshRow(updatedTransUnit, editorClientId, TransUnitUpdated.UpdateType.WebEditorSaveFuzzy);

      // Then:
      verifyZeroInteractions(eventBus, targetContentsPresenter);
   }

   @Test
   public void willRefreshRowFromCurrentUserNotAsEditorSave()
   {
      // Given: coming client id is the same as current user
      EditorClientId editorClientId = new EditorClientId("session", 1);
      when(translatorService.getCurrentEditorClientId()).thenReturn(editorClientId);
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(1);
      presenter.setStateForTesting(updatedTransUnit.getId());

      // When: refreshRow from same user but update type is replace
      presenter.refreshRow(updatedTransUnit, editorClientId, TransUnitUpdated.UpdateType.ReplaceText);

      // Then:
      verify(targetContentsPresenter).updateRow(updatedTransUnit);
      verifyZeroInteractions(eventBus);
      verifyNoMoreInteractions(targetContentsPresenter);
   }

   @Test
   public void willDetectSaveDoneByAnotherUserAndCurrentUserDoNotHaveUnsavedChange()
   {
      // Given: coming client id is NOT current user
      EditorClientId currentUser = new EditorClientId("session1", 1);
      when(translatorService.getCurrentEditorClientId()).thenReturn(currentUser);
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(1);
      presenter.setStateForTesting(updatedTransUnit.getId());
      when(messages.concurrentEdit()).thenReturn("concurrent edit detected");
      // current user does not have unsaved change
      when(targetContentsPresenter.currentEditorContentHasChanged()).thenReturn(false);

      // When: update type is save fuzzy
      presenter.refreshRow(updatedTransUnit, new EditorClientId("session2", 2), TransUnitUpdated.UpdateType.WebEditorSaveFuzzy);

      // Then:
      ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getMessage(), Matchers.equalTo("concurrent edit detected"));
      verify(targetContentsPresenter).currentEditorContentHasChanged();
      verify(targetContentsPresenter).updateRow(updatedTransUnit);
      verifyNoMoreInteractions(targetContentsPresenter);
      verifyZeroInteractions(translationHistoryPresenter);
   }

   @Test
   public void willDetectSaveDoneByAnotherUserAndCurrentUserHasUnsavedChange()
   {
      // Given: coming client id is NOT current user
      EditorClientId currentUser = new EditorClientId("session1", 1);
      when(translatorService.getCurrentEditorClientId()).thenReturn(currentUser);
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(1);
      presenter.setStateForTesting(updatedTransUnit.getId());
      when(messages.concurrentEdit()).thenReturn("concurrent edit detected");
      when(messages.concurrentEditTitle()).thenReturn("please resolve conflict");
      // current user does not have unsaved change
      when(targetContentsPresenter.currentEditorContentHasChanged()).thenReturn(true);

      // When: update type is save fuzzy
      presenter.refreshRow(updatedTransUnit, new EditorClientId("session2", 2), TransUnitUpdated.UpdateType.WebEditorSaveFuzzy);

      // Then:
      ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getMessage(), Matchers.equalTo("concurrent edit detected"));

      ArgumentCaptor<TransHistoryItem> transHistoryCaptor = ArgumentCaptor.forClass(TransHistoryItem.class);
      InOrder inOrder = Mockito.inOrder(targetContentsPresenter, translationHistoryPresenter);
      inOrder.verify(translationHistoryPresenter).popupAndShowLoading("please resolve conflict");
      inOrder.verify(translationHistoryPresenter).displayEntries(transHistoryCaptor.capture(), eq(Collections.<TransHistoryItem>emptyList()));
      assertThat(transHistoryCaptor.getValue().getVersionNum(), Matchers.equalTo(updatedTransUnit.getVerNum().toString()));
      assertThat(transHistoryCaptor.getValue().getContents(), Matchers.equalTo(updatedTransUnit.getTargets()));
      inOrder.verify(targetContentsPresenter).updateRow(updatedTransUnit);
   }

   @Test
   public void canHighlightSearch()
   {
      presenter.highlightSearch("blah");

      verify(targetContentsPresenter).highlightSearch("blah");
      verify(sourceContentsPresenter).highlightSearch("blah");
   }

   @Test
   public void onRowSelected()
   {
      // Given: current row index is 1
      when(navigationService.getCurrentRowIndexOnPage()).thenReturn(1);

      // When: select row index 2
      presenter.onRowSelected(2);

      // Then:
      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(navigationService).selectByRowIndex(2);
      verify(display).applySelectedStyle(2);

   }

   @Test
   public void willDoNothingIfSelectSameRow()
   {
      // Given: current row index is 1
      when(navigationService.getCurrentRowIndexOnPage()).thenReturn(1);

      // When: select row index is also 1
      presenter.onRowSelected(1);

      // Then:
      verify(navigationService).getCurrentRowIndexOnPage();
      verifyNoMoreInteractions(navigationService);
      verifyZeroInteractions(targetContentsPresenter, display);
   }

   @Test
   public void onTableRowSelectingSameRow()
   {
      // Given: selecting id is on row index 2, and current selected row index is also 2
      TransUnitId selectingId = new TransUnitId(1);
      when(navigationService.findRowIndexById(selectingId)).thenReturn(2);
      when(navigationService.getCurrentRowIndexOnPage()).thenReturn(2);

      // When:
      presenter.onTableRowSelected(new TableRowSelectedEvent(selectingId));

      // Then:
      verify(navigationService).getCurrentRowIndexOnPage();
      verifyZeroInteractions(targetContentsPresenter);
   }

   @Test
   public void onTableRowSelectingDifferentRow()
   {
      // Given: selecting id is on row index 2, and current selected row index is 3
      TransUnitId selectingId = new TransUnitId(1);
      when(navigationService.findRowIndexById(selectingId)).thenReturn(2);
      when(navigationService.getCurrentRowIndexOnPage()).thenReturn(3);

      // When:
      presenter.onTableRowSelected(new TableRowSelectedEvent(selectingId));

      // Then:
      verify(navigationService, times(2)).getCurrentRowIndexOnPage();
      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(navigationService).selectByRowIndex(2);
      verify(display).applySelectedStyle(2);
   }

   @Test
   public void onTableRowSelectingDifferentRowAndSuppressSavePending()
   {
      // Given: selecting id is on row index 2, and current selected row index is 3
      TransUnitId selectingId = new TransUnitId(1);
      when(navigationService.findRowIndexById(selectingId)).thenReturn(2);
      when(navigationService.getCurrentRowIndexOnPage()).thenReturn(3);

      // When:
      presenter.onTableRowSelected(new TableRowSelectedEvent(selectingId).setSuppressSavePending(true));

      // Then:
      verify(navigationService, times(2)).getCurrentRowIndexOnPage();
      verify(targetContentsPresenter, never()).savePendingChangesIfApplicable();
      verify(navigationService).selectByRowIndex(2);
      verify(display).applySelectedStyle(2);
   }

   @Test
   public void onLoadingEvent()
   {
      presenter.onLoading(LoadingEvent.START_EVENT);
      verify(display).showLoading(true);

      presenter.onLoading(LoadingEvent.FINISH_EVENT);
      verify(display).showLoading(false);
   }

   @Test
   public void canRefreshViewWithSearch()
   {
      // Given: presenter has highlight search term
      presenter.highlightSearch("blah");
      TargetContentsDisplay targetDisplay = mock(TargetContentsDisplay.class);
      SourceContentsDisplay sourceDisplay = mock(SourceContentsDisplay.class);
      // assuming two displays in the list
      List<TargetContentsDisplay> targetContentsDisplays = Lists.newArrayList(targetDisplay, targetDisplay);
      List<SourceContentsDisplay> sourceContentsDisplays = Lists.newArrayList(sourceDisplay, sourceDisplay);
      when(targetContentsPresenter.getDisplays()).thenReturn(targetContentsDisplays);
      when(sourceContentsPresenter.getDisplays()).thenReturn(sourceContentsDisplays);

      // When:
      presenter.refreshView();

      // Then:
      verify(sourceDisplay, times(2)).refresh();
      verify(targetDisplay, times(2)).refresh();
      verify(sourceDisplay, times(2)).highlightSearch("blah");
      verify(targetDisplay, times(2)).highlightSearch("blah");
   }

   @Test
   public void canRefreshViewWithNoSearch()
   {
      // Given: presenter has no highlight search term
      TargetContentsDisplay targetDisplay = mock(TargetContentsDisplay.class);
      SourceContentsDisplay sourceDisplay = mock(SourceContentsDisplay.class);
      // assuming two displays in the list
      List<TargetContentsDisplay> targetContentsDisplays = Lists.newArrayList(targetDisplay, targetDisplay);
      List<SourceContentsDisplay> sourceContentsDisplays = Lists.newArrayList(sourceDisplay, sourceDisplay);
      when(targetContentsPresenter.getDisplays()).thenReturn(targetContentsDisplays);
      when(sourceContentsPresenter.getDisplays()).thenReturn(sourceContentsDisplays);

      // When:
      presenter.refreshView();

      // Then:
      verify(sourceDisplay, times(2)).refresh();
      verify(targetDisplay, times(2)).refresh();
      verify(sourceDisplay, never()).highlightSearch(anyString());
      verify(targetDisplay, never()).highlightSearch(anyString());
   }

   @Test
   public void onCodeMirrorRefreshPageEvent()
   {
      presenter.onRefreshPage(RefreshPageEvent.REFRESH_CODEMIRROR_EVENT);

      verify(display).delayRefresh();
      verifyNoMoreInteractions(display);
      verifyZeroInteractions(targetContentsPresenter);
   }

   @Test
   public void onRedrawPageEventWithSelectedTransUnit()
   {
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1));
      when(navigationService.getCurrentPageValues()).thenReturn(transUnits);
      TransUnitId selectedId = transUnits.get(0).getId();
      when(sourceContentsPresenter.getCurrentTransUnitIdOrNull()).thenReturn(selectedId);

      presenter.onRefreshPage(RefreshPageEvent.REDRAW_PAGE_EVENT);

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(targetContentsPresenter).showData(transUnits);
      verify(sourceContentsPresenter).setSelectedSource(selectedId);
      verify(targetContentsPresenter).setSelected(selectedId);
      verify(display).buildTable(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
   }

   @Test
   public void onRedrawPageEventWithoutSelectedTransUnit()
   {
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1));
      when(navigationService.getCurrentPageValues()).thenReturn(transUnits);
      when(sourceContentsPresenter.getCurrentTransUnitIdOrNull()).thenReturn(null);

      presenter.onRefreshPage(RefreshPageEvent.REDRAW_PAGE_EVENT);

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(targetContentsPresenter).showData(transUnits);
      verify(display).buildTable(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
      verify(targetContentsPresenter, never()).setSelected(any(TransUnitId.class));
   }
   
   @Test
   public void onUserConfigChanged()
   {
      when(userOptionsService.getConfigHolder()).thenReturn(new UserConfigHolder());

      UserConfigChangeEvent mockEvent = mock(UserConfigChangeEvent.class);
      
      presenter.onUserConfigChanged(mockEvent);

      verify(display).setThemes(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());
   }
}
