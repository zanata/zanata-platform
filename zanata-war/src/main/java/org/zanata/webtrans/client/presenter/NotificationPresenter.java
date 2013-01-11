/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.NotificationEventHandler;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.view.NotificationDisplay;

import com.allen_sauer.gwt.log.client.Log;
import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class NotificationPresenter extends WidgetPresenter<NotificationDisplay> implements NotificationEventHandler, NotificationDisplay.Listener
{
   @Inject
   public NotificationPresenter(NotificationDisplay display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   private NotificationLabelListener listener;

   /**
    * Message count to keep in notification area
    */
   private static final int MESSAGE_TO_KEEP = 100;

   /**
    * 
    * Display order for the notification, Default = ASCENDING
    */
   public enum DisplayOrder
   {
      DESCENDING, ASCENDING
   }

   @Override
   protected void onBind()
   {
      display.setMessagesToKeep(MESSAGE_TO_KEEP);
      display.setMessageOrder(DisplayOrder.ASCENDING);
      display.setListener(this);

      registerHandler(eventBus.addHandler(NotificationEvent.getType(), this));
   }

   @Override
   public void onClearClick()
   {
      display.clearMessages();
      listener.setNotificationLabel(display.getMessageCount(), Severity.Info);
   }

   public void setNotificationListener(NotificationLabelListener listener)
   {
      this.listener = listener;
      listener.setNotificationLabel(0, Severity.Info);
   }


   private void appendNotification(Severity severity, String summary, String msg, boolean displayAsHtml, InlineLink inlineLink)
   {
      display.appendMessage(severity, summary, msg, displayAsHtml, inlineLink);
      if (severity == Severity.Error)
      {
         listener.showNotification();
      }
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

   @Override
   public void onNotification(NotificationEvent event)
   {
      appendNotification(event.getSeverity(), event.getSummary(), event.getMessage(), event.isDisplayAsHtml(), event.getInlineLink());
      Log.info("Notification:" + event.getMessage());
      listener.setNotificationLabel(display.getMessageCount(), event.getSeverity());
   }
}
