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

package org.zanata.webtrans.client.ui;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;

public class TransHistoryItemLine extends Composite {
    private static TransHistoryItemLineUiBinder ourUiBinder = GWT
            .create(TransHistoryItemLineUiBinder.class);
    private static TransHistoryItemTemplate template = GWT
            .create(TransHistoryItemTemplate.class);
    private final TransHistoryItem item;
    private final TranslationHistoryDisplay.Listener listener;

    @UiField(provided = true)
    InlineHTML heading;
    @UiField(provided = true)
    InlineHTML targetContents;
    @UiField
    InlineLabel creationDate;
    @UiField(provided = true)
    InlineHTML revision;
    @UiField
    Anchor compare;
    @UiField
    Anchor copyIntoEditor;
    @UiField
    WebTransMessages messages;
    @UiField
    SpanElement icon;

    public TransHistoryItemLine(TransHistoryItem item,
            TranslationHistoryDisplay.Listener listener,
            ContentStateRenderer stateRenderer) {
        this.item = item;
        this.listener = listener;
        // modified by person can be empty if translation is pushed from client
        String person =
                item.getModifiedBy().isEmpty() ? "(Someone offline)" : item
                        .getModifiedBy();
        heading =
                new InlineHTML(template.heading(person,
                        stateToStyle(item.getStatus()),
                        stateRenderer.render(item.getStatus())));
        targetContents =
                new InlineHTML(template.targetContent(TextContentsDisplay
                        .asSyntaxHighlight(item.getContents()).toSafeHtml()));
        revision =
                new InlineHTML(template.targetRevision(item.getVersionNum(),
                        item.getOptionalTag()));
        initWidget(ourUiBinder.createAndBindUi(this));

        if (item.getStatus() == ContentState.Approved
                || item.getStatus() == ContentState.Rejected) {
            icon.addClassName("i--review");
        } else {
            icon.addClassName("i--translate");
        }
        creationDate.setText(DateUtil.formatShortDate(item.getModifiedDate()));
    }

    private static String stateToStyle(ContentState status) {
        if (status == null) {
            return "";
        }
        switch (status) {
        case New:
            return "txt--state-neutral";
        case NeedReview:
            return "txt--state-unsure";
        case Translated:
            return "txt--state-success";
        case Approved:
            return "txt--state-highlight";
        case Rejected:
            return "txt--state-danger";
        }
        return "";
    }

    @UiHandler("copyIntoEditor")
    public void copyIntoEditorClicked(ClickEvent event) {
        listener.copyIntoEditor(item.getContents());
    }

    @UiHandler("compare")
    public void compareClicked(ClickEvent event) {
        listener.compareClicked(item);
        if (listener.isItemInComparison(item)) {
            compare.setText(messages.removeFromComparison());
        } else {
            compare.setText(messages.compare());
        }
    }

    interface TransHistoryItemLineUiBinder extends
            UiBinder<HTMLPanel, TransHistoryItemLine> {
    }

    public interface TransHistoryItemTemplate extends SafeHtmlTemplates {
        @Template("<div class='l--pad-v-half'>{0}</div>")
        SafeHtml targetContent(SafeHtml message);

        @Template("<div class='txt--meta'>{0} created a <strong class='{1}'>{2}</strong> revision</div>")
                SafeHtml heading(String person, String contentStateStyle,
                        String contentState);

        @Template("<span class='txt--important'>Revision {0} </span><span class=\"label\">{1}</span>")
                SafeHtml
                targetRevision(String versionNum, String optionalLabel);
    }
}
