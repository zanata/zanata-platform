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

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CheckStateHasChangedEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.ReloadUserConfigUIEvent;
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
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
// @formatter:off
public class TargetContentsPresenter implements
      TargetContentsDisplay.Listener,
      TransUnitEditEventHandler,
      UserConfigChangeHandler,
      RequestValidationEventHandler,
      InsertStringInEditorHandler,
      CopyDataToEditorHandler,
      WorkspaceContextUpdateEventHandler
// @formatter:on
{
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

   private TargetContentsDisplay display;
   private List<TargetContentsDisplay> displayList = Collections.emptyList();
   private int currentEditorIndex = 0;
   private TransUnitId currentTransUnitId;

   // cached state
   private String findMessage;
   private boolean isDisplayButtons;
   private boolean spellCheckEnabled;

   @Inject
   // @formatter:off
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, EditorTranslators editorTranslators, final EventBus eventBus,
                                  TableEditorMessages messages,
                                  SourceContentsPresenter sourceContentsPresenter,
                                  UserWorkspaceContext userWorkspaceContext,
                                  EditorKeyShortcuts editorKeyShortcuts,
                                  TranslationHistoryPresenter historyPresenter,
                                  UserOptionsService userOptionsService, 
                                  SaveAsApprovedConfirmationDisplay saveAsApprovedConfirmation)
   // @formatter:on
   {
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
      isDisplayButtons = userOptionsService.getConfigHolder().isDisplayButtons();
      spellCheckEnabled = userOptionsService.getConfigHolder().isSpellCheckEnabled();
      editorKeyShortcuts.registerKeys(this);
      saveAsApprovedConfirmation.setListener(this);

      bindEventHandlers();
   }

   private void bindEventHandlers()
   {
      eventBus.addHandler(UserConfigChangeEvent.TYPE, this);
      eventBus.addHandler(RequestValidationEvent.getType(), this);
      eventBus.addHandler(InsertStringInEditorEvent.getType(), this);
      eventBus.addHandler(CopyDataToEditorEvent.getType(), this);
      eventBus.addHandler(TransUnitEditEvent.getType(), this);
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
   }

   public void savePendingChangesIfApplicable()
   {
      if (currentEditorContentHasChanged())
      {
         saveCurrent(ContentState.Approved);
      }
   }

   public void saveCurrent(ContentState status)
   {
      eventBus.fireEvent(new TransUnitSaveEvent(getNewTargets(), status, display.getId(), display.getVerNum(), display.getCachedTargets()));
   }

   public boolean currentEditorContentHasChanged()
   {
      return hasSelectedRow() && !equal(display.getCachedTargets(), display.getNewTargets());
   }

   private ToggleEditor getCurrentEditor()
   {
      return display.getEditors().get(currentEditorIndex);
   }

   public void setSelected(final TransUnitId currentTransUnitId)
   {
      this.currentTransUnitId = currentTransUnitId;

      if (display != null)
      {
         editorTranslators.clearTranslatorList(display.getEditors()); // clear previous selection's translator list
      }

      display = findDisplayById(currentTransUnitId).get();
      Log.info("selecting id:" + currentTransUnitId + " version: " + display.getVerNum());

      normaliseCurrentEditorIndex();

      for (ToggleEditor editor : display.getEditors())
      {
         editor.clearTranslatorList();
         validate(editor);
      }
      display.showButtons(isDisplayButtons());

      if (userWorkspaceContext.hasReadOnlyAccess())
      {
         display.setToMode(ViewMode.VIEW);
         concealDisplay();
      }
      else
      {
         display.focusEditor(currentEditorIndex);
         editorTranslators.updateTranslator(display.getEditors(), currentTransUnitId);
         revealDisplay();
      }
   }

   private Optional<TargetContentsDisplay> findDisplayById(TransUnitId currentTransUnitId)
   {
      return Iterables.tryFind(displayList, new FindByTransUnitIdPredicate(currentTransUnitId));
   }

   private void normaliseCurrentEditorIndex()
   {
      ArrayList<ToggleEditor> currentEditors = display.getEditors();
      if (currentEditorIndex == LAST_INDEX)
      {
         currentEditorIndex = currentEditors.size() - 1;
      }
      if (currentEditorIndex < 0 || currentEditorIndex >= currentEditors.size())
      {
         Log.warn("editor index is invalid:" + currentEditorIndex + ". Set to 0");
         currentEditorIndex = 0;
      }
   }

   @Override
   public void onTransUnitEdit(TransUnitEditEvent event)
   {
      if (event.getSelectedTransUnitId() != null)
      {
         ArrayList<ToggleEditor> editors = display.getEditors();
         editorTranslators.clearTranslatorList(editors);
         editorTranslators.updateTranslator(editors, currentTransUnitId);
      }
   }

   @Override
   public void validate(ToggleEditor editor)
   {
      RunValidationEvent event = new RunValidationEvent(sourceContentsPresenter.getSelectedSource(), editor.getText(), false);
      if (hasSelectedRow())
      {
         event.addWidget(display);
      }
      event.addWidget(editor);
      eventBus.fireEvent(event);
   }

   /**
    * Will fire a save event and a following navigation event will cause another pending save event.
    * But TransUnitSaveService will ignore the second one.
    * @see org.zanata.webtrans.client.service.TransUnitSaveService#onTransUnitSave(org.zanata.webtrans.client.events.TransUnitSaveEvent)
    * @param transUnitId the state variable of the display that user has clicked on
    */
   @Override
   public void saveAsApprovedAndMoveNext(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      if (currentEditorIndex + 1 < display.getEditors().size())
      {
         display.focusEditor(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         saveCurrent(ContentState.Approved);
         eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
      }
   }

   public void showSaveAsApprovedConfirmation(TransUnitId transUnitId)
   {
      saveAsApprovedConfirmation.center(transUnitId);
   }

   /*
    * Show of confirmation dialog save as approved.
    * 
    * @see org.zanata.webtrans.client.view.TargetContentsDisplay.Listener#
    * saveAsApprovedAndMoveNext(org.zanata.webtrans.shared.model.TransUnitId,
    * boolean)
    */
   @Override
   public void saveAsApprovedAndMoveNext(TransUnitId transUnitId, boolean checkForConfirmation)
   {
      ensureRowSelection(transUnitId);

      if (checkForConfirmation && userOptionsService.getConfigHolder().isShowSaveApprovedWarning())
      {
         eventBus.fireEvent(new CheckStateHasChangedEvent(transUnitId, getNewTargets(), ContentState.Approved));
      }
      else
      {
         saveAsApprovedAndMoveNext(transUnitId);
      }
   }

   @Override
   public void saveAsFuzzy(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      saveCurrent(ContentState.NeedReview);
   }

   protected void moveToPreviousEntry()
   {
      if (currentEditorIndex - 1 >= 0)
      {
         display.focusEditor(currentEditorIndex - 1);
         currentEditorIndex--;
      }
      else
      {
         currentEditorIndex = LAST_INDEX;
         savePendingChangesIfApplicable();
         eventBus.fireEvent(NavTransUnitEvent.PREV_ENTRY_EVENT);
      }
   }

   protected void moveToNextEntry()
   {
      if (currentEditorIndex == LAST_INDEX)
      {
         currentEditorIndex = 0;
      }
      if (currentEditorIndex + 1 < display.getEditors().size())
      {
         display.focusEditor(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         savePendingChangesIfApplicable();
         eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
         Log.info("fired nav to next event");
      }
   }


   public TransUnitId getCurrentTransUnitIdOrNull()
   {
      return currentTransUnitId;
   }

   @Override
   public boolean isDisplayButtons()
   {
      return userOptionsService.getConfigHolder().isDisplayButtons() && !userWorkspaceContext.hasReadOnlyAccess();
   }

   @Override
   public boolean isReadOnly()
   {
      return userWorkspaceContext.hasReadOnlyAccess();
   }

   @Override
   public void showHistory(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      historyPresenter.showTranslationHistory(currentTransUnitId);
   }

   @Override
   public void onEditorClicked(TransUnitId id, int editorIndex)
   {
      currentEditorIndex = editorIndex;
      ensureRowSelection(id);
   }

   @Override
   public void onCancel(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      display.revertEditorContents();
      display.highlightSearch(findMessage);
      setFocus();
   }

   private void ensureRowSelection(TransUnitId transUnitId)
   {
      if (!equal(currentTransUnitId, transUnitId))
      {
         //user click on editor area that is not on current selected row
         eventBus.fireEvent(new TableRowSelectedEvent(transUnitId));
      }
   }

   @Override
   public void copySource(ToggleEditor editor, TransUnitId id)
   {
      currentEditorIndex = editor.getIndex();
      ensureRowSelection(id);
      editor.setTextAndValidate(sourceContentsPresenter.getSelectedSource());
      editor.setFocus();

      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   protected void copySourceForActiveRow()
   {
      if (getCurrentEditor().isFocused())
      {
         copySource(getCurrentEditor(), currentTransUnitId);
      }
   }

   public List<String> getNewTargets()
   {
      return hasSelectedRow() ? display.getNewTargets() : null;
   }

   @Override
   public void  onUserConfigChanged(UserConfigChangeEvent event)
   {
      if (event.getView() != MainView.Editor)
      {
         return;
      }
      boolean displayButtons = userOptionsService.getConfigHolder().isDisplayButtons();
      boolean isSpellCheckEnabled = userOptionsService.getConfigHolder().isSpellCheckEnabled();

      if (isDisplayButtons != displayButtons || spellCheckEnabled != isSpellCheckEnabled)
      {
         for (TargetContentsDisplay contentsDisplay : displayList)
         {
            contentsDisplay.showButtons(displayButtons);
            contentsDisplay.setEnableSpellCheck(isSpellCheckEnabled);
         }
      }

      saveAsApprovedConfirmation.setShowSaveApprovedWarning(userOptionsService.getConfigHolder().isShowSaveApprovedWarning());
      isDisplayButtons = displayButtons;
      spellCheckEnabled = isSpellCheckEnabled;
   }

   @Override
   public void onRequestValidation(RequestValidationEvent event)
   {
      if (equal(sourceContentsPresenter.getCurrentTransUnitIdOrNull(), currentTransUnitId))
      {
         for (ToggleEditor editor : display.getEditors())
         {
            validate(editor);
         }
      }
   }

   @Override
   public void onInsertString(final InsertStringInEditorEvent event)
   {
      copyTextWhenIsEditing(Arrays.asList(event.getSuggestion()), true);
   }

   @Override
   public void onDataCopy(final CopyDataToEditorEvent event)
   {
      copyTextWhenIsEditing(event.getTargetResult(), false);
   }

   private void copyTextWhenIsEditing(List<String> contents, boolean isInsertText)
   {
      if (isInsertText)
      {
         getCurrentEditor().insertTextInCursorPosition(contents.get(0));
         validate(getCurrentEditor());
      }
      else
      {
         ArrayList<ToggleEditor> editors = display.getEditors();
         for (int i = 0; i < contents.size(); i++)
         {
            ToggleEditor editor = editors.get(i);
            editor.setTextAndValidate(contents.get(i));
         }
      }
      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   public void revealDisplay()
   {
      editorKeyShortcuts.enableEditContext();
   }

   public void concealDisplay()
   {
      editorKeyShortcuts.enableNavigationContext();
   }

   public void addUndoLink(int row, UndoLink undoLink)
   {
      TargetContentsDisplay targetContentsDisplay = displayList.get(row);
      targetContentsDisplay.addUndo(undoLink);
   }

   public void showData(List<TransUnit> transUnits)
   {
      ImmutableList.Builder<TargetContentsDisplay> builder = ImmutableList.builder();
      for (TransUnit transUnit : transUnits)
      {
         TargetContentsDisplay display = displayProvider.get();
         display.setListener(this);
         display.setValueAndCreateNewEditors(transUnit);
         if (userWorkspaceContext.hasReadOnlyAccess())
         {
            display.setToMode(ViewMode.VIEW);
         }
         display.showButtons(isDisplayButtons());
         builder.add(display);
      }
      displayList = builder.build();
   }

   public List<TargetContentsDisplay> getDisplays()
   {
      return displayList;
   }

   public void highlightSearch(String message)
   {
      findMessage = message;
      for (TargetContentsDisplay targetContentsDisplay : displayList)
      {
         targetContentsDisplay.highlightSearch(message);
      }
   }

   /**
    * Being called when there is a TransUnitUpdatedEvent.
    * @param updatedTransUnit updated trans unit
    */
   public void updateRow(TransUnit updatedTransUnit)
   {
      Optional<TargetContentsDisplay> contentsDisplayOptional = findDisplayById(updatedTransUnit.getId());
      if (contentsDisplayOptional.isPresent())
      {
         TargetContentsDisplay contentsDisplay = contentsDisplayOptional.get();
         contentsDisplay.setState(TargetContentsDisplay.EditingState.SAVED);
         contentsDisplay.setValueAndCreateNewEditors(updatedTransUnit);
         contentsDisplay.refresh();
         if (equal(updatedTransUnit.getId(), currentTransUnitId))
         {
            editorTranslators.updateTranslator(display.getEditors(), currentTransUnitId);
         }
      }
   }

   /**
    * Being called when this client saves successful (not relying on TransUnitUpdatedEvent from EventService).
    * This will only update the version in underlying table cached value.
    * @param updatedTU updated trans unit from user itself
    */
   public void confirmSaved(TransUnit updatedTU)
   {
      Optional<TargetContentsDisplay> contentsDisplayOptional = findDisplayById(updatedTU.getId());
      if (contentsDisplayOptional.isPresent())
      {
         TargetContentsDisplay contentsDisplay = contentsDisplayOptional.get();
         if (contentsDisplay.getEditingState() == TargetContentsDisplay.EditingState.SAVED)
         {
            // If current display is in saved state, we update both in editor and cached value
            contentsDisplay.setValueAndCreateNewEditors(updatedTU);
            contentsDisplay.refresh();
         }
         else
         {
            // editor is in saving state or unsaved state, we don't want to update value in editor, just the cached value.
            contentsDisplay.updateCachedTargetsAndVersion(updatedTU.getTargets(), updatedTU.getVerNum(), updatedTU.getStatus());
         }
         setEditingState(updatedTU.getId(), TargetContentsDisplay.EditingState.SAVED);
      }
   }

   public void setFocus()
   {
      if (hasSelectedRow())
      {
         normaliseCurrentEditorIndex();
         display.focusEditor(currentEditorIndex);
      }
   }

   public boolean hasSelectedRow()
   {
      return display != null && currentTransUnitId != null;
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      // FIXME once setting codemirror editor to readonly it won't be editable again
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      if (userWorkspaceContext.hasReadOnlyAccess())
      {
         Log.info("from editable to readonly");
         for (TargetContentsDisplay targetContentsDisplay : displayList)
         {
            targetContentsDisplay.setToMode(ViewMode.VIEW);
            targetContentsDisplay.showButtons(false);
         }
         concealDisplay();
      }
      else if (!userWorkspaceContext.hasReadOnlyAccess())
      {
         Log.info("from readonly mode to writable");
         for (TargetContentsDisplay targetContentsDisplay : displayList)
         {
            targetContentsDisplay.setToMode(ViewMode.EDIT);
            targetContentsDisplay.showButtons(isDisplayButtons());
         }
         revealDisplay();
      }

   }

   /**
    * Being used when save failed and when user typing in editor
    * @param transUnitId id
    * @param editingState editing state
    *
    */
   @Override
   public void setEditingState(TransUnitId transUnitId, TargetContentsDisplay.EditingState editingState)
   {
      Optional<TargetContentsDisplay> displayOptional = findDisplayById(transUnitId);
      if (!displayOptional.isPresent())
      {
         return;
      }

      TargetContentsDisplay contentsDisplay = displayOptional.get();
      if (editingState == TargetContentsDisplay.EditingState.SAVING)
      {
         contentsDisplay.setState(TargetContentsDisplay.EditingState.SAVING);
      }
      else if (!Objects.equal(contentsDisplay.getCachedTargets(), contentsDisplay.getNewTargets()))
      {
         contentsDisplay.setState(TargetContentsDisplay.EditingState.UNSAVED);
      }
      else
      {
         contentsDisplay.setState(TargetContentsDisplay.EditingState.SAVED);
      }
   }

   public TargetContentsDisplay getCurrentDisplay()
   {
      return display;
   }

   @Override
   public void saveUserDecision(Boolean value)
   {
      userOptionsService.getConfigHolder().setShowSaveApprovedWarning(value);
      userOptionsService.persistOptionChange(userOptionsService.getEditorOptions());
      eventBus.fireEvent(new ReloadUserConfigUIEvent(MainView.Editor));
   }

   @Override
   public UserConfigHolder.ConfigurationState getConfigState()
   {
      return userOptionsService.getConfigHolder().getState();
   }

   /**
    * For testing only
    * @param currentTransUnitId current trans unit id
    * @param currentEditorIndex current editor index
    * @param display current display
    */
   protected void setStatesForTesting(TransUnitId currentTransUnitId, int currentEditorIndex, TargetContentsDisplay display)
   {
      if (!GWT.isClient())
      {
         this.currentTransUnitId = currentTransUnitId;
         this.currentEditorIndex = currentEditorIndex;
         this.display = display;
      }
   }
}
