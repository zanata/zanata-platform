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

import org.zanata.webtrans.client.resources.UiMessages;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransMemoryMergePopupPanelView implements
        TransMemoryMergePopupPanelDisplay {

    @UiField(provided = true)
    final UiMessages messages;
    private final FlowPanel processingPanel = new FlowPanel();
    private final Button cancelButton = new Button("Cancel");

    interface TMIMergeUiBinder extends
            UiBinder<DialogBox, TransMemoryMergePopupPanelView> {
    }

    private final TMMergeForm TMMergeForm;
    private final Label processingLabel;

    private DialogBox dialogBox;

    private static TMIMergeUiBinder uiBinder = GWT
            .create(TMIMergeUiBinder.class);

    @Inject
    public TransMemoryMergePopupPanelView(TMMergeForm TMMergeForm,
            UiMessages messages) {
        this.messages = messages;
        // auto hide false, modal true

        dialogBox = uiBinder.createAndBindUi(this);
        dialogBox.setText(messages.mergeTMCaption());
        dialogBox.setGlassEnabled(true);
        dialogBox.ensureDebugId("TMMerge");
        dialogBox.setAutoHideEnabled(false);
        dialogBox.setModal(true);

        VerticalPanel main = new VerticalPanel();
        HTMLPanel heading = new HTMLPanel(messages.mergeTMHeading());
        main.add(heading);
        main.add(TMMergeForm);

        processingLabel = new Label(messages.processing());
        processingPanel.add(processingLabel);
        processingPanel.add(cancelButton);

        main.add(processingPanel);
        dialogBox.add(main);
        this.TMMergeForm = TMMergeForm;
        processingPanel.setVisible(false);
        hide();
    }

    @Override
    public void setListener(Listener listener) {
        TMMergeForm.setListener(listener);
        cancelButton.addClickHandler(event -> listener.cancelMergeTM());
    }

    @Override
    public void showProcessing(String progress) {
        TMMergeForm.setVisible(false);
        processingPanel.setVisible(true);
        processingLabel.setText(progress);
    }

    @Override
    public void showForm() {
        processingLabel.setText(messages.processing());
        processingPanel.setVisible(false);
        TMMergeForm.setVisible(true);
        dialogBox.center();
    }

    @Override
    public Widget asWidget() {
        return dialogBox;
    }

    @Override
    public void hide() {
        dialogBox.hide();

    }
}
