/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.EnumMessages;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.shared.rpc.MergeOptions;
import org.zanata.webtrans.shared.rpc.MergeRule;

import com.google.common.base.Preconditions;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMMergeForm extends Composite implements
        EnumRadioButtonGroup.SelectionChangeListener<MergeRule> {
    private static TMMergeFormUiBinder uiBinder = GWT
            .create(TMMergeFormUiBinder.class);

    @UiField
    ListBox matchThreshold;
    @UiField
    Button confirmButton, cancelButton;

    @UiField
    HorizontalPanel differentProject, differentDocument, differentContext,
            importedMatchPanel;

    @UiField
    InlineLabel differentProjectStatus, differentDocIdStatus,
            differentContextStatus, importedMatchStatus;

    @UiField
    EnumMessages enumMessages;
    @UiField
    InlineLabel differentContentStatus;
    @UiField
    Label differentContentLabel;
    @UiField
    UiMessages messages;

    private final CheckBox projectOption;
    private final CheckBox docOption;
    private final CheckBox contextOption;
    private final CheckBox importedTMOption;

    private final MergeStatusRenderer mergeStatusRenderer;

    private TransMemoryMergePopupPanelDisplay.Listener listener;

    @Inject
    public TMMergeForm(MergeStatusRenderer mergeStatusRenderer) {
        this.mergeStatusRenderer = mergeStatusRenderer;
        initWidget(uiBinder.createAndBindUi(this));

        matchThreshold.setItemText(0, messages.identical());

        projectOption = new CheckBox(messages.copyAsFuzzy());
        changeStatusLabelFor(MergeRule.fromSelection(projectOption.getValue()),
                OptionType.PROJECT_MISMATCH);
        projectOption.addValueChangeHandler(event -> {
            changeStatusLabelFor(MergeRule.fromSelection(event.getValue()),
                    OptionType.PROJECT_MISMATCH);
        });
        differentProject.add(projectOption);

        docOption = new CheckBox(messages.copyAsFuzzy());
        changeStatusLabelFor(MergeRule.fromSelection(docOption.getValue()),
                OptionType.DOC_ID_MISMATCH);
        docOption.addValueChangeHandler(event -> {
            changeStatusLabelFor(MergeRule.fromSelection(event.getValue()),
                    OptionType.DOC_ID_MISMATCH);
        });
        differentDocument.add(docOption);

        contextOption = new CheckBox(messages.copyAsFuzzy());
        changeStatusLabelFor(MergeRule.fromSelection(contextOption.getValue()),
                OptionType.CTX_MISMATCH);
        contextOption.addValueChangeHandler(event -> {
            changeStatusLabelFor(MergeRule.fromSelection(event.getValue()),
                    OptionType.CTX_MISMATCH);
        });
        differentContext.add(contextOption);

        importedTMOption = new CheckBox(messages.copyAsFuzzy());
        changeStatusLabelFor(
                MergeRule.fromSelection(importedTMOption.getValue()),
                OptionType.IMPORTED_MATCH);
        importedTMOption.addValueChangeHandler(event -> {
            changeStatusLabelFor(MergeRule.fromSelection(event.getValue()),
                    OptionType.IMPORTED_MATCH);
        });
        importedMatchPanel.add(importedTMOption);
    }

    public void
            setListener(TransMemoryMergePopupPanelDisplay.Listener listener) {
        this.listener = listener;
    }

    @UiHandler("confirmButton")
    public void onConfirmButtonClick(ClickEvent event) {
        Preconditions.checkNotNull(listener,
                "Do you forget to call setListener on TMMergeForm?");
        listener.proceedToMergeTM(getSelectedMatchThreshold(),
                getSelectedMergeOptions());
    }

    private MergeOptions getSelectedMergeOptions() {
        // default to most conservative option
        MergeOptions opts = MergeOptions.allReject();

        opts.setDifferentDocument(MergeRule.fromSelection(docOption.getValue()));
        opts.setDifferentProject(MergeRule.fromSelection(projectOption.getValue()));
        opts.setDifferentResId(MergeRule.fromSelection(contextOption.getValue()));
        opts.setImportedMatch(MergeRule.fromSelection(importedTMOption.getValue()));
        return opts;
    }

    private int getSelectedMatchThreshold() {
        String percent =
                matchThreshold.getValue(matchThreshold.getSelectedIndex());
        return Integer.parseInt(percent);
    }

    @UiHandler("cancelButton")
    public void onCancelButtonClick(ClickEvent event) {
        listener.cancelMergeTM();
    }

    @UiHandler("matchThreshold")
    public void onThresholdPercentChange(ChangeEvent event) {
        if (getSelectedMatchThreshold() == 100) {
            differentContentStatus.setStyleName("label--danger l--pad-v-quarter l--pad-h-half");
            differentContentStatus.setText(enumMessages.rejectMerge());
            differentContentLabel.setText(enumMessages.rejectMerge());
        } else {
            differentContentStatus.setStyleName("label--warning l--pad-v-quarter l--pad-h-half");
            differentContentStatus.setText(enumMessages.downgradeToFuzzy());
            differentContentLabel.setText(enumMessages.downgradeToFuzzy());
        }
    }

    @Override
    public void onSelectionChange(String groupName, MergeRule option) {
        OptionType optionType = OptionType.valueOf(groupName);
        changeStatusLabelFor(option, optionType);
    }

    private void changeStatusLabelFor(MergeRule option,
            OptionType optionType) {
        InlineLabel statusLabel = getStatusLabelFor(optionType);
        statusLabel.setText(mergeStatusRenderer.render(option));
        statusLabel.setStyleName(resolveStyle(option));
    }

    private InlineLabel getStatusLabelFor(OptionType optionType) {
        switch (optionType) {
        case PROJECT_MISMATCH:
            return differentProjectStatus;
        case DOC_ID_MISMATCH:
            return differentDocIdStatus;
        case CTX_MISMATCH:
            return differentContextStatus;
        case IMPORTED_MATCH:
            return importedMatchStatus;
        default:
            throw new RuntimeException("unknown option: " + optionType);
        }
    }

    private String resolveStyle(MergeRule option) {
        switch (option) {
        case FUZZY:
            return "label--unsure l--pad-v-quarter l--pad-h-half";
        case REJECT:
            return "label--danger l--pad-v-quarter l--pad-h-half";
        case IGNORE_CHECK:
            return "label--neutral l--pad-v-quarter l--pad-h-half";
        }
        return "label--success l--pad-v-quarter l--pad-h-half";
    }

    interface TMMergeFormUiBinder extends UiBinder<Widget, TMMergeForm> {
    }

    enum OptionType {
        PROJECT_MISMATCH, DOC_ID_MISMATCH, CTX_MISMATCH, IMPORTED_MATCH
    }
}
