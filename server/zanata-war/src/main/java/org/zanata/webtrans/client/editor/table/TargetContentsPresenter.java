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

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.customware.gwt.presenter.client.EventBus;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.shared.model.TransUnit;

import javax.inject.Provider;
import java.util.List;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener
{

   private TargetContentsDisplay currentDisplay;
   private Provider<TargetContentsDisplay> displayProvider;
   private EventBus eventBus;
   private SourceContentsPresenter sourceContentsPresenter;
   private List<TargetContentsDisplay> displayList;
   private ToggleEditor currentEditor;
   private List<ToggleEditor> currentEditors;
   //TODO this is really a hacky way of indicating InlineTargetCellEditor we are not meant to edit the cell, just clicking the buttons
   private boolean isClickingButtons;

   @Inject
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, EventBus eventBus, SourceContentsPresenter sourceContentsPresenter)
   {
      this.displayProvider = displayProvider;
      this.eventBus = eventBus;
      this.sourceContentsPresenter = sourceContentsPresenter;
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
      if (currentEditor != null)
      {
         currentEditor.setText(text);
      }
   }

   public void insertTextInCursorPosition(String text)
   {
      // TODO implement
      // throw new UnsupportedOperationException("Implement me!");
   }

   public void showEditors(int rowIndex)
   {
      Log.info("show editors at row:" + rowIndex);
      currentDisplay = displayList.get(rowIndex);
      currentEditors = currentDisplay.getEditors();
      currentEditor = currentEditors.get(0);
      currentEditor.setViewMode(ToggleEditor.ViewMode.EDIT);
   }

   public TargetContentsDisplay getNextTargetContentsDisplay(int rowIndex, TransUnit transUnit)
   {
      TargetContentsDisplay result = displayList.get(rowIndex);
      result.setTargets(transUnit.getTargets());
      currentDisplay = result;
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
      // eventBus.fireEvent(new RunValidationEvent(id, ));
   }

   @Override
   public void saveAsApproved(ToggleEditor editor)
   {
      isClickingButtons = true;
      // TODO we should get new value out and save
      currentDisplay.setToView();
      int editorIndex = currentEditors.indexOf(editor);
      if (editorIndex + 1 < currentEditors.size())
      {
         currentEditor = currentEditors.get(editorIndex + 1);
         currentEditor.setViewMode(ToggleEditor.ViewMode.EDIT);
      }
      else
      {
         // TODO if it's out of current editor index, we should move to next row
         currentDisplay = null;
         currentEditors = null;
         eventBus.fireEvent(new NavTransUnitEvent(NavTransUnitEvent.NavigationType.NextEntry));
      }
   }

   @Override
   public void copySource(ToggleEditor editor)
   {
      editor.setText(sourceContentsPresenter.getSelectedSource());
      editor.autoSize();
   }

   @Override
   public void toggleView(ToggleEditor editor)
   {
      currentDisplay.setToView();
      editor.setViewMode(ToggleEditor.ViewMode.EDIT);
      currentEditor = editor;
   }

   public boolean isClickingButtons()
   {
      return isClickingButtons;
   }
}
