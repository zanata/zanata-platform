package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NotificationEvent.Severity;

public interface NotificationLabelListener {
    void setNotificationLabel(int count, Severity severity);

    void showNotification();
}
