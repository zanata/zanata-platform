/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class SearchField extends Composite {
    interface SearchFieldUiBinder extends UiBinder<Widget, SearchField> {
    }

    private static SearchFieldUiBinder uiBinder = GWT
            .create(SearchFieldUiBinder.class);

    @UiField
    TextBox filterTextBox;

    @UiField
    InlineLabel cancelBtn;

    private SearchFieldListener listener;

    public SearchField(SearchFieldListener listener) {
        this.listener = listener;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("filterTextBox")
    public void onFilterTextBoxValueChange(ValueChangeEvent<String> event) {
        listener.onSearchFieldValueChange(event.getValue());
    }

    @UiHandler("filterTextBox")
    public void onFilterTextBoxClick(ClickEvent event) {
        listener.onSearchFieldClick();
    }

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

    public boolean containStyleName(String styleName) {
        return filterTextBox.getStyleName().contains(styleName);
    }

    public void addStyleName(String styleName) {
        filterTextBox.addStyleName(styleName);
    }

    public void removeStyleName(String styleName) {
        filterTextBox.removeStyleName(styleName);
    }

    public String getText() {
        return filterTextBox.getText();
    }

    public void setTextBoxTitle(String title) {
        filterTextBox.setTitle(title);
    }
}
