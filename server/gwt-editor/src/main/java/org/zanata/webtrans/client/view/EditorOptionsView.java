/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.EnumRadioButtonGroup;
import org.zanata.webtrans.client.ui.NavOptionRenderer;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditorOptionsView extends Composite implements
        EditorOptionsDisplay {

    private static EditorOptionsUiBinder uiBinder = GWT
            .create(EditorOptionsUiBinder.class);

    private final EnumRadioButtonGroup<NavOption> navOptionGroup;

    @UiField
    VerticalPanel optionsContainer;

    @UiField
    Anchor five, ten, twentyFive, fifty;

    @UiField
    RadioButton diffModeDiff, diffModeHighlight;

    @UiField
    CheckBox showTMChk, showGlossaryChk, showOptionalTransUnitDetailsChk,
            useCodeMirrorChk, showSaveApprovedWarningChk,
            enterChk, editorButtonsChk;

    @UiField
    WebTransMessages messages;

    @UiField
    HTMLPanel sourceLangListBoxContainer;

    private Listener listener;

    @Inject
    public EditorOptionsView(WebTransMessages messages,
            NavOptionRenderer navOptionRenderer, UiMessages uiMessages) {
        initWidget(uiBinder.createAndBindUi(this));
        navOptionGroup =
                new EnumRadioButtonGroup<NavOption>("navOption",
                        NavOption.class, navOptionRenderer);
        navOptionGroup.addToContainer(optionsContainer);

        showSaveApprovedWarningChk.setTitle(messages
                .showSaveApprovedWarningTooltip());

        diffModeDiff.setText(uiMessages.diffModeAsDiff());
        diffModeHighlight.setText(uiMessages.diffModeAsHighlight());
        diffModeDiff.setValue(true);

        showTMChk.setText(messages.showTranslationMemoryPanel());
        showGlossaryChk.setText(messages.showGlossaryPanel());
        showOptionalTransUnitDetailsChk
                .setText(messages.showTransUnitDetails());
        showOptionalTransUnitDetailsChk.setTitle(messages
                .showTransUnitDetailsTooltip());

        useCodeMirrorChk.ensureDebugId("syntax-highlight-chk");
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @UiHandler("five")
    public void onPageSizeFiveClicked(ClickEvent event) {
        selectPageSize(5);
    }

    @UiHandler("ten")
    public void onPageSizeTenClicked(ClickEvent event) {
        selectPageSize(10);
    }

    @UiHandler("twentyFive")
    public void onPageSizeTwentyFiveClicked(ClickEvent event) {
        selectPageSize(25);
    }

    @UiHandler("fifty")
    public void onPageSizeFiftyClicked(ClickEvent event) {
        selectPageSize(50);
    }

    @UiHandler("editorButtonsChk")
    public void onEditorButtonsOptionChanged(ValueChangeEvent<Boolean> event) {
        listener.onEditorButtonsOptionChanged(editorButtonsChk.getValue());
    }

    @UiHandler("enterChk")
    public void onEnterSaveOptionChanged(ValueChangeEvent<Boolean> event) {
        listener.onEnterSaveOptionChanged(enterChk.getValue());
    }

    @UiHandler("useCodeMirrorChk")
    public void onCodeMirrorOptionChanged(ValueChangeEvent<Boolean> event) {
        listener.onUseCodeMirrorOptionChanged(useCodeMirrorChk.getValue(), false);
    }

    @UiHandler("showSaveApprovedWarningChk")
    public void
            onShowSaveApprovedWarningChanged(ValueChangeEvent<Boolean> event) {
        listener.onShowSaveApprovedWarningChanged(showSaveApprovedWarningChk
                .getValue());
    }

    @UiHandler({ "diffModeDiff", "diffModeHighlight" })
    public void onDiffModeOptionChange(ValueChangeEvent<Boolean> event) {
        if (diffModeDiff.getValue()) {
            listener.onTransMemoryDisplayModeChanged(DiffMode.NORMAL);
        } else {
            listener.onTransMemoryDisplayModeChanged(DiffMode.HIGHLIGHT);
        }
    }

    @UiHandler({ "showTMChk", "showGlossaryChk" })
    public void onTMOrGlossaryDisplayOptionsChanged(
            ValueChangeEvent<Boolean> event) {
        listener.onTMOrGlossaryDisplayOptionsChanged(showTMChk.getValue(),
                showGlossaryChk.getValue());
    }

    @UiHandler("showOptionalTransUnitDetailsChk")
    public void onDisplayTransUnitDetailsOptionChanged(
            ValueChangeEvent<Boolean> event) {
        listener.onDisplayTransUnitDetailsOptionChanged(showOptionalTransUnitDetailsChk
                .getValue());
    }

    @Override
    public void setSourceLangListBox(IsWidget sourceLangListBoxView) {
        sourceLangListBoxContainer.clear();
        sourceLangListBoxContainer.add(sourceLangListBoxView);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
        navOptionGroup.setSelectionChangeListener(listener);
    }

    @Override
    public void setOptionsState(UserConfigHolder.ConfigurationState state) {
        enterChk.setValue(state.isEnterSavesApproved());
        editorButtonsChk.setValue(state.isDisplayButtons());

        navOptionGroup.setDefaultSelected(state.getNavOption());
        selectPageSize(state.getEditorPageSize());
        useCodeMirrorChk.setValue(state.isUseCodeMirrorEditor());
        listener.onUseCodeMirrorOptionChanged(useCodeMirrorChk.getValue(), true);
        showSaveApprovedWarningChk.setValue(state.isShowSaveApprovedWarning());

        if (state.getTransMemoryDisplayMode() == DiffMode.NORMAL) {
            diffModeDiff.setValue(true);
        } else {
            diffModeHighlight.setValue(true);
        }

        showTMChk.setValue(state.isShowTMPanel());
        showGlossaryChk.setValue(state.isShowGlossaryPanel());
        onTMOrGlossaryDisplayOptionsChanged(null);
        showOptionalTransUnitDetailsChk.setValue(state
                .isShowOptionalTransUnitDetails());
    }

    private void selectPageSize(int pageSize) {
        switch (pageSize) {
            case 5:
                updatePageSizeAnchorStyle(five);
                listener.onPageSizeClick(pageSize);
                break;
            case 10:
                updatePageSizeAnchorStyle(ten);
                listener.onPageSizeClick(pageSize);
                break;
            case 25:
                updatePageSizeAnchorStyle(twentyFive);
                listener.onPageSizeClick(pageSize);
                break;
            case 50:
                updatePageSizeAnchorStyle(fifty);
                listener.onPageSizeClick(pageSize);
                break;
        }
    }

    private void updatePageSizeAnchorStyle(Anchor selectedPage) {
        five.removeStyleName("txt--important");
        ten.removeStyleName("txt--important");
        twentyFive.removeStyleName("txt--important");
        fifty.removeStyleName("txt--important");
        selectedPage.addStyleName("txt--important");
    }

    interface EditorOptionsUiBinder extends UiBinder<Widget, EditorOptionsView> {
    }
}
