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
package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.CheckKey;
import org.zanata.webtrans.client.editor.CheckKeyImpl;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEventHandler;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RequestValidationEventHandler;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.ValidationMessagePanelDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;
import org.zanata.webtrans.shared.rpc.TransUnitEditResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener, EnableModalNavigationEventHandler, TransUnitEditEventHandler, UserConfigChangeHandler, RequestValidationEventHandler, InsertStringInEditorHandler, CopyDataToEditorHandler
{
   public static final int NO_OPEN_EDITOR = -1;
   private static final int LAST_INDEX = -2;
   private final EventBus eventBus;
   private final TableEditorMessages messages;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final UserSessionService sessionService;
   private final UserConfigHolder configHolder;

   private final CheckKey checkKey;
   private WorkspaceContext workspaceContext;
   private Scheduler scheduler;

   private final ValidationMessagePanelDisplay validationMessagePanel;
   private TargetContentsDisplay currentDisplay;
   private Provider<TargetContentsDisplay> displayProvider;
   private ArrayList<TargetContentsDisplay> displayList = Lists.newArrayList();
   private int currentEditorIndex = NO_OPEN_EDITOR;
   private ArrayList<ToggleEditor> currentEditors;
   private TransUnitsEditModel cellEditor;

   private boolean isModalNavEnabled;

   private final Identity identity;

   private final CachingDispatchAsync dispatcher;

   @Inject
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, final CachingDispatchAsync dispatcher, final Identity identity, final EventBus eventBus, final TableEditorMessages messages, final SourceContentsPresenter sourceContentsPresenter, final UserSessionService sessionService, UserConfigHolder configHolder, WorkspaceContext workspaceContext, Scheduler scheduler, ValidationMessagePanelDisplay validationMessagePanel)
   {
      this.displayProvider = displayProvider;
      this.eventBus = eventBus;
      this.messages = messages;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.configHolder = configHolder;
      this.workspaceContext = workspaceContext;
      this.scheduler = scheduler;
      this.validationMessagePanel = validationMessagePanel;
      this.sessionService = sessionService;
      this.identity = identity;
      this.dispatcher = dispatcher;

      checkKey = new CheckKeyImpl(ShortcutContext.Edit);
      eventBus.addHandler(UserConfigChangeEvent.getType(), this);
      eventBus.addHandler(RequestValidationEvent.getType(), this);
      eventBus.addHandler(InsertStringInEditorEvent.getType(), this);
      eventBus.addHandler(CopyDataToEditorEvent.getType(), this);
      eventBus.addHandler(TransUnitEditEvent.getType(), this);
      eventBus.addHandler(EnableModalNavigationEvent.getType(), this);
   }

   private ToggleEditor getCurrentEditor()
   {
      return currentEditors.get(currentEditorIndex);
   }

   public boolean isEditing()
   {
      return currentDisplay != null && currentDisplay.isEditing();
   }

   public void setToViewMode()
   {
      if (currentDisplay != null)
      {
         currentDisplay.setToView();
         currentDisplay.showButtons(false);
      }
   }

   private void fireTransUnitEditAction()
   {
      dispatcher.execute(new TransUnitEditAction(identity.getPerson(), cellEditor.getTargetCell()), new AsyncCallback<TransUnitEditResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
         }

         @Override
         public void onSuccess(TransUnitEditResult result)
         {
         }
      });
   }
   public void showEditors(int rowIndex, int editorIndex)
   {
      Log.debug("enter show editor with editor index:" + editorIndex + " current editor index:" + currentEditorIndex);
      currentDisplay = displayList.get(rowIndex);
      currentEditors = currentDisplay.getEditors();

      for (ToggleEditor editor : currentDisplay.getEditors())
      {
         editor.setViewMode(ToggleEditor.ViewMode.EDIT);
         editor.clearTranslatorList();
         validate(editor);
      }
      
      fireTransUnitEditAction();
      
      if (configHolder.isDisplayButtons())
      {
         currentDisplay.showButtons(true);
      }

      if (editorIndex != NO_OPEN_EDITOR)
      {
         currentEditorIndex = editorIndex;
      }
      else if (currentEditorIndex == LAST_INDEX)
      {
         currentEditorIndex = currentEditors.size() - 1;
      }
      else
      {
         currentEditorIndex = 0;
      }

      if (currentEditorIndex != NO_OPEN_EDITOR && currentEditorIndex < currentEditors.size())
      {
         validationMessagePanel.clear();
         currentDisplay.focusEditor(currentEditorIndex);
         Log.debug("show editors at row:" + rowIndex + " current editor:" + currentEditorIndex);
         updateTranslators();
      }
   }

   @Override
   public void onTransUnitEdit(final TransUnitEditEvent event)
   {
      if (event.getSelectedTransUnit() != null)
      {
         updateEditorTranslatorList(event.getSelectedTransUnit().getId(), event.getPerson(), event.getEditorClientId());
      }
   }

   private void updateEditorTranslatorList(TransUnitId selectedTransUnitId, Person person, EditorClientId editorClientId)
   {
      if (cellEditor.getTargetCell() != null)
      {
         if (!editorClientId.equals(identity.getEditorClientId()) && cellEditor.getTargetCell().getId().equals(selectedTransUnitId))
         {
            for (ToggleEditor editor : currentEditors)
            {
               editor.addTranslator(person.getName(), sessionService.getColor(editorClientId.getValue()));
            }
         }
         else
         {
            for (ToggleEditor editor : currentEditors)
            {
               editor.removeTranslator(person.getName(), sessionService.getColor(editorClientId.getValue()));
            }
         }
      }
   }

   public void updateTranslators()
   {
      if (isEditing())
      {
         for (ToggleEditor editor : currentEditors)
         {
            editor.clearTranslatorList();
         }

         for (Map.Entry<EditorClientId, UserPanelSessionItem> entry : sessionService.getUserSessionMap().entrySet())
         {
            if (entry.getValue().getSelectedTransUnit() != null)
            {
               updateEditorTranslatorList(entry.getValue().getSelectedTransUnit().getId(), entry.getValue().getPerson(), entry.getKey());
            }
         }
      }
   }

   public TargetContentsDisplay getNextTargetContentsDisplay(int rowIndex, TransUnit transUnit, String findMessages)
   {
      TargetContentsDisplay result = displayList.get(rowIndex);
      result.setFindMessage(findMessages);
      result.setTargets(transUnit.getTargets());
      if (workspaceContext.isReadOnly())
      {
         Log.debug("read only mode. Hide buttons");
         result.showButtons(false);
      }
      return result;
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
      if (editor.getIndex() != currentEditorIndex)
      {
         // the timer may kickoff validation event when user press ctrl + enter
         // etc to move editor around. We don't want to reset currentEditorIndex
         // for such event.
         return;
      }
      currentEditorIndex = editor.getIndex();
      RunValidationEvent event = new RunValidationEvent(sourceContentsPresenter.getSelectedSource(), editor.getText(), false);
      event.addWidget(validationMessagePanel);
      event.addWidget(editor);
      eventBus.fireEvent(event);
   }

   @Override
   public void saveAsApprovedAndMoveNext()
   {
      moveNext(true);
   }

   private void moveNext(boolean forceSave)
   {
      if (currentEditorIndex + 1 < currentEditors.size())
      {
         currentDisplay.focusEditor(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         if (forceSave)
         {
            cellEditor.acceptEdit();
            cellEditor.setRowValueOverride(cellEditor.getCurrentRow(), cellEditor.getTargetCell());
         }
         scheduler.scheduleDeferred(new Scheduler.ScheduledCommand()
         {
            @Override
            public void execute()
            {
               cellEditor.saveAndMoveRow(NavTransUnitEvent.NavigationType.NextEntry);
            }
         });
      }
   }

   @Override
   public void saveAsApprovedAndMovePrevious()
   {
      movePrevious(true);
   }

   private void movePrevious(boolean forceSave)
   {
      if (currentEditorIndex - 1 >= 0)
      {
         currentDisplay.focusEditor(currentEditorIndex - 1);
         currentEditorIndex--;
      }
      else
      {
         currentEditorIndex = LAST_INDEX;
         if (forceSave)
         {
            cellEditor.acceptEdit();
            cellEditor.setRowValueOverride(cellEditor.getCurrentRow(), cellEditor.getTargetCell());
         }
         scheduler.scheduleDeferred(new Scheduler.ScheduledCommand()
         {
            @Override
            public void execute()
            {
               cellEditor.saveAndMoveRow(NavTransUnitEvent.NavigationType.PrevEntry);
            }
         });
      }
   }

   @Override
   public void saveAsFuzzy()
   {
      Preconditions.checkState(cellEditor != null, "InlineTargetCellEditor must be set for triggering table save event");
      cellEditor.acceptFuzzyEdit();
      cellEditor.setRowValueOverride(cellEditor.getCurrentRow(), cellEditor.getTargetCell());
      cellEditor.gotoCurrentRow(true);
   }

   @Override
   public boolean isDisplayButtons()
   {
      return configHolder.isDisplayButtons();
   }

   @Override
   public void onCancel()
   {
      List<String> targets = cellEditor.getTargetCell().getTargets();
      for (ToggleEditor editor : currentEditors)
      {
         String content = null;
         if (targets != null && targets.size() > editor.getIndex())
         {
            content = targets.get(editor.getIndex());
         }
         editor.setTextAndValidate(content);
      }
      setToViewMode();
   }

   @Override
   public void copySource(ToggleEditor editor)
   {
      Log.debug("copy source");
      currentEditorIndex = editor.getIndex();
      currentDisplay.showButtons(true);
      editor.setTextAndValidate(sourceContentsPresenter.getSelectedSource());
      editor.setViewMode(ViewMode.EDIT);
      editor.autoSize();
      editor.setFocus();
      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   @Override
   public void toggleView(final ToggleEditor editor)
   {
      // this will get deferred execution since we want to trigger table
      // selection event first
      scheduler.scheduleDeferred(new Scheduler.ScheduledCommand()
      {
         @Override
         public void execute()
         {
            currentEditorIndex = editor.getIndex();
            Log.debug("toggle view current editor index:" + currentEditorIndex);
            if (currentDisplay != null)
            {
               currentDisplay.focusEditor(currentEditorIndex);
            }
         }
      });

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

   // TODO InlineTargetCellEditor is not managed by gin. Therefore this can't be
   // injected
   public void setCellEditor(TransUnitsEditModel cellEditor)
   {
      this.cellEditor = cellEditor;
   }

   @Override
   public void onValueChanged(UserConfigChangeEvent event)
   {
      // TODO optimise a bit. If some config hasn't changed or not relevant in
      // this context, don't bother doing anything
      for (TargetContentsDisplay display : displayList)
      {
         display.showButtons(configHolder.isDisplayButtons());
         for (ToggleEditor editor : display.getEditors())
         {
            editor.showCopySourceButton(configHolder.isDisplayButtons());
         }
      }
   }

   @Override
   public void onRequestValidation(RequestValidationEvent event)
   {
      if (isEditing())
      {
         for (ToggleEditor editor : currentDisplay.getEditors())
         {
            editor.setViewMode(ToggleEditor.ViewMode.EDIT);
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
   public void onTransMemoryCopy(final CopyDataToEditorEvent event)
   {
      copyTextWhenIsEditing(event.getTargetResult(), false);
   }

   private void copyTextWhenIsEditing(List<String> contents, boolean isInsertText)
   {
      if (!isEditing())
      {
         eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnopened()));
         return;
      }

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

   @Override
   public void onEditorKeyDown(KeyDownEvent event, ToggleEditor editor)
   {
      checkKey.init(event.getNativeEvent());

      if (checkKey.isCopyFromSourceKey())
      {
         copySource(editor);
      }
      else if (checkKey.isNextEntryKey())
      {
         moveNext(false);
      }
      else if (checkKey.isPreviousEntryKey())
      {
         movePrevious(false);
      }
      else if (checkKey.isNextStateEntryKey() && isModalNavEnabled)
      {
         moveToNextState(NavTransUnitEvent.NavigationType.NextEntry);
      }
      else if (checkKey.isPreviousStateEntryKey() && isModalNavEnabled)
      {
         moveToNextState(NavTransUnitEvent.NavigationType.PrevEntry);
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
         onCancel();
      }
      else if (checkKey.isUserTyping() && checkKey.isEnterKey())
      {
         //because enter itself will increase one line
         editor.autoSizePlusOne();
      }
      else if (checkKey.isUserTyping())
      {
         editor.autoSize();
      }
   }

   public void moveToNextState(final NavTransUnitEvent.NavigationType nav)
   {
      cellEditor.savePendingChange(true);
      scheduler.scheduleDeferred(new Scheduler.ScheduledCommand()
      {
         @Override
         public void execute()
         {
            goToRowWithState(nav);
         }
      });
   }

   private void goToRowWithState(NavTransUnitEvent.NavigationType nav)
   {
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

   @Override
   public void onEnable(EnableModalNavigationEvent event)
   {
      isModalNavEnabled = event.isEnable();
   }
}
