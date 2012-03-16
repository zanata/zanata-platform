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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.zanata.webtrans.client.ui.HighlightingLabel;

public class SingleTargetEditorWidget implements ToggleWidget {
    private static Binder binder = GWT.create(Binder.class);

    @UiField
    TextArea editor;
    @UiField
    HighlightingLabel readOnly;
    @UiField
    PushButton copySourceButton;

    private HorizontalPanel rootPanel;

    public SingleTargetEditorWidget() {
        rootPanel = binder.createAndBindUi(this);

        readOnly.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getViewMode() == ViewMode.VIEW) {
                    setViewMode(ViewMode.EDIT);
                }
            }
        });
    }


    @Override
    public ViewMode getViewMode() {
        if (readOnly.isVisible()) {
            return ViewMode.VIEW;
        } else {
            return ViewMode.EDIT;
        }
    }

    @Override
    public void setViewMode(ViewMode viewMode) {
        readOnly.setVisible(viewMode == ViewMode.VIEW);
        editor.setVisible(viewMode == ViewMode.EDIT);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public void setValue(String value) {
        readOnly.setText(value);
        editor.setText(value);
    }

    @Override
    public String getValue() {
        return editor.getText();
    }

    public static interface Binder extends UiBinder<HorizontalPanel, SingleTargetEditorWidget> {
    }
}
