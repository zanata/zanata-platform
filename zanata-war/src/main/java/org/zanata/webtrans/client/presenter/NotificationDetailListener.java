package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NotificationEvent;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface NotificationDetailListener {

    void showNotificationDetail(NotificationEvent notificationEvent);

    /**
     * remove message from list
     * @param notificationEvent
     */
    void closeMessage(NotificationEvent notificationEvent);
}
