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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Provider;
import net.customware.gwt.presenter.client.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CommentBeforeSaveEvent;
import org.zanata.webtrans.client.events.CommentChangedEvent;
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
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.SaveAsApprovedConfirmationDisplay;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.ui.ValidationWarningDisplay;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.ui.UserConfigHolder;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.test.GWTTestData.extractFromEvents;
import static org.zanata.webtrans.test.GWTTestData.makeTransUnit;
import static org.zanata.webtrans.test.GWTTestData.userWorkspaceContext;

public class TargetContentsPresenterTest {
    private TargetContentsPresenter presenter;

    private static final List<String> NEW_TARGETS = ImmutableList
            .<String> builder().add("a").build();
    private static final List<String> CACHED_TARGETS = ImmutableList
            .<String> builder().add("b").build();
    List<TransUnit> currentPageRows = ImmutableList.<TransUnit> builder()
            .add(makeTransUnit(2)).add(makeTransUnit(3)).add(makeTransUnit(6))
            .build();

    @Mock
    private EventBus eventBus;
    @Mock
    private TableEditorMessages tableEditorMessages;
    @Mock
    private SourceContentsPresenter sourceContentPresenter;
    private UserWorkspaceContext userWorkspaceContext;
    @Mock
    private TargetContentsDisplay display;
    @Mock
    private ToggleEditor editor, editor2;
    private TransUnit selectedTU;

    // all event extends GwtEvent therefore captor will capture them all
    @Captor
    private ArgumentCaptor<GwtEvent<EventHandler>> eventCaptor;

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
    @Mock
    private ValidationWarningDisplay validationWarning;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        configHolder = new UserConfigHolder();

        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        userWorkspaceContext = userWorkspaceContext();
        presenter =
                new TargetContentsPresenter(displayProvider, editorTranslators,
                        eventBus, tableEditorMessages, sourceContentPresenter,
                        userWorkspaceContext, editorKeyShortcuts,
                        historyPresenter, userOptionsService,
                        saveAsApprovedConfirmation, validationWarning);

        verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, presenter);
        verify(eventBus)
                .addHandler(RequestValidationEvent.getType(), presenter);
        verify(eventBus).addHandler(InsertStringInEditorEvent.getType(),
                presenter);
        verify(eventBus).addHandler(CopyDataToEditorEvent.getType(), presenter);
        verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
        verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
        verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(),
                presenter);
        verify(eventBus).addHandler(CommentChangedEvent.TYPE, presenter);
        verify(saveAsApprovedConfirmation).setListener(presenter);
        verify(validationWarning).setListener(presenter);

        when(displayProvider.get()).thenReturn(display);
        presenter.showData(currentPageRows);

    }

    @Test
    public void canValidate() {
        selectedTU = currentPageRows.get(0);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(editor.getId()).thenReturn(selectedTU.getId());
        when(editor.getText()).thenReturn("target");
        when(sourceContentPresenter.getSourceContent(selectedTU.getId()))
                .thenReturn(Optional.of("source"));

        presenter.validate(editor);

        verify(eventBus).fireEvent(eventCaptor.capture());

        RunValidationEvent event =
                extractFromEvents(eventCaptor.getAllValues(),
                        RunValidationEvent.class);
        assertThat(event.getSourceContent()).isEqualTo("source");
        assertThat(event.getTarget()).isEqualTo("target");
        assertThat(event.isFireNotification()).isFalse();
        assertThat(event.getWidgetList())
                .containsExactlyInAnyOrder(editor, display);
    }

    @Test
    public void canSaveAsFuzzy() {
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
        TransUnitSaveEvent event =
                extractFromEvents(eventCaptor.getAllValues(),
                        TransUnitSaveEvent.class);

        assertThat(event.getTransUnitId()).isEqualTo(selectedTU.getId());
        assertThat(event.getTargets()).isEqualTo(NEW_TARGETS);
        assertThat(event.getStatus()).isEqualTo(ContentState.NeedReview);
    }

    @Test
    public void canCopySource() {
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
    public void canGetNewTargets() {
        selectedTU = currentPageRows.get(0);
        when(display.getNewTargets()).thenReturn(NEW_TARGETS);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        List<String> result = presenter.getNewTargets();

        assertThat(result).isSameAs(NEW_TARGETS);
    }

    @Test
    public
            void
            onRequestValidationWillNotFireRunValidationEventIfSourceAndTargetDoNotMatch() {
        // given current display is null
        when(sourceContentPresenter.getCurrentTransUnitIdOrNull()).thenReturn(
                new TransUnitId(1));

        presenter.onRequestValidation(RequestValidationEvent.EVENT);

        verifyNoMoreInteractions(eventBus);

    }

    @Test
    public void onRequestValidationWillFireRunValidationEvent() {
        // given current display has one editor and current editor has target
        // content
        selectedTU = currentPageRows.get(0);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(editor.getId()).thenReturn(selectedTU.getId());
        when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
        when(sourceContentPresenter.getCurrentTransUnitIdOrNull()).thenReturn(
                selectedTU.getId());
        when(sourceContentPresenter.getSourceContent(selectedTU.getId()))
                .thenReturn(Optional.of("source"));
        when(editor.getText()).thenReturn("target");

        presenter.onRequestValidation(RequestValidationEvent.EVENT);

        verify(eventBus).fireEvent(eventCaptor.capture());
        RunValidationEvent event =
                extractFromEvents(eventCaptor.getAllValues(),
                        RunValidationEvent.class);
        assertThat(event.getTarget()).isEqualTo("target");
    }

    @Test
    public void onCancelCanResetTextBack() {
        // Given:
        selectedTU = currentPageRows.get(0);
        when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
        when(display.getId()).thenReturn(selectedTU.getId());
        ArrayList<ToggleEditor> currentEditors = Lists.newArrayList(editor);
        when(display.getEditors()).thenReturn(currentEditors);
        presenter.highlightSearch("");
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        // When:
        presenter.onCancel(selectedTU.getId());

        // Then:
        verify(display).revertEditorContents();
        verify(display, atLeastOnce()).highlightSearch(anyString());
        verify(display).focusEditor(0);
    }

    @Test
    public void testOnInsertString() {
        // Given:
        selectedTU = currentPageRows.get(0);
        when(editor.getId()).thenReturn(selectedTU.getId());
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
        when(tableEditorMessages.notifyCopied()).thenReturn("copied");
        when(sourceContentPresenter.getSourceContent(selectedTU.getId()))
                .thenReturn(Optional.of("source content"));

        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        // When:
        presenter
                .onInsertString(new InsertStringInEditorEvent("", "suggestion"));

        // Then:
        verify(editor).insertTextInCursorPosition("suggestion");

        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        NotificationEvent notificationEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        NotificationEvent.class);
        assertThat(notificationEvent.getMessage()).isEqualTo("copied");

        RunValidationEvent runValidationEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        RunValidationEvent.class);
        assertThat(runValidationEvent.getSourceContent()).isEqualTo("source content");
    }

    @Test
    public void testOnTransMemoryCopy() {
        when(tableEditorMessages.notifyCopied()).thenReturn("copied");
        selectedTU = currentPageRows.get(0);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        presenter
                .onDataCopy(new CopyDataToEditorEvent(Arrays.asList("target")));

        verify(editor).setTextAndValidate("target");
        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        NotificationEvent notificationEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        NotificationEvent.class);
        assertThat(notificationEvent.getMessage()).isEqualTo("copied");
    }

    @Test
    public void willNotMoveToNextEntryIfTranslationHasError() {
        // Given: selected display and focus on first entry
        ValidationMessages messages = mock(ValidationMessages.class);

        Map<ValidationAction, List<String>> errorMessage = Maps.newHashMap();
        errorMessage.put(new HtmlXmlTagValidation(ValidationId.HTML_XML,
                messages), new ArrayList<String>());

        selectedTU = currentPageRows.get(0);
        when(display.getNewTargets()).thenReturn(NEW_TARGETS);
        when(display.getCachedTargets()).thenReturn(CACHED_TARGETS);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
        when(display.getErrorMessages()).thenReturn(errorMessage);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        // When:
        presenter.saveAsApprovedAndMoveNext(selectedTU.getId());

        // Then:
        verify(validationWarning).center(selectedTU.getId(),
                userWorkspaceContext.getSelectedDoc(), NEW_TARGETS,
                errorMessage);
    }

    @Test
    public void willMoveToNextEntryIfItIsPluralAndNotAtLastEntry() {
        // Given: selected display and focus on first entry
        selectedTU = currentPageRows.get(0);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditors()).thenReturn(
                Lists.newArrayList(editor, editor2));
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        // When:
        presenter.saveAsApprovedAndMoveNext(selectedTU.getId());

        // Then:
        verify(display).focusEditor(1);
    }

    @Test
    public void willSaveAndMoveToNextRow() {
        // Given: selected display and there is only one entry(no plural or last
        // entry of plural)
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

        TransUnitSaveEvent saveEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TransUnitSaveEvent.class);
        assertThat(saveEvent.getTransUnitId()).isEqualTo(selectedTU.getId());
        assertThat(saveEvent.getTargets()).isEqualTo(NEW_TARGETS);
        assertThat(saveEvent.getStatus()).isEqualTo(ContentState.Translated);

        NavTransUnitEvent navEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        NavTransUnitEvent.class);
        assertThat(navEvent.getRowType())
                .isEqualTo(NavTransUnitEvent.NavigationType.NextEntry);
    }

    @Test
    public void canShowHistory() {
        // Given:
        selectedTU = currentPageRows.get(0);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        // When:
        presenter.showHistory(selectedTU.getId());

        // Then:
        verify(historyPresenter).showTranslationHistory(selectedTU.getId());
    }

    @Test
    public void canSavePendingChangesIfContentHasChanged() {
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
        verify(eventBus).fireEvent(
                new TransUnitSaveEvent(NEW_TARGETS, ContentState.Approved,
                        selectedTU.getId(), 99, CACHED_TARGETS));
    }

    @Test
    public void willIgnoreSavePendingIfNoChange() {
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
    public void canGetCurrentTransUnitId() {
        selectedTU = currentPageRows.get(0);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        TransUnitId result = presenter.getCurrentTransUnitIdOrNull();
        assertThat(result).isSameAs(selectedTU.getId());
    }

    @Test
    public void testIsReadOnly() {
        userWorkspaceContext.setProjectActive(false);

        boolean readOnly = presenter.isReadOnly();

        assertThat(readOnly).isTrue();
    }

    @Test
    public void canGetConfigState() {
        assertThat(presenter.getConfigState()).isEqualTo(configHolder.getState());
    }

    @Test
    public void testIsDisplayButtons() {
        userWorkspaceContext.setHasEditTranslationAccess(false);
        userWorkspaceContext.setHasReviewAccess(false);

        boolean displayButtons = presenter.isDisplayButtons();

        assertThat(displayButtons).isFalse();
    }

    @Test
    public void testOnFocus() {
        selectedTU = currentPageRows.get(0);
        TransUnitId oldSelection = currentPageRows.get(1).getId();
        presenter.setStatesForTesting(oldSelection, 0, display);

        presenter.onEditorClicked(selectedTU.getId(), 1);

        verify(eventBus).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(tableRowSelectedEvent.getSelectedId()).isEqualTo(selectedTU.getId());
    }

    @Test
    public void testSetFocus() {
        selectedTU = currentPageRows.get(0);
        presenter.setStatesForTesting(selectedTU.getId(), 99, display);

        presenter.setFocus();

        // current editor index will be corrected if out of bound
        verify(display).focusEditor(0);
    }

    @Test
    public void onUserConfigChangeEvent() {
        // Given: change default settings in config
        configHolder.setDisplayButtons(false);

        // When:
        presenter
                .onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

        // Then:
        verify(display, times(3)).showButtons(false);
        verify(saveAsApprovedConfirmation).setShowSaveApprovedWarning(
                userOptionsService.getConfigHolder().getState()
                        .isShowSaveApprovedWarning());
    }

    @Test
    public void testRevealDisplay() {
        presenter.revealDisplay();

        verify(editorKeyShortcuts).enableEditContext();
    }

    @Test
    public void testConcealDisplay() {
        presenter.concealDisplay();

        verify(editorKeyShortcuts).enableNavigationContext();
    }

    @Test
    public void canGetDisplays() {
        List<TargetContentsDisplay> displays = presenter.getDisplays();

        assertThat(displays).contains(display, display, display);
    }

    @Test
    public void canHighlightSearch() {
        presenter.highlightSearch("search");

        verify(display, times(3)).highlightSearch("search");
    }

    @Test
    public void canUpdateRowIfInCurrentDisplays() {
        selectedTU = currentPageRows.get(2);
        when(display.getId()).thenReturn(selectedTU.getId());
        TransUnit updatedTransUnit =
                makeTransUnit(selectedTU.getId().getId());

        presenter.updateRow(updatedTransUnit);

        InOrder inOrder = Mockito.inOrder(display);
        inOrder.verify(display).setValueAndCreateNewEditors(updatedTransUnit);
        inOrder.verify(display).setState(
                TargetContentsDisplay.EditingState.SAVED);
        inOrder.verify(display).refresh();
    }

    @Test
    public void canUpdateRowIfInCurrentDisplaysAndIsCurrentRow() {
        selectedTU = currentPageRows.get(2);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);
        when(display.getId()).thenReturn(selectedTU.getId());
        TransUnit updatedTransUnit =
                makeTransUnit(selectedTU.getId().getId());

        presenter.updateRow(updatedTransUnit);

        verify(display).setValueAndCreateNewEditors(updatedTransUnit);
        verify(display).refresh();
        verify(editorTranslators).updateTranslator(display, selectedTU.getId());
    }

    @Test
    public void willIgnoreIfUpdateRowWithValueNotInCurrentPage() {
        selectedTU = currentPageRows.get(2);
        when(display.getId()).thenReturn(selectedTU.getId());
        TransUnit updatedTransUnit = makeTransUnit(99);

        presenter.updateRow(updatedTransUnit);

        verify(display, never()).setValueAndCreateNewEditors(updatedTransUnit);
    }

    @Test
    public void onWorkspaceContextUpdateEventBecomeReadOnly() {
        // Given: event sets workspace to read only
        WorkspaceContextUpdateEvent event =
                mock(WorkspaceContextUpdateEvent.class);
        when(event.isProjectActive()).thenReturn(false);
        userWorkspaceContext.setHasEditTranslationAccess(false);

        // When:
        presenter.onWorkspaceContextUpdated(event);

        // Then:
        verify(display, times(3)).setToMode(ToggleEditor.ViewMode.VIEW);
        verify(display, times(3)).showButtons(false);
        verify(editorKeyShortcuts).enableNavigationContext();
    }

    @Test
    public void onWorkspaceContextUpdateEventFromReadOnlyToWritable() {
        // Given: event sets workspace to writable and we first have workspace
        // as read only
        userWorkspaceContext.setProjectActive(false);
        WorkspaceContextUpdateEvent event =
                mock(WorkspaceContextUpdateEvent.class);
        when(event.isProjectActive()).thenReturn(true);

        // When:
        presenter.onWorkspaceContextUpdated(event);

        // Then:
        verify(display, times(3)).setToMode(ToggleEditor.ViewMode.EDIT);
        verify(display, atLeast(3)).showButtons(true);
        verify(editorKeyShortcuts).enableEditContext();
    }

    @Test
    public void onTransUnitEditEvent() {
        selectedTU = currentPageRows.get(0);
        TransUnitEditEvent event = mock(TransUnitEditEvent.class);
        when(event.getSelectedTransUnitId()).thenReturn(selectedTU.getId());
        ArrayList<ToggleEditor> currentEditors = Lists.newArrayList(editor);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);
        when(display.getEditors()).thenReturn(currentEditors);

        presenter.onTransUnitEdit(event);

        verify(display).clearTranslatorList();

        InOrder inOrder = Mockito.inOrder(editorTranslators);
        inOrder.verify(editorTranslators).updateTranslator(display,
                selectedTU.getId());
        verifyNoMoreInteractions(editorTranslators);
    }

    @Test
    public void canMoveToPreviousEditorInPluralForm() {
        // Given: current editor index is 1
        selectedTU = currentPageRows.get(1);
        presenter.setStatesForTesting(selectedTU.getId(), 1, display);

        // When:
        presenter.moveToPreviousEntry();

        // Then:
        verify(display).focusEditor(0);
        verifyZeroInteractions(eventBus);
    }

    @Test
    public void canMoveToPreviousEntry() {
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
    public void canMoveToNextEditorInPluralForm() {
        // Given: current editor index is 0
        selectedTU = currentPageRows.get(0);
        when(display.getEditors()).thenReturn(
                Lists.newArrayList(editor, editor2));
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        // When:
        presenter.moveToNextEntry();

        // Then:
        verify(display).focusEditor(1);
        verifyZeroInteractions(eventBus);
    }

    @Test
    public void canMoveToNextEditorInPluralFormOnFirstRow() {
        // Given: current editor index is last index (represent last entry from
        // move to previous)
        selectedTU = currentPageRows.get(currentPageRows.size() - 1);
        when(display.getEditors()).thenReturn(
                Lists.newArrayList(editor, editor2));
        presenter.setStatesForTesting(selectedTU.getId(),
                TargetContentsPresenter.LAST_INDEX, display);

        // When:
        presenter.moveToNextEntry();

        // Then:
        verify(display).focusEditor(1);
        verifyZeroInteractions(eventBus);
    }

    @Test
    public void canMoveToNextEntry() {
        // Given: current editor index is 1
        selectedTU = currentPageRows.get(1);
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
    public void canCopySourceForActiveRow() {
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
    public void canAddUndoLink() {
        UndoLink undoLink = mock(UndoLink.class);

        presenter.addUndoLink(0, undoLink);

        verify(display).addUndo(undoLink);
    }

    @Test
    public
            void
            canUpdateStateToSavedOnlyIfCachedTargetsAndInEditorTargetsAreEqual() {
        selectedTU = currentPageRows.get(0);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getCachedTargets()).thenReturn(Lists.newArrayList("a"));
        when(display.getNewTargets()).thenReturn(Lists.newArrayList("a"));

        presenter.setEditingState(selectedTU.getId(),
                TargetContentsDisplay.EditingState.SAVED);

        verify(display).setState(TargetContentsDisplay.EditingState.SAVED);
    }

    @Test
    public
            void
            willIgnoreUpdateEditingStateToSavedIfCachedTargetsAndInEditorTargetsAreNotEqual() {
        selectedTU = currentPageRows.get(0);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getCachedTargets()).thenReturn(Lists.newArrayList("a"));
        when(display.getNewTargets()).thenReturn(Lists.newArrayList("b"));

        presenter.setEditingState(selectedTU.getId(),
                TargetContentsDisplay.EditingState.SAVED);

        verify(display, never()).setState(
                TargetContentsDisplay.EditingState.SAVED);
    }

    @Test
    public void
            canUpdateToOtherStateIfCachedTargetsAndInEditorTargetsAreNotEqual() {
        selectedTU = currentPageRows.get(0);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getCachedTargets()).thenReturn(Lists.newArrayList("a"));
        when(display.getNewTargets()).thenReturn(Lists.newArrayList("b"));

        presenter.setEditingState(selectedTU.getId(),
                TargetContentsDisplay.EditingState.SAVING);

        verify(display).setState(TargetContentsDisplay.EditingState.SAVING);
    }

    @Test
    public void canConfirmSavedOnSavingState() {
        selectedTU = currentPageRows.get(1);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditingState()).thenReturn(
                TargetContentsDisplay.EditingState.SAVING);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        presenter.confirmSaved(selectedTU);

        verify(display).updateCachedTargetsAndVersion(selectedTU.getTargets(),
                selectedTU.getVerNum(), selectedTU.getStatus());
        verify(display).setState(TargetContentsDisplay.EditingState.SAVED);
    }

    @Test
    public void canConfirmSavedOnSavedState() {
        selectedTU = currentPageRows.get(1);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditingState()).thenReturn(
                TargetContentsDisplay.EditingState.SAVED);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        presenter.confirmSaved(selectedTU);

        InOrder inOrder = Mockito.inOrder(display);
        inOrder.verify(display).setValueAndCreateNewEditors(selectedTU);
        inOrder.verify(display).refresh();
        inOrder.verify(display).setState(
                TargetContentsDisplay.EditingState.SAVED);
    }

    @Test
    public void testShowEditorsInReadOnlyMode() {
        // Given:
        userWorkspaceContext.setProjectActive(false);
        userWorkspaceContext.setHasEditTranslationAccess(false);
        selectedTU = currentPageRows.get(0);
        ArrayList<ToggleEditor> currentEditors = Lists.newArrayList(editor);
        when(editor.getId()).thenReturn(selectedTU.getId());
        presenter.setStatesForTesting(null, 0, display);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getEditors()).thenReturn(currentEditors);
        when(sourceContentPresenter.getSourceContent(selectedTU.getId()))
                .thenReturn(Optional.of("source"));

        // When:
        presenter.setSelected(selectedTU.getId());

        // Then:
        verify(display).clearTranslatorList();
        verify(display).showButtons(false);
        verify(display).setToMode(ToggleEditor.ViewMode.VIEW);
        verify(editorKeyShortcuts).enableNavigationContext();
    }

    @Test
    public void willIgnoreRejectIfItsAlreadyRejectedState() {
        selectedTU = currentPageRows.get(1);
        when(display.getId()).thenReturn(selectedTU.getId());
        when(display.getCachedState()).thenReturn(ContentState.Rejected);
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        presenter.rejectTranslation(selectedTU.getId());

        verifyZeroInteractions(eventBus);
    }

    @Test
    public void rejectTranslationWillForceComment() {
        selectedTU = currentPageRows.get(1);
        when(display.getId()).thenReturn(selectedTU.getId());
        presenter.setStatesForTesting(selectedTU.getId(), 0, display);

        presenter.rejectTranslation(selectedTU.getId());

        verify(eventBus).fireEvent(eventCaptor.capture());
        CommentBeforeSaveEvent commentBeforeSaveEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        CommentBeforeSaveEvent.class);
        assertThat(commentBeforeSaveEvent.getSaveEvent().getStatus())
                .isEqualTo(ContentState.Rejected);
    }
}
