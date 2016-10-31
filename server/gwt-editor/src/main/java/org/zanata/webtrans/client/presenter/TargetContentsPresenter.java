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

import static com.google.common.base.Objects.equal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CheckStateHasChangedEvent;
import org.zanata.webtrans.client.events.CommentBeforeSaveEvent;
import org.zanata.webtrans.client.events.CommentChangedEvent;
import org.zanata.webtrans.client.events.CommentChangedEventHandler;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.ReloadUserConfigUIEvent;
import org.zanata.webtrans.client.events.RequestSelectTableRowEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RequestValidationEventHandler;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.SaveAsApprovedConfirmationDisplay;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.ui.ValidationWarningDisplay;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.WorkspaceRestrictions;
import org.zanata.webtrans.shared.util.Finds;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.gwt.core.shared.GWT;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener,
        TransUnitEditEventHandler, UserConfigChangeHandler,
        RequestValidationEventHandler, InsertStringInEditorHandler,
        CopyDataToEditorHandler, WorkspaceContextUpdateEventHandler,
        CommentChangedEventHandler {
    protected static final int LAST_INDEX = -2;
    private final EventBus eventBus;
    private final TableEditorMessages messages;
    private final SourceContentsPresenter sourceContentsPresenter;
    private final TranslationHistoryPresenter historyPresenter;
    private final Provider<TargetContentsDisplay> displayProvider;
    private final EditorTranslators editorTranslators;
    private final EditorKeyShortcuts editorKeyShortcuts;
    private final UserWorkspaceContext userWorkspaceContext;
    private final UserOptionsService userOptionsService;
    private final SaveAsApprovedConfirmationDisplay saveAsApprovedConfirmation;
    private final ValidationWarningDisplay validationWarning;

    private TargetContentsDisplay display;
    private List<TargetContentsDisplay> displayList = Collections.emptyList();
    private int currentEditorIndex = 0;
    private TransUnitId currentTransUnitId;

    // cached state
    private String findMessage;
    private boolean isDisplayButtons;

    @Inject
    public TargetContentsPresenter(
            Provider<TargetContentsDisplay> displayProvider,
            EditorTranslators editorTranslators, final EventBus eventBus,
            TableEditorMessages messages,
            SourceContentsPresenter sourceContentsPresenter,
            UserWorkspaceContext userWorkspaceContext,
            EditorKeyShortcuts editorKeyShortcuts,
            TranslationHistoryPresenter historyPresenter,
            UserOptionsService userOptionsService,
            SaveAsApprovedConfirmationDisplay saveAsApprovedConfirmation,
            ValidationWarningDisplay validationWarning) {
        this.displayProvider = displayProvider;
        this.editorTranslators = editorTranslators;
        this.userWorkspaceContext = userWorkspaceContext;
        this.eventBus = eventBus;
        this.messages = messages;
        this.sourceContentsPresenter = sourceContentsPresenter;
        this.editorKeyShortcuts = editorKeyShortcuts;
        this.historyPresenter = historyPresenter;
        this.historyPresenter.setCurrentValueHolder(this);
        this.userOptionsService = userOptionsService;
        this.saveAsApprovedConfirmation = saveAsApprovedConfirmation;
        this.validationWarning = validationWarning;

        isDisplayButtons =
                userOptionsService.getConfigHolder().getState()
                        .isDisplayButtons();

        editorKeyShortcuts.registerKeys(this);
        saveAsApprovedConfirmation.setListener(this);
        validationWarning.setListener(this);

        bindEventHandlers();
    }

    private void bindEventHandlers() {
        eventBus.addHandler(UserConfigChangeEvent.TYPE, this);
        eventBus.addHandler(RequestValidationEvent.getType(), this);
        eventBus.addHandler(InsertStringInEditorEvent.getType(), this);
        eventBus.addHandler(CopyDataToEditorEvent.getType(), this);
        eventBus.addHandler(TransUnitEditEvent.getType(), this);
        eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
        eventBus.addHandler(CommentChangedEvent.TYPE, this);
    }

    public void savePendingChangesIfApplicable() {
        if (currentEditorContentHasChanged()) {
            saveCurrentIfValid(ContentState.Translated);
        }
    }

    public void gotoRow(DocumentInfo documentInfo, TransUnitId transUnitId) {
        eventBus.fireEvent(new RequestSelectTableRowEvent(documentInfo,
                transUnitId));
    }

    /**
     * Fire TransUnitSaveEvent if there's no validation error
     *
     * @param status
     * @return if saving is to continue by TransUnitSaveEvent listener
     */
    public boolean saveCurrentIfValid(ContentState status) {
        Map<ValidationAction, List<String>> errorMessages =
                display.getErrorMessages();

        if (status.isTranslated() && !errorMessages.isEmpty()) {
            validationWarning.center(display.getId(),
                    userWorkspaceContext.getSelectedDoc(), getNewTargets(),
                    errorMessages);
            return false;
        } else {
            eventBus.fireEvent(new TransUnitSaveEvent(getNewTargets(), status,
                    display.getId(), display.getVerNum(), display
                            .getCachedTargets()));
        }
        return true;
    }

    public boolean currentEditorContentHasChanged() {
        return hasSelectedRow()
                && !equal(display.getCachedTargets(), display.getNewTargets());
    }

    private ToggleEditor getCurrentEditor() {
        return display.getEditors().get(currentEditorIndex);
    }

    public void setSelected(final TransUnitId currentTransUnitId) {
        this.currentTransUnitId = currentTransUnitId;

        if (display != null) {
            // clear previous selection's translator list
            display.clearTranslatorList();
        }

        display = Finds.findDisplayById(displayList, currentTransUnitId).get();
        Log.info("selecting id:" + currentTransUnitId + " version: "
                + display.getVerNum());

        normaliseCurrentEditorIndex();

        for (ToggleEditor editor : display.getEditors()) {
            validate(editor);
        }
        display.showButtons(isDisplayButtons());

        if (!canEditTranslation()) {
            display.setToMode(ViewMode.VIEW);
            concealDisplay();
        } else {
            display.focusEditor(currentEditorIndex);
            editorTranslators.updateTranslator(display, currentTransUnitId);
            revealDisplay();
        }
    }

    private void normaliseCurrentEditorIndex() {
        ArrayList<ToggleEditor> currentEditors = display.getEditors();
        if (currentEditorIndex == LAST_INDEX) {
            currentEditorIndex = currentEditors.size() - 1;
        }
        if (currentEditorIndex < 0
                || currentEditorIndex >= currentEditors.size()) {
            Log.warn("editor index is invalid:" + currentEditorIndex
                    + ". Set to 0");
            currentEditorIndex = 0;
        }
    }

    @Override
    public void onTransUnitEdit(TransUnitEditEvent event) {
        if (event.getSelectedTransUnitId() != null && display != null) {
            display.clearTranslatorList();
            editorTranslators.updateTranslator(display, currentTransUnitId);
        }
    }

    @Override
    public void validate(ToggleEditor editor) {
        TransUnitId transUnitId = editor.getId();
        Optional<String> sourceContent =
                sourceContentsPresenter.getSourceContent(transUnitId);
        if (sourceContent.isPresent()) {
            RunValidationEvent event =
                    new RunValidationEvent(sourceContent.get(),
                            editor.getText(), false);
            // widget that displays red outline
            event.addWidget(editor);
            // widget that displays warnings
            Optional<TargetContentsDisplay> targetDisplay =
                    Finds.findDisplayById(displayList, transUnitId);
            if (targetDisplay.isPresent()) {
                event.addWidget(targetDisplay.get());
            }
            eventBus.fireEvent(event);
        }
    }

    /**
     * Will fire a save event and a following navigation event will cause
     * another pending save event. But TransUnitSaveService will ignore the
     * second one.
     *
     * @see org.zanata.webtrans.client.service.TransUnitSaveService#onTransUnitSave(org.zanata.webtrans.client.events.TransUnitSaveEvent)
     * @param transUnitId
     *            the state variable of the display that user has clicked on
     */
    @Override
    public void saveAsApprovedAndMoveNext(TransUnitId transUnitId) {
        ensureRowSelection(transUnitId);
        if (currentEditorIndex + 1 < display.getEditors().size()) {
            display.focusEditor(currentEditorIndex + 1);
            currentEditorIndex++;
        } else {
            if (saveCurrentIfValid(ContentState.Translated)) {
                currentEditorIndex = 0;
                eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
            }
        }
    }

    public void showSaveAsApprovedConfirmation(TransUnitId transUnitId) {
        saveAsApprovedConfirmation.center(transUnitId);
    }

    public void checkConfirmationBeforeSave() {
        TransUnitId transUnitId = getCurrentTransUnitIdOrNull();
        if (userOptionsService.getConfigHolder().getState()
                .isShowSaveApprovedWarning()) {
            eventBus.fireEvent(new CheckStateHasChangedEvent(transUnitId,
                    getNewTargets(), ContentState.Translated));
        } else {
            saveAsApprovedAndMoveNext(transUnitId);
        }
    }

    @Override
    public void saveAsFuzzy(TransUnitId transUnitId) {
        ensureRowSelection(transUnitId);
        saveCurrentIfValid(ContentState.NeedReview);
    }

    protected void moveToPreviousEntry() {
        if (currentEditorIndex - 1 >= 0) {
            display.focusEditor(currentEditorIndex - 1);
            currentEditorIndex--;
        } else {
            currentEditorIndex = LAST_INDEX;
            savePendingChangesIfApplicable();
            eventBus.fireEvent(NavTransUnitEvent.PREV_ENTRY_EVENT);
        }
    }

    protected void moveToNextEntry() {
        if (currentEditorIndex == LAST_INDEX) {
            currentEditorIndex = 0;
        }
        if (currentEditorIndex + 1 < display.getEditors().size()) {
            display.focusEditor(currentEditorIndex + 1);
            currentEditorIndex++;
        } else {
            currentEditorIndex = 0;
            savePendingChangesIfApplicable();
            eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
        }
    }

    public TransUnitId getCurrentTransUnitIdOrNull() {
        return currentTransUnitId;
    }

    @Override
    public boolean isDisplayButtons() {
        return userOptionsService.getConfigHolder().getState()
                .isDisplayButtons()
                && !userWorkspaceContext.hasReadOnlyAccess();
    }

    @Override
    public boolean isReadOnly() {
        return userWorkspaceContext.hasReadOnlyAccess();
    }

    @Override
    public void showHistory(TransUnitId transUnitId) {
        ensureRowSelection(transUnitId);
        historyPresenter.showTranslationHistory(currentTransUnitId);
    }

    @Override
    public void onEditorClicked(TransUnitId id, int editorIndex) {
        currentEditorIndex = editorIndex;
        ensureRowSelection(id);
    }

    @Override
    public void onCancel(TransUnitId transUnitId) {
        ensureRowSelection(transUnitId);
        display.revertEditorContents();
        display.highlightSearch(findMessage);
        setFocus();
    }

    private void ensureRowSelection(TransUnitId transUnitId) {
        if (!equal(currentTransUnitId, transUnitId)) {
            // user click on editor area that is not on current selected row
            eventBus.fireEvent(new TableRowSelectedEvent(transUnitId));
        }
    }

    @Override
    public void copySource(ToggleEditor editor, TransUnitId id) {
        if (canEditTranslation()) {
            currentEditorIndex = editor.getIndex();
            ensureRowSelection(id);
            editor.setTextAndValidate(sourceContentsPresenter
                    .getSelectedSource());
            editor.setFocus();

            eventBus.fireEvent(new NotificationEvent(Severity.Info, messages
                    .notifyCopied()));
        }
    }

    protected void copySourceForActiveRow() {
        if (getCurrentEditor().isFocused()) {
            copySource(getCurrentEditor(), currentTransUnitId);
        }
    }

    public List<String> getNewTargets() {
        return hasSelectedRow() ? display.getNewTargets() : null;
    }

    @Override
    public void onUserConfigChanged(UserConfigChangeEvent event) {
        if (event.getView() != MainView.Editor) {
            return;
        }
        boolean displayButtons =
                userOptionsService.getConfigHolder().getState()
                        .isDisplayButtons();

        if (isDisplayButtons != displayButtons) {
            for (TargetContentsDisplay contentsDisplay : displayList) {
                contentsDisplay.showButtons(displayButtons);
            }
        }

        saveAsApprovedConfirmation
                .setShowSaveApprovedWarning(userOptionsService
                        .getConfigHolder().getState()
                        .isShowSaveApprovedWarning());
        isDisplayButtons = displayButtons;
    }

    @Override
    public void onRequestValidation(RequestValidationEvent event) {
        if (hasSelectedRow()
                && equal(sourceContentsPresenter.getCurrentTransUnitIdOrNull(),
                        currentTransUnitId)) {
            for (ToggleEditor editor : display.getEditors()) {
                validate(editor);
            }
        }
    }

    @Override
    public void onInsertString(final InsertStringInEditorEvent event) {
        copyTextWhenIsEditing(Arrays.asList(event.getSuggestion()), true);
    }

    @Override
    public void onDataCopy(final CopyDataToEditorEvent event) {
        copyTextWhenIsEditing(event.getTargetResult(), false);
    }

    private void copyTextWhenIsEditing(List<String> contents,
            boolean isInsertText) {
        if (canEditTranslation()) {
            if (isInsertText) {
                getCurrentEditor().insertTextInCursorPosition(contents.get(0));
                validate(getCurrentEditor());
            } else {
                ArrayList<ToggleEditor> editors = display.getEditors();
                for (int i = 0; i < contents.size(); i++) {
                    ToggleEditor editor = editors.get(i);
                    editor.setTextAndValidate(contents.get(i));
                }
            }
            eventBus.fireEvent(new NotificationEvent(Severity.Info, messages
                    .notifyCopied()));
        }
    }

    public void revealDisplay() {
        editorKeyShortcuts.enableEditContext();
    }

    public void concealDisplay() {
        editorKeyShortcuts.enableNavigationContext();
    }

    public void addUndoLink(int row, UndoLink undoLink) {
        TargetContentsDisplay targetContentsDisplay = displayList.get(row);
        targetContentsDisplay.addUndo(undoLink);
    }

    public void showData(List<TransUnit> transUnits) {
        ImmutableList.Builder<TargetContentsDisplay> builder =
                ImmutableList.builder();
        for (TransUnit transUnit : transUnits) {
            TargetContentsDisplay display = displayProvider.get();
            display.setListener(this);
            display.setValueAndCreateNewEditors(transUnit);

            if (!canEditTranslation()) {
                display.setToMode(ViewMode.VIEW);
            }

            display.showButtons(isDisplayButtons());
            builder.add(display);
        }
        displayList = builder.build();
        display = null;
    }

    public List<TargetContentsDisplay> getDisplays() {
        return displayList;
    }

    public void highlightSearch(String message) {
        findMessage = message;
        for (TargetContentsDisplay targetContentsDisplay : displayList) {
            targetContentsDisplay.highlightSearch(message);
        }
    }

    /**
     * Being called when there is a TransUnitUpdatedEvent.
     *
     * @param updatedTransUnit
     *            updated trans unit
     */
    public void updateRow(TransUnit updatedTransUnit) {
        Optional<TargetContentsDisplay> contentsDisplayOptional =
                Finds.findDisplayById(displayList, updatedTransUnit.getId());
        if (contentsDisplayOptional.isPresent()) {
            TargetContentsDisplay contentsDisplay =
                    contentsDisplayOptional.get();
            contentsDisplay.setValueAndCreateNewEditors(updatedTransUnit);
            contentsDisplay.setState(TargetContentsDisplay.EditingState.SAVED);
            contentsDisplay.refresh();
            if (equal(updatedTransUnit.getId(), currentTransUnitId)) {
                editorTranslators.updateTranslator(display, currentTransUnitId);
            }
        }
    }

    /**
     * Being called when this client saves successful (not relying on
     * TransUnitUpdatedEvent from EventService). This will only update the
     * version in underlying table cached value.
     *
     * @param updatedTU
     *            updated trans unit from user itself
     */
    public void confirmSaved(TransUnit updatedTU) {
        Optional<TargetContentsDisplay> contentsDisplayOptional =
                Finds.findDisplayById(displayList, updatedTU.getId());
        if (contentsDisplayOptional.isPresent()) {
            TargetContentsDisplay contentsDisplay =
                    contentsDisplayOptional.get();
            if (contentsDisplay.getEditingState() == TargetContentsDisplay.EditingState.SAVED) {
                // If current display is in saved state, we update both in
                // editor and cached value
                contentsDisplay.setValueAndCreateNewEditors(updatedTU);
                contentsDisplay.refresh();
            } else {
                // editor is in saving state or unsaved state, we don't want to
                // update value in editor, just the cached value.
                contentsDisplay.updateCachedTargetsAndVersion(
                        updatedTU.getTargets(), updatedTU.getVerNum(),
                        updatedTU.getStatus());
            }
            setEditingState(updatedTU.getId(),
                    TargetContentsDisplay.EditingState.SAVED);
        }
    }

    public void setFocus() {
        if (hasSelectedRow()) {
            normaliseCurrentEditorIndex();
            display.focusEditor(currentEditorIndex);
        }
    }

    public boolean hasSelectedRow() {
        return display != null && currentTransUnitId != null;
    }

    @Override
    public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event) {
        // FIXME once setting codemirror editor to readonly it won't be editable
        // again
        userWorkspaceContext.setProjectActive(event.isProjectActive());
        userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                .getProjectIterationId().setProjectType(event.getProjectType());

        for (TargetContentsDisplay targetContentsDisplay : displayList) {
            ViewMode viewMode =
                    canEditTranslation() ? ViewMode.EDIT : ViewMode.VIEW;
            boolean showButtons =
                    !userWorkspaceContext.hasReadOnlyAccess() &&
                            isDisplayButtons();

            targetContentsDisplay.setToMode(viewMode);
            targetContentsDisplay.showButtons(showButtons);
        }

        if (userWorkspaceContext.hasReadOnlyAccess()) {
            concealDisplay();
        } else {
            revealDisplay();
        }
    }

    /**
     * Being used when save failed and when user typing in editor
     *
     * @param transUnitId
     *            id
     * @param editingState
     *            editing state
     *
     */
    @Override
    public void setEditingState(TransUnitId transUnitId,
            TargetContentsDisplay.EditingState editingState) {
        Optional<TargetContentsDisplay> displayOptional =
                Finds.findDisplayById(displayList, transUnitId);
        if (!displayOptional.isPresent()) {
            return;
        }

        TargetContentsDisplay contentsDisplay = displayOptional.get();
        if (editingState == TargetContentsDisplay.EditingState.SAVING) {
            contentsDisplay.setState(TargetContentsDisplay.EditingState.SAVING);
        } else if (!Objects.equal(contentsDisplay.getCachedTargets(),
                contentsDisplay.getNewTargets())) {
            contentsDisplay
                    .setState(TargetContentsDisplay.EditingState.UNSAVED);
        } else {
            contentsDisplay.setState(TargetContentsDisplay.EditingState.SAVED);
        }
    }

    public TargetContentsDisplay getCurrentDisplay() {
        return display;
    }

    @Override
    public void saveUserDecision(Boolean value) {
        userOptionsService.getConfigHolder().setShowSaveApprovedWarning(value);
        userOptionsService.persistOptionChange(userOptionsService
                .getEditorOptions());
        eventBus.fireEvent(new ReloadUserConfigUIEvent(MainView.Editor));
    }

    @Override
    public UserConfigHolder.ConfigurationState getConfigState() {
        return userOptionsService.getConfigHolder().getState();
    }

    @Override
    public boolean canReview() {
        WorkspaceRestrictions restrictions =
                userWorkspaceContext.getWorkspaceRestrictions();
        return restrictions.isHasReviewAccess();
    }

    @Override
    public boolean canEditTranslation() {
        WorkspaceRestrictions restrictions =
                userWorkspaceContext.getWorkspaceRestrictions();
        return restrictions.isHasEditTranslationAccess();
    }

    @Override
    public void acceptTranslation(TransUnitId id) {
        ensureRowSelection(id);
        saveCurrentIfValid(ContentState.Approved);
    }

    @Override
    public void rejectTranslation(TransUnitId id) {
        ensureRowSelection(id);
        if (display.getCachedState() != ContentState.Rejected) {
            TransUnitSaveEvent event =
                    new TransUnitSaveEvent(getNewTargets(),
                            ContentState.Rejected, display.getId(),
                            display.getVerNum(), display.getCachedTargets());
            eventBus.fireEvent(new CommentBeforeSaveEvent(event));
        }
    }

    @Override
    public void onCommentChanged(CommentChangedEvent event) {
        Optional<TargetContentsDisplay> displayOptional =
                Finds.findDisplayById(displayList, event.getTransUnitId());
        if (displayOptional.isPresent()) {
            displayOptional.get().updateCommentIndicator(
                    event.getCommentCount());
        }
    }

    /**
     * For testing only
     *
     * @param currentTransUnitId
     *            current trans unit id
     * @param currentEditorIndex
     *            current editor index
     * @param display
     *            current display
     */
    protected void setStatesForTesting(TransUnitId currentTransUnitId,
            int currentEditorIndex, TargetContentsDisplay display) {
        if (!GWT.isClient()) {
            this.currentTransUnitId = currentTransUnitId;
            this.currentEditorIndex = currentEditorIndex;
            this.display = display;
        }
    }

    public void toggleSyntaxHighlighting() {
        if (hasSelectedRow()) {
            getCurrentDisplay().toggleSyntaxHighlighting();
        }
    }

    public void acceptTranslation() {
        TransUnitId id = getCurrentTransUnitIdOrNull();
        acceptTranslation(id);
    }

    public void rejectTranslation() {
        TransUnitId id = getCurrentTransUnitIdOrNull();
        rejectTranslation(id);
    }
}
