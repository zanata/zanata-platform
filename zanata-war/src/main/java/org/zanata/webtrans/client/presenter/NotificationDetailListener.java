package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NotificationEvent;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface NotificationDetailListener {

    void showNotificationDetail(NotificationEvent notificationEvent);

    void closeMessage(NotificationEvent notificationEvent);
}
