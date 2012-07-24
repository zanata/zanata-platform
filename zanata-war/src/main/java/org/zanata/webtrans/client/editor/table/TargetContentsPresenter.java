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

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEventHandler;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorHandler;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RequestValidationEventHandler;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.keys.SurplusKeyListener;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.ui.ValidationMessagePanelDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;
import org.zanata.webtrans.shared.rpc.TransUnitEditResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
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
   private final KeyShortcutPresenter keyShortcutPresenter;
   private final UserSessionService sessionService;
   private final UserConfigHolder configHolder;

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
   private final UserWorkspaceContext userWorkspaceContext;
   private final CachingDispatchAsync dispatcher;

   private boolean enterSavesApprovedRegistered;
   private boolean escClosesEditorRegistered;

   private KeyShortcut enterSavesApprovedShortcut;
   private KeyShortcut enterTriggersAutoSizeShortcut;
   private KeyShortcut escClosesEditorShortcut;

   private HandlerRegistration enterSavesApprovedHandlerRegistration;
   private HandlerRegistration enterTriggersAutoSizeHandlerRegistration;
   private HandlerRegistration escClosesEditorHandlerRegistration;

   @Inject
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, final CachingDispatchAsync dispatcher, final Identity identity, final EventBus eventBus, final TableEditorMessages messages, final SourceContentsPresenter sourceContentsPresenter, final UserSessionService sessionService, final UserConfigHolder configHolder, UserWorkspaceContext userWorkspaceContext, Scheduler scheduler, ValidationMessagePanelDisplay validationMessagePanel, final KeyShortcutPresenter keyShortcutPresenter)
   {
      this.displayProvider = displayProvider;
      this.eventBus = eventBus;
      this.messages = messages;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.configHolder = configHolder;
      this.userWorkspaceContext = userWorkspaceContext;
      this.scheduler = scheduler;
      this.validationMessagePanel = validationMessagePanel;
      this.sessionService = sessionService;
      this.identity = identity;
      this.dispatcher = dispatcher;
      this.keyShortcutPresenter = keyShortcutPresenter;

      eventBus.addHandler(UserConfigChangeEvent.getType(), this);
      eventBus.addHandler(RequestValidationEvent.getType(), this);
      eventBus.addHandler(InsertStringInEditorEvent.getType(), this);
      eventBus.addHandler(CopyDataToEditorEvent.getType(), this);
      eventBus.addHandler(TransUnitEditEvent.getType(), this);
      eventBus.addHandler(EnableModalNavigationEvent.getType(), this);

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_1), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_1)), ShortcutContext.Edit, messages.copyFromTM(1), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            eventBus.fireEvent(new TransMemoryShortcutCopyEvent(0));
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_2), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_2)), ShortcutContext.Edit, messages.copyFromTM(2), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            eventBus.fireEvent(new TransMemoryShortcutCopyEvent(1));
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_3), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_3)), ShortcutContext.Edit, messages.copyFromTM(3), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            eventBus.fireEvent(new TransMemoryShortcutCopyEvent(2));
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_4), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_4)), ShortcutContext.Edit, messages.copyFromTM(4), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            eventBus.fireEvent(new TransMemoryShortcutCopyEvent(3));
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN), new Keys(Keys.ALT_KEY, 'K')), ShortcutContext.Edit, messages.moveToNextRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            moveNext(false);
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP), new Keys(Keys.ALT_KEY, 'J')), ShortcutContext.Edit, messages.moveToPreviousRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            movePrevious(false);
         }
      }));

      // Register shortcut ALT+(PageDown) to move next state entry - if modal
      // navigation is enabled
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEDOWN), ShortcutContext.Edit, messages.moveToNextStateRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (isModalNavEnabled)
            {
               moveToNextState(NavTransUnitEvent.NavigationType.NextEntry);
            }
         }
      }));

      // Register shortcut ALT+(PageUp) to move previous state entry - if modal
      // navigation is enabled
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEUP), ShortcutContext.Edit, messages.moveToPreviousStateRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (isModalNavEnabled)
            {
               moveToNextState(NavTransUnitEvent.NavigationType.PrevEntry);
            }
         }
      }));

      // Register shortcut CTRL+S to save as fuzzy
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.CTRL_KEY, 'S'), ShortcutContext.Edit, messages.saveAsFuzzy(), KeyEvent.KEY_DOWN, true, true, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            saveAsFuzzy();
         }
      }));

      KeyShortcutEventHandler saveAsApprovedKeyShortcutHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            saveAsApprovedAndMoveNext();
         }
      };

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER), ShortcutContext.Edit, messages.saveAsApproved(), KeyEvent.KEY_DOWN, true, true, saveAsApprovedKeyShortcutHandler));

      enterSavesApprovedShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Edit, messages.saveAsApproved(), KeyEvent.KEY_DOWN, true, true, saveAsApprovedKeyShortcutHandler);

      enterTriggersAutoSizeShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Edit, KeyShortcut.DO_NOT_DISPLAY_DESCRIPTION, KeyEvent.KEY_DOWN, false, false, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            getCurrentEditor().autoSizePlusOne();
         }
      });

      if (configHolder.isEnterSavesApproved())
      {
         enterSavesApprovedRegistered = true;
         enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
      }
      else
      {
         enterSavesApprovedRegistered = false;
         enterTriggersAutoSizeHandlerRegistration = keyShortcutPresenter.register(enterTriggersAutoSizeShortcut);
      }

      // Auto-size for any non-shortcut key presses
      keyShortcutPresenter.register(new SurplusKeyListener(KeyEvent.KEY_DOWN, ShortcutContext.Edit, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            getCurrentEditor().autoSize();
         }
      }));

      escClosesEditorShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ESCAPE), ShortcutContext.Edit, messages.closeEditor(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (configHolder.isEscClosesEditor() && !keyShortcutPresenter.getDisplay().isShowing())
            {
               onCancel();
            }
         }
      });

      if (configHolder.isEscClosesEditor())
      {
         escClosesEditorRegistered = true;
         escClosesEditorHandlerRegistration = keyShortcutPresenter.register(escClosesEditorShortcut);
      }
      else
      {
         escClosesEditorRegistered = false;
      }

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'G'), ShortcutContext.Edit, "Copy from source", new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (getCurrentEditor().isFocused())
            {
               copySource(getCurrentEditor());
            }
         }
      }));

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
      concealDisplay();
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
      revealDisplay();

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
      if (userWorkspaceContext.hasReadOnlyAccess())
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

   protected void moveNext(boolean forceSave)
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

   protected void movePrevious(boolean forceSave)
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
   public boolean isReadOnly()
   {
      return userWorkspaceContext.hasReadOnlyAccess();
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

      revealDisplay();

      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   @Override
   public void toggleView(final ToggleEditor editor)
   {
      currentEditorIndex = editor.getIndex();
      Log.debug("toggle view current editor index:" + currentEditorIndex);
      if (currentDisplay != null)
      {
         currentDisplay.focusEditor(currentEditorIndex);
      }

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

      boolean enterSavesApproved = configHolder.isEnterSavesApproved();
      if (enterSavesApproved != enterSavesApprovedRegistered)
      {
         if (enterSavesApproved)
         {
            if (enterTriggersAutoSizeHandlerRegistration != null)
            {
               enterTriggersAutoSizeHandlerRegistration.removeHandler();
            }
            enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
         }
         else
         {
            if (enterSavesApprovedHandlerRegistration != null)
            {
               enterSavesApprovedHandlerRegistration.removeHandler();
            }
            enterTriggersAutoSizeHandlerRegistration = keyShortcutPresenter.register(enterTriggersAutoSizeShortcut);
         }
         enterSavesApprovedRegistered = enterSavesApproved;
      }

      boolean escClosesEditor = configHolder.isEscClosesEditor();
      if (escClosesEditor != escClosesEditorRegistered)
      {
         if (escClosesEditor)
         {
            escClosesEditorHandlerRegistration = keyShortcutPresenter.register(escClosesEditorShortcut);
         }
         else
         {
            if (escClosesEditorHandlerRegistration != null)
            {
               escClosesEditorHandlerRegistration.removeHandler();
            }
         }
         escClosesEditorRegistered = escClosesEditor;
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
         revealDisplay();
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

   public void revealDisplay()
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.Edit, true);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, false);
   }

   public void concealDisplay()
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.Edit, false);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
   }

   public void addUndoLink(int row, UndoLink undoLink)
   {
      TargetContentsDisplay targetContentsDisplay = displayList.get(row);
      targetContentsDisplay.addUndo(undoLink);
   }
}
