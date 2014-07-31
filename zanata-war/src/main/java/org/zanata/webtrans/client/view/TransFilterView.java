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
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.UserConfigHolder.ConfigurationState;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.EditorSearchField;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransFilterView extends Composite implements TransFilterDisplay {
    private static TransFilterViewUiBinder uiBinder = GWT
            .create(TransFilterViewUiBinder.class);

    @UiField(provided = true)
    EditorSearchField searchField;

    @UiField
    Styles style;

    @UiField
    CheckBox parentIncompleteChk, untranslatedChk, fuzzyChk, rejectedChk,
            parentCompleteChk, translatedChk, approvedChk, hasErrorChk;

    private Listener listener;

    interface TransFilterViewUiBinder extends UiBinder<Widget, TransFilterView> {
    }

    interface Styles extends CssResource {
        String transFilterTextBoxEmpty();
    }

    @Inject
    public TransFilterView(UiMessages messages) {
        searchField = new EditorSearchField(this);
        searchField.setPlaceholderText(messages.filterMesssagesByTerm());
        initWidget(uiBinder.createAndBindUi(this));
        hasErrorChk.setTitle(messages.invalidTooltip());
        getElement().setId("TransFilterView");
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public boolean isFocused() {
        return searchField.isFocused();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setSearchTerm(String searchTerm) {
        searchField.setText(searchTerm);
    }

    @Override
    public void setUntranslatedFilter(boolean filterByUntranslated) {
        updateChildCheckbox(untranslatedChk, filterByUntranslated);
    }

    @Override
    public void setNeedReviewFilter(boolean filterByNeedReview) {
        updateChildCheckbox(fuzzyChk, filterByNeedReview);
    }

    @Override
    public void setTranslatedFilter(boolean filterByTranslated) {
        updateChildCheckbox(translatedChk, filterByTranslated);
    }

    @Override
    public void setApprovedFilter(boolean filterByApproved) {
        updateChildCheckbox(approvedChk, filterByApproved);
    }

    @Override
    public void setRejectedFilter(boolean filterByRejected) {
        updateChildCheckbox(rejectedChk, filterByRejected);
    }

    @Override
    public void setHasErrorFilter(boolean filterByHasError) {
        updateChildCheckbox(hasErrorChk, filterByHasError);
    }

    private void updateChildCheckbox(CheckBox checkbox, boolean value) {
        checkbox.setValue(value);
        updateParentCheckboxes();
    }

    @Override
    public void onSearchFieldValueChange(String value) {
        listener.searchTerm(value);
    }

    @Override
    public void onSearchFieldBlur() {
        listener.onSearchFieldFocused(false);
    }

    @Override
    public void onSearchFieldFocus() {
        listener.onSearchFieldFocused(true);
    }

    @Override
    public void onSearchFieldCancel() {
        searchField.setValue("");
        listener.searchTerm("");
    }

    @UiHandler({ "translatedChk", "fuzzyChk", "untranslatedChk", "approvedChk",
            "rejectedChk", "hasErrorChk" })
    public void onFilterOptionsChanged(ValueChangeEvent<Boolean> event) {
        updateParentCheckboxes();
        listener.messageFilterOptionChanged(translatedChk.getValue(),
                fuzzyChk.getValue(), untranslatedChk.getValue(),
                approvedChk.getValue(), rejectedChk.getValue(),
                hasErrorChk.getValue());
    }

    private void updateParentCheckboxes() {
        updateParentCheckboxToMatchChildren(parentIncompleteChk,
                untranslatedChk, fuzzyChk, rejectedChk);
        updateParentCheckboxToMatchChildren(parentCompleteChk, translatedChk,
                approvedChk);
    }

    private void updateParentCheckboxToMatchChildren(CheckBox parent,
            CheckBox... children) {
        boolean allChecked = allHaveValue(true, children);
        boolean noneChecked = allHaveValue(false, children);
        boolean partiallyChecked = !(allChecked || noneChecked);

        parent.setValue(allChecked);
        setPartiallyChecked(parent, partiallyChecked);
    }

    private static boolean allHaveValue(boolean checkValue,
            CheckBox... checkboxes) {
        for (CheckBox checkbox : checkboxes) {
            if (checkbox.getValue() != checkValue) {
                return false;
            }
        }
        return true;
    }

    private static void setPartiallyChecked(CheckBox checkbox,
            boolean partiallyChecked) {
        setElementIndeterminate(checkbox.getElement(), partiallyChecked);
    }

    private static native void setElementIndeterminate(Element elem,
            boolean indeterminate)/*-{
    elem.getElementsByTagName('input')[0].indeterminate = indeterminate;
    }-*/;

    @UiHandler("parentIncompleteChk")
    public void onIncompleteChkChanged(ValueChangeEvent<Boolean> event) {
        untranslatedChk.setValue(event.getValue());
        fuzzyChk.setValue(event.getValue());
        rejectedChk.setValue(event.getValue());
        onFilterOptionsChanged(event);
    }

    @UiHandler("parentCompleteChk")
    public void onCompleteChkChanged(ValueChangeEvent<Boolean> event) {
        translatedChk.setValue(event.getValue());
        approvedChk.setValue(event.getValue());
        onFilterOptionsChanged(event);
    }

    @Override
    public void selectPartialText(String text) {
        searchField.selectText(text);
    }

    @Override
    public void setOptionsState(ConfigurationState state) {
        untranslatedChk.setValue(state.isFilterByUntranslated());
        fuzzyChk.setValue(state.isFilterByFuzzy());
        translatedChk.setValue(state.isFilterByTranslated());
        approvedChk.setValue(state.isFilterByApproved());
        rejectedChk.setValue(state.isFilterByRejected());
        hasErrorChk.setValue(state.isFilterByHasError());
    }
}
