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
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.ContentStateToStyleUtil;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
    private static WebTransMessages messages = GWT
            .create(WebTransMessages.class);
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
    SpanElement icon;
    @UiField(provided = true)
    InlineHTML revisionComment;


    public TransHistoryItemLine(TransHistoryItem item,
            TranslationHistoryDisplay.Listener listener,
            ContentStateRenderer stateRenderer) {
        this.item = item;
        this.listener = listener;

        if (item.getModifiedBy().isEmpty()) {
            // before rhbz1149968 modified by person can be empty if translation is
            // pushed from client
            SafeHtml anonymous = template.anonymousUser(messages.anonymousUser());
            heading =
                new InlineHTML(template.anonymousHeading(anonymous,
                    ContentStateToStyleUtil.stateToStyle(item.getStatus()),
                    stateRenderer.render(item.getStatus())));
        } else {
            SafeHtml username = new SafeHtmlBuilder()
                    .appendHtmlConstant(item.getModifiedBy()).toSafeHtml();
            String url = Application.getUserProfileURL(item.getModifiedBy());
            heading =
                new InlineHTML(template.heading(url, username,
                    ContentStateToStyleUtil.stateToStyle(item.getStatus()),
                    stateRenderer.render(item.getStatus())));
        }

        targetContents =
                new InlineHTML(template.targetContent(TextContentsDisplay
                        .asSyntaxHighlight(item.getContents()).toSafeHtml()));
        if (!Strings.isNullOrEmpty(item.getOptionalTag())) {
            revision =
                new InlineHTML(template.targetRevision(item.getVersionNum(),
                    item.getOptionalTag()));
        } else {
            revision = new InlineHTML("");
        }

        if (!Strings.isNullOrEmpty(item.getRevisionComment())) {
            revisionComment = new InlineHTML(template.revisionComment(
                    item.getRevisionComment()));
        } else {
            revisionComment = new InlineHTML("");
        }

        initWidget(ourUiBinder.createAndBindUi(this));

        if (item.getStatus() == ContentState.Approved
                || item.getStatus() == ContentState.Rejected) {
            icon.addClassName("i--review");
        } else {
            icon.addClassName("i--translate");
        }
        creationDate.setText(DateUtil.formatShortDate(item.getModifiedDate()));
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

        @Template("<div class='txt--meta'><a href='{0}' target='_blank'>{1}</a> created a <strong class='{2}'>{3}</strong> revision</div>")
                SafeHtml heading(String url, SafeHtml username, String contentStateStyle,
                        String contentState);

        @Template("<div class='txt--meta'>{0} created a <strong class='{1}'>{2}</strong> revision</div>")
        SafeHtml anonymousHeading(SafeHtml person, String contentStateStyle,
            String contentState);

        @Template("<span class='txt--important'>Revision {0} </span><span class=\"label\">{1}</span>")
                SafeHtml
                targetRevision(String versionNum, String optionalLabel);

        @Template("<span class='txt--neutral'>{0}</span>")
        SafeHtml anonymousUser(String anonymous);

        @Template("<span class='txt--meta'>{0}</span>")
        SafeHtml revisionComment(String comment);
    }
}
