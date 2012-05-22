package org.zanata.webtrans.client.ui;

public interface HasNotificationData
{
   void setMessagesToKeep(int count);

   void appendMessage(String msg);

   void setPopupPosition(int left, int top);

   void show();

   void hide(boolean autoClosed);
}
