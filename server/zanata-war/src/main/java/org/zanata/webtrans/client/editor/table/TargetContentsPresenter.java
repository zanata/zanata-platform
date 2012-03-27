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
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ToggleEditor.ViewMode;
import org.zanata.webtrans.client.ui.ValidationMessagePanel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener
{
   private static final int NO_OPEN_EDITOR = -1;
   private final EventBus eventBus;
   private final TableEditorMessages messages;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final ValidationMessagePanel validationMessagePanel;

   private TargetContentsDisplay currentDisplay;
   private Provider<TargetContentsDisplay> displayProvider;
   private ArrayList<TargetContentsDisplay> displayList;
   private int currentEditorIndex = NO_OPEN_EDITOR;
   private ArrayList<ToggleEditor> currentEditors;
   private TransUnitsEditModel cellEditor;

   @Inject
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, final EventBus eventBus, final TableEditorMessages messages, final SourceContentsPresenter sourceContentsPresenter)
   {
      this.displayProvider = displayProvider;
      this.eventBus = eventBus;
      this.messages = messages;
      this.sourceContentsPresenter = sourceContentsPresenter;

      validationMessagePanel = new ValidationMessagePanel(true, messages);

      eventBus.addHandler(UpdateValidationWarningsEvent.getType(), new UpdateValidationWarningsEventHandler()
      {
         @Override
         public void onUpdate(UpdateValidationWarningsEvent event)
         {
            validationMessagePanel.setContent(event.getErrors());
         }
      });

      eventBus.addHandler(RequestValidationEvent.getType(), new RequestValidationEventHandler()
      {
         @Override
         public void onRequestValidation(RequestValidationEvent event)
         {
            if (isEditing())
            {
               eventBus.fireEvent(new RunValidationEvent(sourceContentsPresenter.getSelectedSource(),
                     getCurrentEditor().getText(), false));
            }
         }
      });

      eventBus.addHandler(InsertStringInEditorEvent.getType(), new InsertStringInEditorHandler()
      {
         @Override
         public void onInsertString(InsertStringInEditorEvent event)
         {
            if (isEditing())
            {
               getCurrentEditor().insertTextInCursorPosition(event.getSuggestion());
               eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
            }
            else
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnopened()));
            }
         }
      });

      eventBus.addHandler(CopyDataToEditorEvent.getType(), new CopyDataToEditorHandler()
      {
         @Override
         public void onTransMemoryCopy(CopyDataToEditorEvent event)
         {
            if (isEditing())
            {
               getCurrentEditor().setText(event.getTargetResult());
               eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
            }
            else
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnopened()));
            }
         }
      });
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
      TargetContentsDisplay previousDisplay = currentDisplay;
      if (previousDisplay != null)
      {
         previousDisplay.setToView();
      }
      currentDisplay = displayList.get(rowIndex);
      if (previousDisplay != currentDisplay)
      {
//         currentEditor = null;
         currentEditorIndex = NO_OPEN_EDITOR;
      }
      currentEditors = currentDisplay.getEditors();

      if (currentEditorIndex != NO_OPEN_EDITOR)
      {
         currentDisplay.openEditorAndCloseOthers(currentEditorIndex);
      	 Log.info("show editors at row:" + rowIndex + " current editor:" + currentEditorIndex);
      }
   }

   public TargetContentsDisplay getNextTargetContentsDisplay(int rowIndex, TransUnit transUnit)
   {
      TargetContentsDisplay result = displayList.get(rowIndex);
      if (currentDisplay != null && currentDisplay != result)
      {
         currentDisplay.setToView();
      }
     
      result.setTargets(transUnit.getTargets());
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

   public TargetContentsDisplay getCurrentDisplay()
   {
      return currentDisplay;
   }

   @Override
   public void validate(ToggleEditor editor)
   {
      eventBus.fireEvent(new RunValidationEvent(sourceContentsPresenter.getSelectedSource(), editor.getText(), false));
   }

   @Override
   public void saveAsApproved(ToggleEditor editor)
   {
      int editorIndex = currentEditors.indexOf(editor);
      if (editorIndex + 1 < currentEditors.size())
      {
         currentDisplay.openEditorAndCloseOthers(editorIndex + 1);
         currentEditorIndex++;
      }
      else
      {
         eventBus.fireEvent(new NavTransUnitEvent(NavTransUnitEvent.NavigationType.NextEntry));
      }
   }

   @Override
   public void saveAsFuzzy(ToggleEditor editor)
   {
      Preconditions.checkState(cellEditor != null, "InlineTargetCellEditor must be set for triggering table save event");
      cellEditor.acceptFuzzyEdit();
   }

   @Override
   public void onCancel(ToggleEditor editor)
   {
      editor.setViewMode(ViewMode.VIEW);
      editor.setText(cellEditor.getTargetCell().getTargets().get(editor.getIndex()));
   }

   @Override
   public void copySource(ToggleEditor editor)
   {
      editor.setText(sourceContentsPresenter.getSelectedSource());
      editor.autoSize();
      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
   }

   @Override
   public void toggleView(ToggleEditor editor)
   {
      currentEditorIndex = editor.getIndex();
      if (currentEditors.contains(editor))
      {
         //still in the same trans unit. won't trigger transunit selection or edit cell event
         currentDisplay.openEditorAndCloseOthers(currentEditorIndex);
      }
      Log.info("current display:" + currentDisplay);
      //else, it's clicking an editor outside current selection. the table selection event will trigger and showEditors will take care of the rest
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
}
