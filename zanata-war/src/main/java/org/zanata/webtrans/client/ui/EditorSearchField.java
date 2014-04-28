/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;


/**
 *
 * @author damason@redhat.com
 *
 */
public class EditorSearchField extends Composite {
    interface EditorSearchFieldUiBinder extends UiBinder<Widget, EditorSearchField> {

    }
    private static EditorSearchFieldUiBinder uiBinder = GWT
            .create(EditorSearchFieldUiBinder.class);

    @UiField
    SearchTextBox filterTextBox;

    @UiField
    HTMLPanel wrapper;

    @UiField
    Anchor cancelBtn;

    private EditorSearchFieldListener listener;

    public EditorSearchField(EditorSearchFieldListener listener) {
        this.listener = listener;
        initWidget(uiBinder.createAndBindUi(this));
        initTextBox(wrapper.getElement(), listener);
    }

    private void onValueChanged(String newValue, EditorSearchFieldListener listener) {
        listener.onSearchFieldValueChange(newValue);
    }

    private native void initTextBox(Element wrapper, EditorSearchFieldListener listener)/*-{
        var valueChangeCallback = this.@org.zanata.webtrans.client.ui.EditorSearchField::onValueChanged(Ljava/lang/String;Lorg/zanata/webtrans/client/ui/EditorSearchFieldListener;);
        $wnd.searchSuggestions.init(wrapper, function (newValue) {
            console.log('editor search field change callback being fired now');
            valueChangeCallback(newValue, listener);
        });
    }-*/;

    @UiHandler("filterTextBox")
    public void onFilterTextBoxBlur(BlurEvent event) {
        listener.onSearchFieldBlur();
    }

    @UiHandler("filterTextBox")
    public void onFilterTextBoxFocus(FocusEvent event) {
        listener.onSearchFieldFocus();
    }

    @UiHandler("cancelBtn")
    public void onCancel(ClickEvent event) {
        listener.onSearchFieldCancel();
    }

    public void setText(String text) {
        filterTextBox.setText(text);
    }

    // fire filterTextBox#ValueChangeEvent
    public void setValue(String text) {
        filterTextBox.setValue(text, true);
    }

    public String getText() {
        return filterTextBox.getValue();
    }

    public void setPlaceholderText(String text) {
        filterTextBox.setPlaceholder(text);
    }

    public boolean isFocused() {
        return filterTextBox.isFocused();
    }
}
