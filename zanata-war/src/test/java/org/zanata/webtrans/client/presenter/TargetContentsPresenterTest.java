/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.Arrays;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;
import org.zanata.webtrans.client.ui.SaveAsApprovedConfirmationDisplay;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.model.TestFixture.makeTransUnit;

@Test(groups = { "unit-tests" })
public class TargetContentsPresenterTest
{
   private TargetContentsPresenter presenter;

   private static final List<String> NEW_TARGETS = ImmutableList.<String>builder().add("a").build();
   private static final List<String> CACHED_TARGETS = ImmutableList.<String>builder().add("b").build();
   // @formatter:off
   List<TransUnit> currentPageRows = ImmutableList.<TransUnit>builder()
         .add(makeTransUnit(2))
         .add(makeTransUnit(3))
         .add(makeTransUnit(6))
         .build();
   // @formatter:on

   @Mock private EventBus eventBus;
   @Mock private TableEditorMessages tableEditorMessages;
   @Mock private SourceContentsPresenter sourceContentPresenter;
   private UserWorkspaceContext userWorkspaceContext;
   @Mock private TargetContentsDisplay display;
   @Mock
   private ToggleEditor editor, editor2;
   private TransUnit selectedTU;

   // all event extends GwtEvent therefore captor will capture them all
   @Captor private ArgumentCaptor<GwtEvent> eventCaptor;

   @Mock
   private Provider<TargetContentsDisplay> displayProvider;

   @Mock
   private TranslationHistoryPresenter historyPresenter;
   private UserConfigHolder configHolder;
   @Mock
   private EditorTranslators editorTranslators;
   @Mock
   private EditorKeyShortcuts editorKeyShortcuts;
   @Mock
   private UserOptionsService userOptionsService;
   @Mock
   private SaveAsApprovedConfirmationDisplay saveAsApprovedConfirmation;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      configHolder = new UserConfigHolder();

      when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

      userWorkspaceContext = TestFixture.userWorkspaceContext();
      presenter = new TargetContentsPresenter(displayProvider, editorTranslators, eventBus, tableEditorMessages, sourceContentPresenter, userWorkspaceContext, editorKeyShortcuts, historyPresenter, userOptionsService, saveAsApprovedConfirmation);

      verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, presenter);
      verify(eventBus).addHandler(RequestValidationEvent.getType(), presenter);
      verify(eventBus).addHandler(InsertStringInEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(CopyDataToEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(), presenter);
      verify(saveAsApprovedConfirmation).setListener(presenter);

      when(displayProvider.get()).thenReturn(display);
      presenter.showData(currentPageRows);

   }

   @Test
   public void canValidate()
   {
      selectedTU = currentPageRows.get(0);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(editor.getId()).thenReturn(selectedTU.getId());
      when(editor.getText()).thenReturn("target");
      when(sourceContentPresenter.getSourceContent(selectedTU.getId())).thenReturn(Optional.of("source"));

      presenter.validate(editor);

      verify(eventBus).fireEvent(eventCaptor.capture());
      RunValidationEvent event = (RunValidationEvent) eventCaptor.getValue();
      assertThat(event.getSourceContent(), equalTo("source"));
      assertThat(event.getTarget(), equalTo("target"));
      assertThat(event.isFireNotification(), equalTo(false));
      assertThat(event.getWidgetList(), Matchers.<HasUpdateValidationWarning>containsInAnyOrder(editor, display));
   }

   @Test
   public void canSaveAsFuzzy()
   {
      // Given: selected one trans unit with some new targets inputted
      selectedTU = currentPageRows.get(0);
      when(display.getNewTargets()).thenReturn(NEW_TARGETS);
      when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.saveAsFuzzy(selectedTU.getId());

      // Then:
      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TransUnitSaveEvent event = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TransUnitSaveEvent.class);

      assertThat(event.getTransUnitId(), equalTo(selectedTU.getId()));
      assertThat(event.getTargets(), Matchers.equalTo(NEW_TARGETS));
      assertThat(event.getStatus(), equalTo(ContentState.NeedReview));
   }

   @Test
   public void canCopySource()
   {
      // Given: selected one trans unit
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");

      presenter.copySource(editor, selectedTU.getId());

      verify(editor).setTextAndValidate("source");
      verify(editor).setFocus();
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
   }


   @Test
   public void canGetNewTargets()
   {
      selectedTU = currentPageRows.get(0);
      when(display.getNewTargets()).thenReturn(NEW_TARGETS);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      List<String> result = presenter.getNewTargets();

      MatcherAssert.assertThat(result, Matchers.sameInstance(NEW_TARGETS));
   }

   @Test
   public void onRequestValidationWillNotFireRunValidationEventIfSourceAndTargetDoNotMatch()
   {
      //given current display is null
      when(sourceContentPresenter.getCurrentTransUnitIdOrNull()).thenReturn(new TransUnitId(1));

      presenter.onRequestValidation(RequestValidationEvent.EVENT);

      verifyNoMoreInteractions(eventBus);

   }

   @Test
   public void onRequestValidationWillFireRunValidationEvent()
   {
      //given current display has one editor and current editor has target content
      selectedTU = currentPageRows.get(0);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(editor.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      when(sourceContentPresenter.getCurrentTransUnitIdOrNull()).thenReturn(selectedTU.getId());
      when(sourceContentPresenter.getSourceContent(selectedTU.getId())).thenReturn(Optional.of("source"));
      when(editor.getText()).thenReturn("target");

      presenter.onRequestValidation(RequestValidationEvent.EVENT);

      verify(eventBus).fireEvent(eventCaptor.capture());
      RunValidationEvent event = TestFixture.extractFromEvents(eventCaptor.getAllValues(), RunValidationEvent.class);
      MatcherAssert.assertThat(event.getTarget(), Matchers.equalTo("target"));
   }

   @Test
   public void onCancelCanResetTextBack()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
      when(display.getId()).thenReturn(selectedTU.getId());
      ArrayList<ToggleEditor> currentEditors = Lists.newArrayList(editor);
      when(display.getEditors()).thenReturn(currentEditors);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.onCancel(selectedTU.getId());

      // Then:
      verify(display).revertEditorContents();
      verify(display).highlightSearch(anyString());
      verify(display).focusEditor(0);
   }

   @Test
   public void testOnInsertString()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      when(editor.getId()).thenReturn(selectedTU.getId());
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      when(sourceContentPresenter.getSourceContent(selectedTU.getId())).thenReturn(Optional.of("source content"));

      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.onInsertString(new InsertStringInEditorEvent("", "suggestion"));

      // Then:
      verify(editor).insertTextInCursorPosition("suggestion");

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      NotificationEvent notificationEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), NotificationEvent.class);
      MatcherAssert.assertThat(notificationEvent.getMessage(), Matchers.equalTo("copied"));

      RunValidationEvent runValidationEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), RunValidationEvent.class);
      assertThat(runValidationEvent.getSourceContent(), equalTo("source content"));
   }

   @Test
   public void testOnTransMemoryCopy()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      presenter.onDataCopy(new CopyDataToEditorEvent(Arrays.asList("target")));

      verify(editor).setTextAndValidate("target");
      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      NotificationEvent notificationEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), NotificationEvent.class);
      MatcherAssert.assertThat(notificationEvent.getMessage(), Matchers.equalTo("copied"));
   }

   @Test
   public void willMoveToNextEntryIfItIsPluralAndNotAtLastEntry()
   {
      // Given: selected display and focus on first entry
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor, editor2));
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.saveAsApprovedAndMoveNext(selectedTU.getId());

      // Then:
      verify(display).focusEditor(1);
   }

   @Test
   public void willSaveAndMoveToNextRow()
   {
      // Given: selected display and there is only one entry(no plural or last entry of plural)
      selectedTU = currentPageRows.get(0);
      when(display.getNewTargets()).thenReturn(NEW_TARGETS);
      when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.saveAsApprovedAndMoveNext(selectedTU.getId());

      // Then:
      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());

      TransUnitSaveEvent saveEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TransUnitSaveEvent.class);
      assertThat(saveEvent.getTransUnitId(), equalTo(selectedTU.getId()));
      assertThat(saveEvent.getTargets(), Matchers.equalTo(NEW_TARGETS));
      assertThat(saveEvent.getStatus(), equalTo(ContentState.Approved));

      NavTransUnitEvent navEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), NavTransUnitEvent.class);
      assertThat(navEvent.getRowType(), equalTo(NavTransUnitEvent.NavigationType.NextEntry));
   }

   @Test
   public void canShowHistory()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.showHistory(selectedTU.getId());

      // Then:
      verify(historyPresenter).showTranslationHistory(selectedTU.getId());
   }

   @Test
   public void canSavePendingChangesIfContentHasChanged()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
      when(display.getNewTargets()).thenReturn(NEW_TARGETS);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getVerNum()).thenReturn(99);

      // When:
      presenter.savePendingChangesIfApplicable();

      // Then:
      verify(eventBus).fireEvent(new TransUnitSaveEvent(NEW_TARGETS, ContentState.Approved, selectedTU.getId(), 99, CACHED_TARGETS));
   }

   @Test
   public void willIgnoreSavePendingIfNoChange()
   {
      // Given: display new targets is equal to cached targets
      selectedTU = currentPageRows.get(0);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
      when(display.getNewTargets()).thenReturn(CACHED_TARGETS);

      // When:
      presenter.savePendingChangesIfApplicable();

      // Then:
      verifyZeroInteractions(eventBus);
      verify(display, never()).revertEditorContents();
   }

   @Test
   public void canGetCurrentTransUnitId()
   {
      selectedTU = currentPageRows.get(0);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      TransUnitId result = presenter.getCurrentTransUnitIdOrNull();
      assertThat(result, Matchers.sameInstance(selectedTU.getId()));
   }

   @Test
   public void testIsReadOnly()
   {
      userWorkspaceContext.setProjectActive(false);

      boolean readOnly = presenter.isReadOnly();

      assertThat(readOnly, Matchers.is(true));
   }

   @Test
   public void canGetConfigState()
   {
      assertThat(presenter.getConfigState(), Matchers.equalTo(configHolder.getState()));
   }

   @Test
   public void testIsDisplayButtons()
   {
      userWorkspaceContext.setHasWriteAccess(false);

      boolean displayButtons = presenter.isDisplayButtons();

      assertThat(displayButtons, Matchers.is(false));
   }

   @Test
   public void testOnFocus()
   {
      selectedTU = currentPageRows.get(0);
      TransUnitId oldSelection = currentPageRows.get(1).getId();
      presenter.setStatesForTesting(oldSelection, 0, display);

      presenter.onEditorClicked(selectedTU.getId(), 1);

      verify(eventBus).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(selectedTU.getId()));
   }

   @Test
   public void testSetFocus()
   {
      presenter.setStatesForTesting(selectedTU.getId(), 99, display);

      presenter.setFocus();

      // current editor index will be corrected if out of bound
      verify(display).focusEditor(0);
   }

   @Test
   public void onUserConfigChangeEvent()
   {
      // Given: change default settings in config
      configHolder.setDisplayButtons(false);
      configHolder.setSpellCheckEnabled(false);

      // When:
      presenter.onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

      // Then:
      verify(display, times(3)).showButtons(false);
      verify(saveAsApprovedConfirmation).setShowSaveApprovedWarning(userOptionsService.getConfigHolder().getState().isShowSaveApprovedWarning());
      verify(display, times(3)).setEnableSpellCheck(false);
   }

   @Test
   public void testRevealDisplay()
   {
      presenter.revealDisplay();

      verify(editorKeyShortcuts).enableEditContext();
   }

   @Test
   public void testConcealDisplay()
   {
      presenter.concealDisplay();

      verify(editorKeyShortcuts).enableNavigationContext();
   }

   @Test
   public void canGetDisplays()
   {
      List<TargetContentsDisplay> displays = presenter.getDisplays();

      assertThat(displays, Matchers.contains(display, display, display));
   }

   @Test
   public void canHighlightSearch()
   {
      presenter.highlightSearch("search");

      verify(display, times(3)).highlightSearch("search");
   }

   @Test
   public void canUpdateRowIfInCurrentDisplays()
   {
      selectedTU = currentPageRows.get(2);
      when(display.getId()).thenReturn(selectedTU.getId());
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(selectedTU.getId().getId());

      presenter.updateRow(updatedTransUnit);

      verify(display).setValueAndCreateNewEditors(updatedTransUnit);
      verify(display).refresh();
   }

   @Test
   public void canUpdateRowIfInCurrentDisplaysAndIsCurrentRow()
   {
      selectedTU = currentPageRows.get(2);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(display.getId()).thenReturn(selectedTU.getId());
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(selectedTU.getId().getId());

      presenter.updateRow(updatedTransUnit);

      verify(display).setValueAndCreateNewEditors(updatedTransUnit);
      verify(display).refresh();
      verify(display).getEditors();
      verify(editorTranslators).updateTranslator(display.getEditors(), selectedTU.getId());
   }

   @Test
   public void willIgnoreIfUpdateRowWithValueNotInCurrentPage()
   {
      selectedTU = currentPageRows.get(2);
      when(display.getId()).thenReturn(selectedTU.getId());
      TransUnit updatedTransUnit = TestFixture.makeTransUnit(99);

      presenter.updateRow(updatedTransUnit);

      verify(display, never()).setValueAndCreateNewEditors(updatedTransUnit);
   }

   @Test
   public void onWorkspaceContextUpdateEventBecomeReadOnly()
   {
      // Given: event sets workspace to read only
      WorkspaceContextUpdateEvent event = mock(WorkspaceContextUpdateEvent.class);
      when(event.isProjectActive()).thenReturn(false);

      // When:
      presenter.onWorkspaceContextUpdated(event);

      // Then:
      verify(display, times(3)).setToMode(ToggleEditor.ViewMode.VIEW);
      verify(display, times(3)).showButtons(false);
      verify(editorKeyShortcuts).enableNavigationContext();
   }

   @Test
   public void onWorkspaceContextUpdateEventFromReadOnlyToWritable()
   {
      // Given: event sets workspace to writable and we first have workspace as read only
      userWorkspaceContext.setProjectActive(false);
      WorkspaceContextUpdateEvent event = mock(WorkspaceContextUpdateEvent.class);
      when(event.isProjectActive()).thenReturn(true);

      // When:
      presenter.onWorkspaceContextUpdated(event);

      // Then:
      verify(display, times(3)).setToMode(ToggleEditor.ViewMode.EDIT);
      verify(display, atLeast(3)).showButtons(true);
      verify(editorKeyShortcuts).enableEditContext();
   }

   @Test
   public void onTransUnitEditEvent()
   {
      selectedTU = currentPageRows.get(0);
      TransUnitEditEvent event = mock(TransUnitEditEvent.class);
      when(event.getSelectedTransUnitId()).thenReturn(selectedTU.getId());
      ArrayList<ToggleEditor> currentEditors = Lists.newArrayList(editor);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);
      when(display.getEditors()).thenReturn(currentEditors);

      presenter.onTransUnitEdit(event);

      InOrder inOrder = inOrder(editorTranslators);
      inOrder.verify(editorTranslators).clearTranslatorList(currentEditors);
      inOrder.verify(editorTranslators).updateTranslator(currentEditors, selectedTU.getId());
      verifyNoMoreInteractions(editorTranslators);
   }

   @Test
   public void canMoveToPreviousEditorInPluralForm()
   {
      // Given: current editor index is 1
      presenter.setStatesForTesting(selectedTU.getId(), 1, display);

      // When:
      presenter.moveToPreviousEntry();

      // Then:
      verify(display).focusEditor(0);
      verifyZeroInteractions(eventBus);
   }

   @Test
   public void canMoveToPreviousEntry()
   {
      // Given: default current editor index is 0
      TargetContentsPresenter spyPresenter = spy(presenter);
      doNothing().when(spyPresenter).savePendingChangesIfApplicable();

      // When:
      spyPresenter.moveToPreviousEntry();

      // Then:
      verify(spyPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(NavTransUnitEvent.PREV_ENTRY_EVENT);
   }

   @Test
   public void canMoveToNextEditorInPluralForm()
   {
      // Given: current editor index is 0
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor, editor2));
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      // When:
      presenter.moveToNextEntry();

      // Then:
      verify(display).focusEditor(1);
      verifyZeroInteractions(eventBus);
   }

   @Test
   public void canMoveToNextEditorInPluralFormOnFirstRow()
   {
      // Given: current editor index is last index (represent last entry from move to previous)
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor, editor2));
      presenter.setStatesForTesting(selectedTU.getId(), TargetContentsPresenter.LAST_INDEX, display);

      // When:
      presenter.moveToNextEntry();

      // Then:
      verify(display).focusEditor(1);
      verifyZeroInteractions(eventBus);
   }

   @Test
   public void canMoveToNextEntry()
   {
      // Given: current editor index is 1
      TargetContentsPresenter spyPresenter = spy(presenter);
      spyPresenter.setStatesForTesting(selectedTU.getId(), 1, display);
      doNothing().when(spyPresenter).savePendingChangesIfApplicable();

      // When:
      spyPresenter.moveToNextEntry();

      // Then:
      verify(spyPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
   }

   @Test
   public void canCopySourceForActiveRow()
   {
      // Given: current editor is focused
      TargetContentsPresenter spyPresenter = spy(presenter);
      selectedTU = currentPageRows.get(0);
      spyPresenter.setStatesForTesting(selectedTU.getId(), 0, display);
      doNothing().when(spyPresenter).copySource(editor, selectedTU.getId());
      when(editor.isFocused()).thenReturn(true);
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));

      // When:
      spyPresenter.copySourceForActiveRow();

      // Then:
      verify(spyPresenter).copySource(editor, selectedTU.getId());
   }

   @Test
   public void canAddUndoLink()
   {
      UndoLink undoLink = mock(UndoLink.class);

      presenter.addUndoLink(0, undoLink);

      verify(display).addUndo(undoLink);
   }

   @Test
   public void canUpdateStateToSavedOnlyIfCachedTargetsAndInEditorTargetsAreEqual()
   {
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getCachedTargets()).thenReturn(Lists.newArrayList("a"));
      when(display.getNewTargets()).thenReturn(Lists.newArrayList("a"));

      presenter.setEditingState(selectedTU.getId(), TargetContentsDisplay.EditingState.SAVED);

      verify(display).setState(TargetContentsDisplay.EditingState.SAVED);
   }

   @Test
   public void willIgnoreUpdateEditingStateToSavedIfCachedTargetsAndInEditorTargetsAreNotEqual()
   {
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getCachedTargets()).thenReturn(Lists.newArrayList("a"));
      when(display.getNewTargets()).thenReturn(Lists.newArrayList("b"));

      presenter.setEditingState(selectedTU.getId(), TargetContentsDisplay.EditingState.SAVED);

      verify(display, never()).setState(TargetContentsDisplay.EditingState.SAVED);
   }

   @Test
   public void canUpdateToOtherStateIfCachedTargetsAndInEditorTargetsAreNotEqual()
   {
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getCachedTargets()).thenReturn(Lists.newArrayList("a"));
      when(display.getNewTargets()).thenReturn(Lists.newArrayList("b"));

      presenter.setEditingState(selectedTU.getId(), TargetContentsDisplay.EditingState.SAVING);

      verify(display).setState(TargetContentsDisplay.EditingState.SAVING);
   }

   @Test
   public void canConfirmSavedOnSavingState()
   {
      selectedTU = currentPageRows.get(1);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditingState()).thenReturn(TargetContentsDisplay.EditingState.SAVING);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      presenter.confirmSaved(selectedTU);

      verify(display).updateCachedTargetsAndVersion(selectedTU.getTargets(), selectedTU.getVerNum(), selectedTU.getStatus());
      verify(display).setState(TargetContentsDisplay.EditingState.SAVED);
   }

   @Test
   public void canConfirmSavedOnSavedState()
   {
      selectedTU = currentPageRows.get(1);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditingState()).thenReturn(TargetContentsDisplay.EditingState.SAVED);
      presenter.setStatesForTesting(selectedTU.getId(), 0, display);

      presenter.confirmSaved(selectedTU);

      InOrder inOrder = Mockito.inOrder(display);
      inOrder.verify(display).setValueAndCreateNewEditors(selectedTU);
      inOrder.verify(display).refresh();
      inOrder.verify(display).setState(TargetContentsDisplay.EditingState.SAVED);
   }

   @Test
   public void testShowEditorsInReadOnlyMode()
   {
      // Given:
      userWorkspaceContext.setProjectActive(false);
      selectedTU = currentPageRows.get(0);
      ArrayList<ToggleEditor> currentEditors = Lists.newArrayList(editor);
      when(editor.getId()).thenReturn(selectedTU.getId());
      ArrayList<ToggleEditor> previousEditors = Lists.newArrayList(editor2);
      presenter.setStatesForTesting(null, 0, display);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(previousEditors, currentEditors);
      when(sourceContentPresenter.getSourceContent(selectedTU.getId())).thenReturn(Optional.of("source"));

      // When:
      presenter.setSelected(selectedTU.getId());

      // Then:
      verify(editorTranslators).clearTranslatorList(previousEditors);
      verify(editor).clearTranslatorList();
      verify(display).showButtons(false);
      verify(display).setToMode(ToggleEditor.ViewMode.VIEW);
      verify(editorKeyShortcuts).enableNavigationContext();
   }
}
