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
import java.util.Map;
import java.util.NoSuchElementException;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.CopyDataToEditorHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
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
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
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
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.NextEntry;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.NextState;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.PrevEntry;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.PrevState;

@Singleton
// @formatter:off
public class TargetContentsPresenter implements
      TargetContentsDisplay.Listener,
      TransUnitEditEventHandler,
      UserConfigChangeHandler,
      RequestValidationEventHandler,
      InsertStringInEditorHandler,
      CopyDataToEditorHandler,
      WorkspaceContextUpdateEventHandler,
      ExitWorkspaceEventHandler
// @formatter:on
{
   private static final int LAST_INDEX = -2;
   private final EventBus eventBus;
   private final TableEditorMessages messages;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final KeyShortcutPresenter keyShortcutPresenter;
   private final TranslationHistoryPresenter historyPresenter;
   private final Provider<TargetContentsDisplay> displayProvider;
   private final EditorTranslators editorTranslators;
   private final UserConfigHolder configHolder;
   private final UserWorkspaceContext userWorkspaceContext;

   private TargetContentsDisplay display;
   private List<TargetContentsDisplay> displayList = Collections.emptyList();
   private int currentEditorIndex = 0;
   private List<ToggleEditor> currentEditors = Collections.emptyList();

   private KeyShortcut enterSavesApprovedShortcut;
   private KeyShortcut escClosesEditorShortcut;

   private HandlerRegistration enterSavesApprovedHandlerRegistration;
   private HandlerRegistration escClosesEditorHandlerRegistration;

   private final KeyShortcut nextStateShortcut;
   private final KeyShortcut prevStateShortcut;
   private String findMessage;
   private UserConfigHolder.ConfigurationState configuration;
   private TransUnitId currentTransUnitId;

   @Inject
   //TODO too many constructor dependencies
   // @formatter:off
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, EditorTranslators editorTranslators, final EventBus eventBus,
                                  TableEditorMessages messages,
                                  SourceContentsPresenter sourceContentsPresenter,
                                  final UserConfigHolder configHolder,
                                  UserWorkspaceContext userWorkspaceContext,
                                  final KeyShortcutPresenter keyShortcutPresenter,
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
      configuration = configHolder.getState();
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.historyPresenter = historyPresenter;
      this.historyPresenter.setCurrentValueHolder(this);

      bindEventHandlers();

      // TODO extract all key shortcuts to a separate class
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
               savePendingChangesIfApplicable();
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
               savePendingChangesIfApplicable();
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
            savePendingChangesIfApplicable();
            eventBus.fireEvent(new NavTransUnitEvent(NextState));
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
            savePendingChangesIfApplicable();
            eventBus.fireEvent(new NavTransUnitEvent(PrevState));
         }
      });
      keyShortcutPresenter.register(prevStateShortcut);

      // Register shortcut CTRL+S to save as fuzzy
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.CTRL_KEY, 'S'), ShortcutContext.Edit, messages.saveAsFuzzy(), KeyEvent.KEY_DOWN, true, true, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            saveAsFuzzy(currentTransUnitId);
         }
      }));

      KeyShortcutEventHandler saveAsApprovedKeyShortcutHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            saveAsApprovedAndMoveNext(currentTransUnitId);
         }
      };

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER), ShortcutContext.Edit, messages.saveAsApproved(), KeyEvent.KEY_DOWN, true, true, saveAsApprovedKeyShortcutHandler));

      enterSavesApprovedShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Edit, messages.saveAsApproved(), KeyEvent.KEY_DOWN, true, true, saveAsApprovedKeyShortcutHandler);

      if (configuration.isEnterSavesApproved())
      {
         enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
      }

      escClosesEditorShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ESCAPE), ShortcutContext.Edit, messages.closeEditor(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (configuration.isEscClosesEditor() && !keyShortcutPresenter.getDisplay().isShowing())
            {
               onCancel(currentTransUnitId);
            }
         }
      });

      if (configuration.isEscClosesEditor())
      {
         escClosesEditorHandlerRegistration = keyShortcutPresenter.register(escClosesEditorShortcut);
      }

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'G'), ShortcutContext.Edit, messages.copyFromSource(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (getCurrentEditor().isFocused())
            {
               copySource(getCurrentEditor(), currentTransUnitId);
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
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
      eventBus.addHandler(ExitWorkspaceEvent.getType(), this);
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
      return display != null && !Objects.equal(display.getCachedTargets(), display.getNewTargets());
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
         editorTranslators.updateTranslator(currentEditors, currentTransUnitId);
      }
   }

   @Override
   public void onExitWorkspace(ExitWorkspaceEvent event)
   {
      editorTranslators.clearTranslatorList(currentEditors);
      editorTranslators.updateTranslator(currentEditors, currentTransUnitId);
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
         eventBus.fireEvent(new NavTransUnitEvent(NextEntry));
      }
   }

   @Override
   public void saveAsFuzzy(TransUnitId transUnitId)
   {
      ensureRowSelection(transUnitId);
      saveCurrent(ContentState.NeedReview);
   }

   public TransUnitId getCurrentTransUnitIdOrNull()
   {
      return currentTransUnitId;
   }

   @Override
   public boolean isDisplayButtons()
   {
      return configuration.isDisplayButtons() && !userWorkspaceContext.hasReadOnlyAccess();
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
      if (!Objects.equal(currentTransUnitId, transUnitId))
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

   public List<String> getNewTargets()
   {
      return display == null ? null : display.getNewTargets();
   }

   @Override
   public void onValueChanged(UserConfigChangeEvent event)
   {
      UserConfigHolder.ConfigurationState oldState = configuration;
      configuration = configHolder.getState();

      // If some config hasn't changed or not relevant in
      // this context, don't bother doing anything
      changeDisplayButtons(oldState);
      changeEnterSavesApproved(oldState);
      changeEscCloseEditor(oldState);
      changeNavShortcutDescription(oldState);
   }

   private void changeDisplayButtons(UserConfigHolder.ConfigurationState oldState)
   {
      if (oldState.isDisplayButtons() != configuration.isDisplayButtons())
      {
         for (TargetContentsDisplay contentsDisplay : displayList)
         {
            contentsDisplay.showButtons(configuration.isDisplayButtons());
         }
      }
   }

   private void changeEnterSavesApproved(UserConfigHolder.ConfigurationState oldState)
   {
      if (oldState.isEnterSavesApproved() != configuration.isEnterSavesApproved())
      {
         boolean enterSavesApproved = configuration.isEnterSavesApproved();
         if (enterSavesApproved)
         {
            enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
         }
         else
         {
            if (enterSavesApprovedHandlerRegistration != null)
            {
               enterSavesApprovedHandlerRegistration.removeHandler();
            }
         }
      }
   }

   private void changeEscCloseEditor(UserConfigHolder.ConfigurationState oldState)
   {
      if (oldState.isEscClosesEditor() != configuration.isEscClosesEditor())
      {
         boolean escClosesEditor = configuration.isEscClosesEditor();
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
      }
   }

   private void changeNavShortcutDescription(UserConfigHolder.ConfigurationState oldState)
   {
      if (oldState.getNavOption() != configuration.getNavOption())
      {
         switch (configuration.getNavOption())
         {
            case FUZZY_UNTRANSLATED:
               nextStateShortcut.setDescription(messages.nextFuzzyOrUntranslated());
               prevStateShortcut.setDescription(messages.nextFuzzyOrUntranslated());
               break;
            case FUZZY:
               nextStateShortcut.setDescription(messages.nextFuzzy());
               prevStateShortcut.setDescription(messages.prevFuzzy());
               break;
            case UNTRANSLATED:
               nextStateShortcut.setDescription(messages.nextUntranslated());
               prevStateShortcut.setDescription(messages.prevUntranslated());
               break;
         }
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
