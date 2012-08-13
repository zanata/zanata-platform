package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.NotificationEvent.Severity;

public interface HasNotificationLabel
{
   void setNotificationLabel(int count, Severity severity);

   void showNotificationAlert();
   
   void cancelNotificationAlert();
}
