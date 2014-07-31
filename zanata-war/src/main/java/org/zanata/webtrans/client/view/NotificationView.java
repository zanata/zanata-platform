/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.NotificationDetailListener;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.NotificationDetailsBox;
import org.zanata.webtrans.client.ui.NotificationItem;
import org.zanata.webtrans.client.ui.UnorderedListWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class NotificationView extends Composite implements NotificationDisplay,
        NotificationDetailListener {

    private static NotificationPanelUiBinder uiBinder = GWT
            .create(NotificationPanelUiBinder.class);

    interface NotificationPanelUiBinder extends
            UiBinder<Widget, NotificationView> {
    }

    @UiField
    Anchor clearLink;

    @UiField
    Resources resources;

    @UiField
    UnorderedListWidget messagePanel;

    private int messagesToKeep;
    private Listener listener;

    private final NotificationDetailsBox notificationDetailsBox;

    private final WebTransMessages messages;

    @Inject
    public NotificationView(WebTransMessages messages,
            KeyShortcutPresenter keyShortcutPresenter) {
        initWidget(uiBinder.createAndBindUi(this));
        this.messages = messages;
        notificationDetailsBox =
                new NotificationDetailsBox(messages, keyShortcutPresenter);
    }

    @UiHandler("clearLink")
    public void onClearButtonClick(ClickEvent event) {
        listener.onClearClick();
    }

    @Override
    public void setMessagesToKeep(int count) {
        messagesToKeep = count;
    }

    @Override
    public void clearMessages() {
        messagePanel.clear();
    }

    @Override
    public void appendMessage(final NotificationEvent notificationEvent) {
        messagePanel.addOnTop(new NotificationItem(messages, notificationEvent,
                this, true));

        while (messagePanel.getWidgetCount() > messagesToKeep) {
            messagePanel.remove(messagePanel.getWidgetCount() - 1);
        }
    }

    @Override
    public int getMessageCount() {
        return messagePanel.getWidgetCount();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void showNotificationDetail(NotificationEvent notificationEvent) {
        notificationDetailsBox.setMessage(notificationEvent);
        notificationDetailsBox.center();
    }

    @Override
    public void closeMessage(NotificationEvent notificationEvent) {
        // not supported on side notification
    }
}
