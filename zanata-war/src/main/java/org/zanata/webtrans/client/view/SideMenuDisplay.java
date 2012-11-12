package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.events.NotificationEvent;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public interface SideMenuDisplay extends WidgetDisplay
{
   //Order of the tab
   static final int NOTIFICATION_VIEW = 0;
   static final int WORKSPACEUSER_VIEW = 1;
   static final int OPTION_VIEW = 2;
   static final int VALIDATION_OPTION_VIEW = 3;
   
   void setSelectedTab(int view);

   void setChatTabAlert(boolean alert);

   int getCurrentTab();

   void setNotificationText(int count, NotificationEvent.Severity severity);

   void setChatTabVisible(boolean visible);

   void setOptionsTabVisible(boolean visible);

   void setValidationOptionsTabVisible(boolean visible);

   void setListener(Listener listener);

   interface Listener
   {

      void onOptionsClick();

      void onNotificationClick();

      void onValidationOptionsClick();

      void onChatClick();
   }
}
