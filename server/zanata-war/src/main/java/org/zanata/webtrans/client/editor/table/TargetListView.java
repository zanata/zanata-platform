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

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.zanata.webtrans.client.ui.Editor;

import java.util.List;

public class TargetListView implements TargetListDisplay {
    public static final int COLUMNS = 1;
    public static final int DEFAULT_ROWS = 1;

    private Grid editorGrid;
    private String findMessage;
    private List<Editor> editors;

    public TargetListView() {
        editorGrid = new Grid(DEFAULT_ROWS, COLUMNS);
        editors = Lists.newArrayList();
    }



    @Override
    public void setTargets(List<String> targets) {
        editors.clear();
        editorGrid.resize(targets.size(), COLUMNS);
        int rowIndex = 0;
        for (String target : targets) {
            Editor editor = new Editor(target, findMessage);
            editor.setText(target);
            editorGrid.setWidget(rowIndex, 0, editor);
        }
    }

    @Override
    public void setFindMessage(String findMessage) {
        this.findMessage = findMessage;
    }

    @Override
    public List<String> getNewTargets() {
        List<String> result = Lists.newArrayList();
        for (IsWidget widget : editorGrid) {
            if (widget instanceof Editor) {
                Editor targetEditorWidget = (Editor) widget;
                editors.add(targetEditorWidget);
                result.add(targetEditorWidget.getText());
            }
        }
        return result;
    }

    @Override
    public void setToView() {
        for (Editor editor : editors) {
            editor.setViewMode(ToggleWidget.ViewMode.VIEW);
        }
    }

    @Override
    public ToggleWidget getCurrentEditor() {
        for (Editor editor : editors) {
            if (editor.getViewMode() == ToggleWidget.ViewMode.EDIT) {
                return editor;
            }
        }
        return null;
    }

    @Override
    public Widget asWidget() {
        return editorGrid;
    }

}
