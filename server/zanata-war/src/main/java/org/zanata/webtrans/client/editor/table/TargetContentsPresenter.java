/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.CheckKey;
import org.zanata.webtrans.client.editor.CheckKeyImpl;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RequestValidationEventHandler;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.UpdateValidationWarningsEvent;
import org.zanata.webtrans.client.events.UpdateValidationWarningsEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.ValidationMessagePanel;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener, UserConfigChangeHandler, UpdateValidationWarningsEventHandler, RequestValidationEventHandler, InsertStringInEditorHandler, CopyDataToEditorHandler
{
   private static final int NO_OPEN_EDITOR = -1;
   private static final int LAST_INDEX = -2;
   private final EventBus eventBus;
   private final TableEditorMessages messages;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final UserConfigHolder configHolder;

   private final CheckKey checkKey;
   private NavigationMessages navMessages;
   private WorkspaceContext workspaceContext;

   private final ValidationMessagePanel validationMessagePanel;
   private TargetContentsDisplay currentDisplay;
   private Provider<TargetContentsDisplay> displayProvider;
   private ArrayList<TargetContentsDisplay> displayList;
   private int currentEditorIndex = NO_OPEN_EDITOR;
   private ArrayList<ToggleEditor> currentEditors;
   private TransUnitsEditModel cellEditor;

   @Inject
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, final EventBus eventBus, final TableEditorMessages messages, final SourceContentsPresenter sourceContentsPresenter, UserConfigHolder configHolder, NavigationMessages navMessages, WorkspaceContext workspaceContext)
   {
      this.displayProvider = displayProvider;
      this.eventBus = eventBus;
      this.messages = messages;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.configHolder = configHolder;
      this.navMessages = navMessages;
      this.workspaceContext = workspaceContext;

      checkKey = new CheckKeyImpl(CheckKeyImpl.Context.Edit);
      validationMessagePanel = new ValidationMessagePanel(true, messages);
      eventBus.addHandler(UserConfigChangeEvent.getType(), this);
      eventBus.addHandler(UpdateValidationWarningsEvent.getType(), this);
      eventBus.addHandler(RequestValidationEvent.getType(), this);
      eventBus.addHandler(InsertStringInEditorEvent.getType(), this);
      eventBus.addHandler(CopyDataToEditorEvent.getType(), this);
   }

   private ToggleEditor getCurrentEditor()
   {
      return currentEditors.get(currentEditorIndex);
   }

   boolean isEditing()
   {
      return currentDisplay != null && currentDisplay.isEditing();
   }

   public void setToViewMode()
   {
      if (currentDisplay != null)
      {
         currentDisplay.setToView();
      }
   }

   public void setCurrentEditorText(String text)
   {
      if (getCurrentEditor() != null)
      {
         getCurrentEditor().setText(text);
      }
   }

   public void showEditors(int rowIndex)
   {
      currentDisplay = displayList.get(rowIndex);
      currentEditors = currentDisplay.getEditors();
      if (currentEditorIndex == LAST_INDEX)
      {
         currentEditorIndex = currentEditors.size() - 1;
      }

      if (currentEditorIndex != NO_OPEN_EDITOR && currentEditorIndex < currentEditors.size())
      {
         currentDisplay.openEditorAndCloseOthers(currentEditorIndex);
         Log.debug("show editors at row:" + rowIndex + " current editor:" + currentEditorIndex);
      }
   }

   public TargetContentsDisplay getNextTargetContentsDisplay(int rowIndex, TransUnit transUnit, String findMessages)
   {
      TargetContentsDisplay result = displayList.get(rowIndex);
      if (currentDisplay != null && currentDisplay != result)
      {
         currentDisplay.setToView();
      }
      result.setFindMessage(findMessages);
      result.setTargets(transUnit.getTargets());
      result.setSaveButtonTitle(decideButtonTitle());
      if (workspaceContext.isReadOnly())
      {
         Log.debug("read only mode. Hide buttons");
         result.showButtons(false);
      }
      return result;
   }

   private String decideButtonTitle()
   {
      return (configHolder.isButtonEnter()) ? navMessages.editSaveWithEnterShortcut() : navMessages.editSaveShortcut();
   }

   public void initWidgets(int pageSize)
   {
      displayList = Lists.newArrayList();
      for (int i = 0; i < pageSize; i++)
      {
         TargetContentsDisplay display = displayProvider.get();
         display.setListener(this);
         displayList.add(display);
      }
   }

   @Override
   public void validate(ToggleEditor editor)
   {
      eventBus.fireEvent(new RunValidationEvent(sourceContentsPresenter.getSelectedSource(), editor.getText(), false));
   }

   @Override
   public void saveAsApprovedAndMoveNext()
   {
      if (currentEditorIndex + 1 < currentEditors.size())
      {
         currentDisplay.openEditorAndCloseOthers(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         cellEditor.saveAndMoveRow(NavTransUnitEvent.NavigationType.NextEntry);
      }
   }

   @Override
   public void saveAsApprovedAndMovePrevious()
   {
      if (currentEditorIndex - 1 >= 0)
      {
         currentDisplay.openEditorAndCloseOthers(currentEditorIndex - 1);
         currentEditorIndex--;
      }
      else
      {
         currentEditorIndex = LAST_INDEX;
         cellEditor.saveAndMoveRow(NavTransUnitEvent.NavigationType.PrevEntry);
      }
   }

   @Override
   public void saveAsFuzzy()
   {
      Preconditions.checkState(cellEditor != null, "InlineTargetCellEditor must be set for triggering table save event");
      cellEditor.acceptFuzzyEdit();
   }

   @Override
   public boolean isDisplayButtons()
   {
      return configHolder.isDisplayButtons();
   }

   @Override
   public void onCancel(ToggleEditor editor)
   {
      editor.setViewMode(ViewMode.VIEW);
      if (cellEditor.getTargetCell().getTargets() != null && cellEditor.getTargetCell().getTargets().size() > editor.getIndex())
      {
         editor.setText(cellEditor.getTargetCell().getTargets().get(editor.getIndex()));
      }
      else
      {
         editor.setText(null);
      }
   }

   @Override
   public void copySource(ToggleEditor editor)
   {
      editor.setText(sourceContentsPresenter.getSelectedSource());
      editor.autoSize();
      validate(editor);
      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   @Override
   public void toggleView(ToggleEditor editor)
   {
      currentEditorIndex = editor.getIndex();
      if (currentEditors != null && currentEditors.contains(editor))
      {
         // still in the same trans unit. won't trigger transunit selection
         // or edit cell event
         currentDisplay.openEditorAndCloseOthers(currentEditorIndex);
      }
      else if (currentDisplay != null)
      {
         currentDisplay.setToView();
      }
      Log.debug("current display:" + currentDisplay);
      // else, it's clicking an editor outside current selection. the table
      // selection event will trigger and showEditors will take care of the rest
   }

   public ArrayList<String> getNewTargets()
   {
      return currentDisplay.getNewTargets();
   }

   @Override
   public void setValidationMessagePanel(ToggleEditor editor)
   {
      validationMessagePanel.clear();
      editor.addValidationMessagePanel(validationMessagePanel);
   }

   public void setCellEditor(TransUnitsEditModel cellEditor)
   {
      this.cellEditor = cellEditor;
   }

   @Override
   public void onValueChanged(UserConfigChangeEvent event)
   {
      String title = decideButtonTitle();
      for (TargetContentsDisplay display : displayList)
      {
         display.setSaveButtonTitle(title);
         display.showButtons(configHolder.isDisplayButtons());
      }
   }

   @Override
   public void onUpdate(UpdateValidationWarningsEvent event)
   {
      validationMessagePanel.setContent(event.getErrors());
   }

   @Override
   public void onRequestValidation(RequestValidationEvent event)
   {
      if (isEditing())
      {
         eventBus.fireEvent(new RunValidationEvent(sourceContentsPresenter.getSelectedSource(), getCurrentEditor().getText(), false));
      }
   }

   @Override
   public void onInsertString(InsertStringInEditorEvent event)
   {
      if (isEditing())
      {
         getCurrentEditor().insertTextInCursorPosition(event.getSuggestion());
         eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
         validate(getCurrentEditor());
      }
      else
      {
         eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnopened()));
      }
   }

   @Override
   public void onTransMemoryCopy(CopyDataToEditorEvent event)
   {
      if (isEditing())
      {
         getCurrentEditor().setText(event.getTargetResult());
         eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
         validate(getCurrentEditor());
      }
      else
      {
         eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnopened()));
      }
   }

   @Override
   public void onTextAreaKeyDown(KeyDownEvent event, ToggleEditor editor)
   {
      checkKey.init(event.getNativeEvent());

      if (checkKey.isCopyFromSourceKey())
      {
         copySource(editor);
      }
      else if (checkKey.isNextEntryKey())
      {
         // See editCell() for saving event
         saveAsApprovedAndMoveNext();
      }
      else if (checkKey.isPreviousEntryKey())
      {
         // See editCell() for saving event
         saveAsApprovedAndMovePrevious();
      }
      else if (checkKey.isNextStateEntryKey())
      {
         saveAsApprovedAndMoveNext();
      }
      else if (checkKey.isPreviousStateEntryKey())
      {
         saveAsApprovedAndMovePrevious();
      }
      else if (checkKey.isSaveAsFuzzyKey())
      {
         event.stopPropagation();
         event.preventDefault(); // stop browser save
         saveAsFuzzy();
      }
      else if (checkKey.isSaveAsApprovedKey(configHolder.isButtonEnter()))
      {
         event.stopPropagation();
         event.preventDefault();
         saveAsApprovedAndMoveNext();
      }
      else if (checkKey.isCloseEditorKey(configHolder.isButtonEsc()))
      {
         onCancel(editor);
      }
      else if (checkKey.isUserTyping() && !checkKey.isBackspace())
      {
         editor.growSize();
      }
      else if (checkKey.isUserTyping() && checkKey.isBackspace())
      {
         editor.shrinkSize(false);
      }
   }

   public void moveToNextState(NavTransUnitEvent.NavigationType nav)
   {
      cellEditor.savePendingChange(true);
      if (configHolder.isFuzzyAndUntranslated())
      {
         cellEditor.gotoFuzzyAndNewRow(nav);
      }
      else if (configHolder.isButtonUntranslated())
      {
         cellEditor.gotoNewRow(nav);
      }
      else if (configHolder.isButtonFuzzy())
      {
         cellEditor.gotoFuzzyRow(nav);
      }
   }

   public void saveAndMoveRow(NavTransUnitEvent.NavigationType nav)
   {
      cellEditor.saveAndMoveRow(nav);
   }

}
