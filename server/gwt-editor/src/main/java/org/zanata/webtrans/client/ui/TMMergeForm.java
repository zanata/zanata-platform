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

import com.google.gwt.user.client.ui.Widget;
import org.zanata.webtrans.client.resources.EnumMessages;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.MergeOptions;

import com.google.common.base.Preconditions;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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

    private final EnumRadioButtonGroup<MergeRule> projectOptionGroup;
    private final EnumRadioButtonGroup<MergeRule> docIdOptionGroup;
    private final EnumRadioButtonGroup<MergeRule> contextOptionGroup;
    private final EnumRadioButtonGroup<MergeRule> importedMatchOptionGroup;
    private final MergeStatusRenderer mergeStatusRenderer;

    private TransMemoryMergePopupPanelDisplay.Listener listener;

    @Inject
    public TMMergeForm(MergeRuleRenderer mergeOptionRenderer,
            MergeStatusRenderer mergeStatusRenderer) {
        this.mergeStatusRenderer = mergeStatusRenderer;
        initWidget(uiBinder.createAndBindUi(this));

        matchThreshold.setItemText(0, messages.identical());

        projectOptionGroup =
                new EnumRadioButtonGroup<MergeRule>(
                        OptionType.PROJECT_MISMATCH.name(), MergeRule.class,
                        mergeOptionRenderer);
        projectOptionGroup.setSelectionChangeListener(this);
        projectOptionGroup.addToContainer(differentProject).setDefaultSelected(
                MergeRule.FUZZY);

        docIdOptionGroup =
                new EnumRadioButtonGroup<MergeRule>(
                        OptionType.DOC_ID_MISMATCH.name(), MergeRule.class,
                        mergeOptionRenderer);
        docIdOptionGroup.setSelectionChangeListener(this);
        docIdOptionGroup.addToContainer(differentDocument).setDefaultSelected(
                MergeRule.FUZZY);

        contextOptionGroup =
                new EnumRadioButtonGroup<MergeRule>(
                        OptionType.CTX_MISMATCH.name(), MergeRule.class,
                        mergeOptionRenderer);
        contextOptionGroup.setSelectionChangeListener(this);
        contextOptionGroup.addToContainer(differentContext).setDefaultSelected(
                MergeRule.FUZZY);

        importedMatchOptionGroup =
                new EnumRadioButtonGroup<MergeRule>(
                        OptionType.IMPORTED_MATCH.name(), MergeRule.class,
                        mergeOptionRenderer);
        importedMatchOptionGroup.setSelectionChangeListener(this);
        importedMatchOptionGroup.addToContainer(importedMatchPanel)
                .setDefaultSelected(MergeRule.FUZZY);
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
        opts.setDifferentDocument(docIdOptionGroup.getSelected());
        opts.setDifferentProject(projectOptionGroup.getSelected());
        opts.setDifferentResId(contextOptionGroup.getSelected());
        opts.setImportedMatch(importedMatchOptionGroup.getSelected());
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
