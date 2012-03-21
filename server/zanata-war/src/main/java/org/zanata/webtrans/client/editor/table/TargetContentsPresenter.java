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

import java.util.Iterator;
import java.util.List;

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.presenter.SourcePanelPresenter;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener
{

   private TargetContentsDisplay currentDisplay;
   private Provider<TargetContentsDisplay> displayProvider;
   private EventBus eventBus;
   private SourcePanelPresenter sourcePanelPresenter;
   private List<TargetContentsDisplay> displayList;
   private Iterator<ToggleEditor> currentEditorIterator;
   private ToggleEditor currentEditor;

   @Inject
   public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, EventBus eventBus, SourcePanelPresenter sourcePanelPresenter)
   {
      this.displayProvider = displayProvider;
      this.eventBus = eventBus;
      this.sourcePanelPresenter = sourcePanelPresenter;
   }

   boolean isEditing()
   {
      return currentDisplay.isEditing();
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

   public void showEditors(int curRow)
   {
      currentDisplay = displayList.get(curRow);
      currentEditorIterator = currentDisplay.iterator();
      currentEditor = currentEditorIterator.next();
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
      // TODO we should probably get new value out and save
      if (currentEditorIterator.hasNext())
      {
         editor.setViewMode(ToggleEditor.ViewMode.VIEW);
         currentEditor = currentEditorIterator.next();
         currentEditor.setViewMode(ToggleEditor.ViewMode.EDIT);
      }
      else
      {
         // TODO if it's out of current editor index, we should move to next row
         eventBus.fireEvent(new NavTransUnitEvent(NavTransUnitEvent.NavigationType.NextEntry));
      }
   }

   @Override
   public void copySource(ToggleEditor editor)
   {
      editor.setText(sourcePanelPresenter.getSelectedSource());
      editor.autoSize();
   }

   @Override
   public void toggleView(ToggleEditor editor)
   {
      // TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //
   }
}
