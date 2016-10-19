/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import com.google.gwt.user.client.ui.Widget;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.DiffMode;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/

public class DiffColorLegendPanel extends PopupPanel {

    interface DiffColorLegendPanelUiBinder extends
            UiBinder<HTMLPanel, DiffColorLegendPanel> {
    }

    interface Styles extends CssResource {
        String diffLegendPanel();
    }

    @UiField
    Label searchOnlyDescription, tmOnlyDescription, matchDescription, absentDescription;

    @UiField
    InlineLabel searchOnlyLabel, tmOnlyLabel, matchLabel, absentLabel;

    @UiField
    Styles style;

    @UiField
    WebTransMessages messages;

    private static DiffColorLegendPanelUiBinder uiBinder = GWT
            .create(DiffColorLegendPanelUiBinder.class);

    @Inject
    public DiffColorLegendPanel() {
        super(true, true);

        HTMLPanel container = uiBinder.createAndBindUi(this);
        setStyleName(style.diffLegendPanel());
        setWidget(container);
    }

    public void show(ShortcutContext context, DiffMode diffMode) {
        //reset to default style
        showTableRow(searchOnlyLabel, true);
        showTableRow(tmOnlyLabel, true);
        showTableRow(matchLabel, true);

        switch (context) {
        case TM:
            searchOnlyLabel.setText(messages.searchOnly());
            tmOnlyLabel.setText(messages.tmOnly());
            matchLabel.setText(messages.noColor());

            searchOnlyDescription.setText(messages.tmInsertTagDesc());
            tmOnlyDescription.setText(messages.tmDelTagDesc());
            matchDescription.setText(messages.tmHighlightTextDesc());

            if (diffMode == DiffMode.NORMAL) {
                showTableRow(absentLabel, false);
            } else {
                showTableRow(tmOnlyLabel, false);
                showTableRow(searchOnlyLabel, false);
                showTableRow(absentLabel, true);
            }
            break;
        case ProjectWideSearch:
            showTableRow(absentLabel, false);
            searchOnlyLabel.setText(messages.searchColor());
            tmOnlyLabel.setText(messages.tmColor());
            matchLabel.setText(messages.highlightColor());

            searchOnlyDescription.setText(messages.searchReplaceInsertTagDesc());
            tmOnlyDescription.setText(messages.searchReplaceDelTagDesc());
            matchDescription.setText(messages.searchReplacePlainTextDesc());
            break;
        default:
            break;
        }
        this.center();
    }

    private void showTableRow(Widget label, boolean show) {
        if (show) {
            label.getElement().getParentElement().getParentElement()
                .removeClassName("is-hidden");
        } else {
            label.getElement().getParentElement().getParentElement()
                .addClassName("is-hidden");
        }
    }
}
