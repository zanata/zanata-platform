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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
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
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import static com.google.common.base.Objects.equal;

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
   private static final int LAST_INDEX = -2;
   private final EventBus eventBus;
   private final TableEditorMessages messages;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TranslationHistoryPresenter historyPresenter;
   private final Provider<TargetContentsDisplay> displayProvider;
   private final EditorTranslators editorTranslators;
   private final UserConfigHolder configHolder;
   private final EditorKeyShortcuts editorKeyShortcuts;
   private final UserWorkspaceContext userWorkspaceContext;

   private TargetContentsDisplay display;
   private List<TargetContentsDisplay> displayList = Collections.emptyList();
   private int currentEditorIndex = 0;
   private TransUnitId currentTransUnitId;
   private List<ToggleEditor> currentEditors = Collections.emptyList();

   // cached state
   private String findMessage;
   private boolean isDisplayButtons;

   @Inject
   // @formatter:off
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, EditorTranslators editorTranslators, final EventBus eventBus,
                                  TableEditorMessages messages,
                                  SourceContentsPresenter sourceContentsPresenter,
                                  final UserConfigHolder configHolder,
                                  UserWorkspaceContext userWorkspaceContext,
                                  EditorKeyShortcuts editorKeyShortcuts,
                                  TranslationHistoryPresenter historyPresenter)
   // @formatter:on
   {
      this.displayProvider = displayProvider;
      this.editorTranslators = editorTranslators;
      this.userWorkspaceContext = userWorkspaceContext;
      this.eventBus = eventBus;
      this.messages = messages;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.configHolder = configHolder;
      this.editorKeyShortcuts = editorKeyShortcuts;
      isDisplayButtons = configHolder.isDisplayButtons();
      this.historyPresenter = historyPresenter;
      this.historyPresenter.setCurrentValueHolder(this);
      editorKeyShortcuts.registerKeys(this);

      bindEventHandlers();
   }

   private void bindEventHandlers()
   {
      eventBus.addHandler(UserConfigChangeEvent.getType(), this);
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
         display.updateCachedAndInEditorTargets(getNewTargets());
      }
   }

   public void saveCurrent(ContentState status)
   {
      eventBus.fireEvent(new TransUnitSaveEvent(getNewTargets(), status, display.getId(), display.getVerNum(), display.getCachedTargets()));
   }

   public boolean currentEditorContentHasChanged()
   {
      return display != null && !equal(display.getCachedTargets(), display.getNewTargets());
   }

   private ToggleEditor getCurrentEditor()
   {
      return currentEditors.get(currentEditorIndex);
   }

   public void showEditors(final TransUnitId currentTransUnitId)
   {
      this.currentTransUnitId = currentTransUnitId;
      Log.info("enter show editor with id:" + currentTransUnitId);

      editorTranslators.clearTranslatorList(currentEditors); // clear previous selection's translator list

      display = findDisplayById(currentTransUnitId);

      currentEditors = display.getEditors();

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
         editorTranslators.updateTranslator(currentEditors, currentTransUnitId);
         revealDisplay();
      }
   }

   private TargetContentsDisplay findDisplayById(TransUnitId currentTransUnitId)
   {
      try
      {
         return Iterables.find(displayList, new FindByTransUnitIdPredicate(currentTransUnitId));
      }
      catch (NoSuchElementException e)
      {
         Log.error("cannot find display by id:" + currentTransUnitId + ". Page has changed?! returning null");
         return null;
      }
   }

   private void normaliseCurrentEditorIndex()
   {
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
   public void onTransUnitEdit(final TransUnitEditEvent event)
   {
      if (event.getSelectedTransUnit() != null)
      {
         editorTranslators.clearTranslatorList(currentEditors);
         editorTranslators.updateTranslator(currentEditors, currentTransUnitId);
      }
   }

   @Override
   public void validate(ToggleEditor editor)
   {
      RunValidationEvent event = new RunValidationEvent(sourceContentsPresenter.getSelectedSource(), editor.getText(), false);
      if (display != null)
      {
         event.addWidget(display);
      }
      event.addWidget(editor);
      eventBus.fireEvent(event);
   }

   /**
    * Will fire a save event and also update cached targets so that a following navigation event won't cause another pending save event.
    * If the save failed, TransUnitSaveService will revert the value back to what it was.
    * @see org.zanata.webtrans.client.service.TransUnitSaveService#onTransUnitSave(org.zanata.webtrans.client.events.TransUnitSaveEvent)
    * @param transUnitId the state variable of the display that user has clicked on
    */
   @Override
   public void saveAsApprovedAndMoveNext(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      if (currentEditorIndex + 1 < currentEditors.size())
      {
         display.focusEditor(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         saveCurrent(ContentState.Approved);
         display.updateCachedAndInEditorTargets(getNewTargets());
         eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
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
      if (currentEditorIndex + 1 < currentEditors.size())
      {
         display.focusEditor(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         savePendingChangesIfApplicable();
         eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
      }
   }


   public TransUnitId getCurrentTransUnitIdOrNull()
   {
      return currentTransUnitId;
   }

   @Override
   public boolean isDisplayButtons()
   {
      return configHolder.isDisplayButtons() && !userWorkspaceContext.hasReadOnlyAccess();
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
   public void onFocus(TransUnitId id, int editorIndex)
   {
      ensureRowSelection(id);
      currentEditorIndex = editorIndex;
   }

   @Override
   public boolean isUsingCodeMirror()
   {
      return configHolder.isUseCodeMirrorEditor();
   }

   @Override
   public void onCancel(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      display.updateCachedAndInEditorTargets(display.getCachedTargets());
      display.highlightSearch(findMessage);
      setFocus();
   }

   private void ensureRowSelection(TransUnitId transUnitId)
   {
      if (!equal(currentTransUnitId, transUnitId))
      {
         //user click on buttons that is not on current selected row
         eventBus.fireEvent(new TableRowSelectedEvent(transUnitId));
      }
   }

   @Override
   public void copySource(ToggleEditor editor, TransUnitId id)
   {
      ensureRowSelection(id);
      currentEditorIndex = editor.getIndex();
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
      return display == null ? null : display.getNewTargets();
   }

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      if (isDisplayButtons != configHolder.isDisplayButtons())
      {
         for (TargetContentsDisplay contentsDisplay : displayList)
         {
            contentsDisplay.showButtons(configHolder.isDisplayButtons());
         }
      }
      isDisplayButtons = configHolder.isDisplayButtons();
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
         for (int i = 0; i < contents.size(); i++)
         {
            ToggleEditor editor = currentEditors.get(i);
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
         display.setValue(transUnit);
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
    * Being called when there is a TransUnitUpdatedEvent
    * @param updatedTransUnit updated trans unit
    */
   public void updateRow(TransUnit updatedTransUnit)
   {
      TargetContentsDisplay contentsDisplay = findDisplayById(updatedTransUnit.getId());
      if (contentsDisplay != null)
      {
         contentsDisplay.setValue(updatedTransUnit);
      }
   }

   public void setFocus()
   {
      if (display != null)
      {
         normaliseCurrentEditorIndex();
         display.focusEditor(currentEditorIndex);
      }
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
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
    * Being used when cancelling edit and also when saved as approved (to prevent another save event triggered by navigation)
    * @param transUnitId id
    * @param targets translation that will be set in editor and cached state in display
    */
   public void updateTargets(TransUnitId transUnitId, List<String> targets)
   {
      TargetContentsDisplay display = findDisplayById(transUnitId);
      if (display != null)
      {
         display.updateCachedAndInEditorTargets(targets);
         display.highlightSearch(findMessage);
      }
   }

   /**
    * For testing only
    * @param currentTransUnitId current trans unit id
    * @param currentEditorIndex current editor index
    * @param display current display
    * @param currentEditors current editors
    */
   protected void setStatesForTesting(TransUnitId currentTransUnitId, int currentEditorIndex, TargetContentsDisplay display, List<ToggleEditor> currentEditors)
   {
      if (!GWT.isClient())
      {
         this.currentTransUnitId = currentTransUnitId;
         this.currentEditorIndex = currentEditorIndex;
         this.display = display;
         this.currentEditors = currentEditors;
      }
   }
}
