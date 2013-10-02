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
import org.zanata.webtrans.client.util.DateUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

    private static NotificationItemLineUiBinder uiBinder = GWT
            .create(NotificationItemLineUiBinder.class);

    public NotificationItem(final NotificationEvent notificationEvent,
            final NotificationDetailListener listener) {

        initWidget(uiBinder.createAndBindUi(this));
        this.addStyleName(getSeverityClass(notificationEvent.getSeverity()));

        message.setText(DateUtil.formatShortDate(notificationEvent
                .getDate()) + " " + notificationEvent.getSummary());

        InlineLink inlineLink = notificationEvent.getInlineLink();
        if (inlineLink != null) {
            link.add(inlineLink);
        } else {
            link.setVisible(false);
        }

        details.setText("Details");
        details.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listener.showNotificationDetail(notificationEvent);
            }
        });

        closeMessage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listener.closeMessage(notificationEvent);
            }
        });
    }

    private String getSeverityClass(NotificationEvent.Severity severity) {
        if (severity == NotificationEvent.Severity.Warning) {
            return "message--warning";
        } else if (severity == NotificationEvent.Severity.Error) {
            return "message--danger";
        }
        return "message--highlight";
    }

    interface NotificationItemLineUiBinder extends
            UiBinder<Widget, NotificationItem> {
    }
}
