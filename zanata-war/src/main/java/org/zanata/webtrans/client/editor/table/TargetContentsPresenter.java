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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
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
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.keys.SurplusKeyListener;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.TranslationHistoryPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
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

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.NextEntry;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.NextState;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.PrevEntry;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.PrevState;

@Singleton
// @formatter:off
public class TargetContentsPresenter implements
      TargetContentsDisplay.Listener,
      EnableModalNavigationEventHandler,
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
   private final KeyShortcutPresenter keyShortcutPresenter;
   private final TranslationHistoryPresenter historyPresenter;
   private final UserSessionService sessionService;
   private final UserConfigHolder configHolder;


   private final ValidationMessagePanelDisplay validationMessagePanel;
   private TargetContentsDisplay display;
   private Provider<TargetContentsDisplay> displayProvider;
   private List<TargetContentsDisplay> displayList = Collections.emptyList();
   private int currentEditorIndex = 0;
   private ArrayList<ToggleEditor> currentEditors;

   private boolean isModalNavEnabled = true;

   private final Identity identity;
   private final UserWorkspaceContext userWorkspaceContext;
   private TransUnitId currentTransUnitId;

   private boolean enterSavesApprovedRegistered;
   private boolean escClosesEditorRegistered;

   private KeyShortcut enterSavesApprovedShortcut;
   private KeyShortcut escClosesEditorShortcut;

   private HandlerRegistration enterSavesApprovedHandlerRegistration;
   private HandlerRegistration enterTriggersAutoSizeHandlerRegistration;
   private HandlerRegistration escClosesEditorHandlerRegistration;

   private final KeyShortcut nextStateShortcut;
   private final KeyShortcut prevStateShortcut;
   private String findMessage;

   @Inject
   //TODO too many constructor dependencies
   // @formatter:off
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, Identity identity, final EventBus eventBus,
                                  TableEditorMessages messages,
                                  SourceContentsPresenter sourceContentsPresenter,
                                  UserSessionService sessionService,
                                  final UserConfigHolder configHolder,
                                  UserWorkspaceContext userWorkspaceContext,
                                  ValidationMessagePanelDisplay validationMessagePanel,
                                  final KeyShortcutPresenter keyShortcutPresenter,
                                  TranslationHistoryPresenter historyPresenter)
   // @formatter:on
   {
      this.displayProvider = displayProvider;
      this.userWorkspaceContext = userWorkspaceContext;
      this.eventBus = eventBus;
      this.messages = messages;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.configHolder = configHolder;
      this.validationMessagePanel = validationMessagePanel;
      this.sessionService = sessionService;
      this.identity = identity;
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.historyPresenter = historyPresenter;
      this.historyPresenter.setCurrentValueHolder(this);

      bindEventHandlers();

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
            if (currentEditorIndex + 1 < currentEditors.size())
            {
               display.focusEditor(currentEditorIndex + 1);
               currentEditorIndex++;
            }
            else
            {
               currentEditorIndex = 0;
               eventBus.fireEvent(new NavTransUnitEvent(NextEntry));
            }
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP), new Keys(Keys.ALT_KEY, 'J')), ShortcutContext.Edit, messages.moveToPreviousRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (currentEditorIndex - 1 >= 0)
            {
               display.focusEditor(currentEditorIndex - 1);
               currentEditorIndex--;
            }
            else
            {
               currentEditorIndex = LAST_INDEX;
               eventBus.fireEvent(new NavTransUnitEvent(PrevEntry));
            }
         }
      }));

      // Register shortcut ALT+(PageDown) to move next state entry - if modal
      // navigation is enabled
      nextStateShortcut = new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEDOWN), ShortcutContext.Edit, messages.nextFuzzyOrUntranslated(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (isModalNavEnabled)
            {
               eventBus.fireEvent(new NavTransUnitEvent(NextState));
            }
         }
      });
      keyShortcutPresenter.register(nextStateShortcut);

      // Register shortcut ALT+(PageUp) to move previous state entry - if modal
      // navigation is enabled
      prevStateShortcut = new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEUP), ShortcutContext.Edit, messages.prevFuzzyOrUntranslated(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (isModalNavEnabled)
            {
               eventBus.fireEvent(new NavTransUnitEvent(PrevState));
            }
         }
      });
      keyShortcutPresenter.register(prevStateShortcut);

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

      if (configHolder.isEnterSavesApproved())
      {
         enterSavesApprovedRegistered = true;
         enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
      }
      else
      {
         enterSavesApprovedRegistered = false;
      }

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

   private void bindEventHandlers()
   {
      eventBus.addHandler(UserConfigChangeEvent.getType(), this);
      eventBus.addHandler(RequestValidationEvent.getType(), this);
      eventBus.addHandler(InsertStringInEditorEvent.getType(), this);
      eventBus.addHandler(CopyDataToEditorEvent.getType(), this);
      eventBus.addHandler(TransUnitEditEvent.getType(), this);
      eventBus.addHandler(EnableModalNavigationEvent.getType(), this);
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
   }

   private ToggleEditor getCurrentEditor()
   {
      return currentEditors.get(currentEditorIndex);
   }

   public boolean displayIsEditable()
   {
      return display != null && display.isEditing();
   }

   public void showEditors(int rowIndex, TransUnitId currentTransUnitId)
   {
      Log.info("enter show editor with rowIndex:" + rowIndex);
      display = displayList.get(rowIndex);
      currentEditors = display.getEditors();
      this.currentTransUnitId = currentTransUnitId;

      for (ToggleEditor editor : display.getEditors())
      {
         editor.clearTranslatorList();
         validate(editor);
      }
      revealDisplay();
      display.showButtons(isDisplayButtons());

      normaliseCurrentEditorIndex();

      if (!userWorkspaceContext.hasReadOnlyAccess())
      {
         validationMessagePanel.clear();
         display.focusEditor(currentEditorIndex);
         updateTranslators();
      }
      else
      {
         display.setToMode(ViewMode.VIEW);
      }
   }

   private void normaliseCurrentEditorIndex()
   {
      if (currentEditorIndex == LAST_INDEX)
      {
         currentEditorIndex = currentEditors.size() - 1;
      }
      else if (currentEditorIndex < 0 || currentEditorIndex >= currentEditors.size())
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
         updateEditorTranslatorList(event.getSelectedTransUnit().getId(), event.getPerson(), event.getEditorClientId());
      }
   }

   private void updateEditorTranslatorList(TransUnitId selectedTransUnitId, Person person, EditorClientId editorClientId)
   {
      if (!editorClientId.equals(identity.getEditorClientId()) && Objects.equal(currentTransUnitId, selectedTransUnitId))
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

   public void updateTranslators()
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

   //This method is used to  cancel edited change and set back view
   public TargetContentsDisplay setValue(TransUnit transUnit)
   {
      display.setFindMessage(this.findMessage);
      display.setValue(transUnit);
      return display;
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
      if (currentEditorIndex + 1 < currentEditors.size())
      {
         display.focusEditor(currentEditorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         currentEditorIndex = 0;
         eventBus.fireEvent(new TransUnitSaveEvent(getNewTargets(), ContentState.Approved, getCurrentTransUnitIdOrNull(), getCurrentVersionOrNull()));
         eventBus.fireEvent(new NavTransUnitEvent(NextEntry));
      }
   }

   @Override
   public void saveAsFuzzy()
   {
      eventBus.fireEvent(new TransUnitSaveEvent(getNewTargets(), ContentState.NeedReview, getCurrentTransUnitIdOrNull(), getCurrentVersionOrNull()));
   }

   private Integer getCurrentVersionOrNull()
   {
      return display == null ? null : display.getVerNum();
   }

   private TransUnitId getCurrentTransUnitIdOrNull()
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
   public void showHistory()
   {
      TransUnitId transUnitId = cellEditor.getTargetCell().getId();
      historyPresenter.showTranslationHistory(transUnitId);
   }

   @Override
   public void onCancel()
   {
      eventBus.fireEvent(TransUnitSaveEvent.CANCEL_EDIT_EVENT);
   }

   @Override
   public void copySource(ToggleEditor editor)
   {
      Log.debug("copy source");
      currentEditorIndex = editor.getIndex();
      display.showButtons(isDisplayButtons());
      editor.setTextAndValidate(sourceContentsPresenter.getSelectedSource());
      editor.setFocus();

      revealDisplay();

      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   @Override
   public void toggleView(final ToggleEditor editor)
   {
      currentEditorIndex = editor.getIndex();
   }

   public ArrayList<String> getNewTargets()
   {
      return display == null ? null : display.getNewTargets();
   }

   @Override
   public void setValidationMessagePanel(ToggleEditor editor)
   {
      validationMessagePanel.clear();
      editor.addValidationMessagePanel(validationMessagePanel);
   }

   @Override
   public void onValueChanged(UserConfigChangeEvent event)
   {
      // TODO optimise a bit. If some config hasn't changed or not relevant in
      // this context, don't bother doing anything
      display.showButtons(configHolder.isDisplayButtons());
      for (ToggleEditor editor : display.getEditors())
      {
         editor.showCopySourceButton(configHolder.isDisplayButtons());
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


      if (configHolder.isButtonFuzzy() && !configHolder.isButtonUntranslated())
      {
         nextStateShortcut.setDescription(messages.nextFuzzy());
         prevStateShortcut.setDescription(messages.prevFuzzy());
      }
      else if (configHolder.isButtonUntranslated() && !configHolder.isButtonFuzzy())
      {
         nextStateShortcut.setDescription(messages.nextUntranslated());
         prevStateShortcut.setDescription(messages.prevUntranslated());
      }
      else if (configHolder.isButtonUntranslated() && configHolder.isButtonFuzzy())
      {
         nextStateShortcut.setDescription(messages.nextFuzzyOrUntranslated());
         prevStateShortcut.setDescription(messages.nextFuzzyOrUntranslated());
      }

   }

   @Override
   public void onRequestValidation(RequestValidationEvent event)
   {
      if (Objects.equal(sourceContentsPresenter.getCurrentTransUnitIdOrNull(), currentTransUnitId))
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
         targetContentsDisplay.setFindMessage(message);
      }
   }

   public void updateRow(int rowNum, TransUnit updatedTransUnit)
   {
      TargetContentsDisplay contentsDisplay = displayList.get(rowNum);
      contentsDisplay.setValue(updatedTransUnit);
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
      if (displayIsEditable() && userWorkspaceContext.hasReadOnlyAccess())
      {
         Log.debug("from editable to readonly");
         for (TargetContentsDisplay targetContentsDisplay : displayList)
         {
            targetContentsDisplay.setToMode(ViewMode.VIEW);
            targetContentsDisplay.showButtons(false);
         }
      }
      else if (!displayIsEditable() && !userWorkspaceContext.hasReadOnlyAccess())
      {
         Log.debug("from readonly mode to writable");
         for (TargetContentsDisplay targetContentsDisplay : displayList)
         {
            targetContentsDisplay.setToMode(ViewMode.EDIT);
            targetContentsDisplay.showButtons(isDisplayButtons());
         }
      }

   }
}
