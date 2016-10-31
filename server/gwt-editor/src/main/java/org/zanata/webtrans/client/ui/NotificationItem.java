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

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.NotificationDetailListener;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.DateUtil;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class NotificationItem extends Composite {

    @UiField
    InlineLabel message;

    @UiField
    HTMLPanel link;

    @UiField
    Anchor details, closeMessage;

    @UiField
    Styles style;

    interface Styles extends CssResource {
        String disabledInlineLink();

        String inlineLink();
    }

    private static NotificationItemLineUiBinder uiBinder = GWT
            .create(NotificationItemLineUiBinder.class);

    public NotificationItem(final WebTransMessages messages,
            final NotificationEvent notificationEvent,
            final NotificationDetailListener listener,
            boolean isSideNotification) {

        initWidget(uiBinder.createAndBindUi(this));

        ClickHandler showDetailsHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listener.showNotificationDetail(notificationEvent);
            }
        };
        message.setText(DateUtil.formatShortDate(notificationEvent.getDate())
                + " " + notificationEvent.getMessage());
        message.addClickHandler(showDetailsHandler);

        InlineLink inlineLink = notificationEvent.getInlineLink();
        if (inlineLink != null) {
            inlineLink.setLinkStyle(style.inlineLink());
            inlineLink.setDisabledStyle(style.disabledInlineLink());
            link.add(inlineLink);
        } else {
            link.setVisible(false);
        }

        details.setText(messages.moreDetais());
        details.addClickHandler(showDetailsHandler);

        closeMessage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listener.closeMessage(notificationEvent);
            }
        });

        if (isSideNotification) {
            details.setVisible(false);
            closeMessage.setVisible(false);
            this.addStyleName(getTxtSeverityClass(notificationEvent
                    .getSeverity()));
        } else {
            this.addStyleName(getSeverityClass(notificationEvent.getSeverity()));
        }
    }

    private String getSeverityClass(NotificationEvent.Severity severity) {
        if (severity == NotificationEvent.Severity.Warning) {
            return "message--warning";
        } else if (severity == NotificationEvent.Severity.Error) {
            return "message--danger app-error";
        }
        return "message--highlight";
    }

    private String getTxtSeverityClass(NotificationEvent.Severity severity) {
        if (severity == NotificationEvent.Severity.Warning) {
            return "txt--warning";
        } else if (severity == NotificationEvent.Severity.Error) {
            return "txt--danger";
        }
        return "txt--highlight";
    }

    interface NotificationItemLineUiBinder extends
            UiBinder<Widget, NotificationItem> {
    }
}
