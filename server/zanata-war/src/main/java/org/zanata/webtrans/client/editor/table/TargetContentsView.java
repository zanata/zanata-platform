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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;
import org.zanata.webtrans.client.ui.Editor;

import java.util.Iterator;
import java.util.List;

public class TargetContentsView implements TargetContentsDisplay
{
   public static final int COLUMNS = 1;
   public static final int DEFAULT_ROWS = 1;

   private Grid editorGrid;
   private String findMessage;
   private List<ToggleEditor> editors;
   private Listener listener;

   public TargetContentsView()
   {
      editorGrid = new Grid(DEFAULT_ROWS, COLUMNS);
      editorGrid.addStyleName("TableEditorCell-Target-Table");
      editorGrid.ensureDebugId("target-contents-grid");
      editorGrid.setWidth("100%");
      editors = Lists.newArrayList();
   }

   @Override
    public void setTargets(List<String> targets) {
        editors.clear();
        editorGrid.resize(targets.size(), COLUMNS);
        int rowIndex = 0;
        for (String target : targets) {
            Editor editor = new Editor(target, findMessage, listener);
            editor.setText(target);
            editorGrid.setWidget(rowIndex, 0, editor);
            editors.add(editor);
            rowIndex++;
        }
    }

   @Override
   public void setFindMessage(String findMessage)
   {
      this.findMessage = findMessage;
   }

   @Override
   public List<String> getNewTargets()
   {
      List<String> result = Lists.newArrayList();
      for (ToggleEditor editor : editors)
      {
         result.add(editor.getText());
      }
      return result;
   }

   @Override
   public void setToView()
   {
      for (ToggleEditor editor : editors)
      {
         editor.setViewMode(ToggleEditor.ViewMode.VIEW);
      }
   }

    @Override
    public boolean isEditing() {
        for (ToggleEditor editor : editors) {
            if (editor.getViewMode() == ToggleEditor.ViewMode.EDIT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ToggleEditor> getEditors()
    {
        return ImmutableList.copyOf(editors);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Widget asWidget() {
        return editorGrid;
    }

    @Override
    public Iterator<ToggleEditor> iterator() {
        return editors.iterator();
    }
}
