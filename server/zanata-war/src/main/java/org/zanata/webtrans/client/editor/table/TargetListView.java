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

import java.util.List;

public class TargetListView implements TargetListDisplay {
    public static final int COLUMNS = 2;
    public static final int DEFAULT_ROWS = 1;

    private VerticalPanel rootPanel;

    private Grid editorGrid;

    public TargetListView() {
        rootPanel = new VerticalPanel();
        editorGrid = new Grid(DEFAULT_ROWS, COLUMNS);
    }

    @Override
    public void setTargets(List<String> targets) {
        editorGrid.resize(targets.size(), COLUMNS);
        int rowIndex = 0;
        for (String target : targets) {
            SingleTargetEditorWidget singleTarget = new SingleTargetEditorWidget();
            singleTarget.setValue(target);
            editorGrid.setWidget(rowIndex, 0, singleTarget);
        }
    }

    @Override
    public List<String> getNewTargets() {
        List<String> result = Lists.newArrayList();
        for (IsWidget widget : editorGrid) {
            if (widget instanceof SingleTargetEditorWidget) {
                SingleTargetEditorWidget targetEditorWidget = (SingleTargetEditorWidget) widget;
                result.add(targetEditorWidget.getValue());
            }
        }
        return result;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

}
