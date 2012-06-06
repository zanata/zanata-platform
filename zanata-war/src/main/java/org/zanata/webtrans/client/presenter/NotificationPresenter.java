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
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.NotificationEventHandler;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class NotificationPresenter extends WidgetPresenter<NotificationPresenter.Display>
{
   @Inject
   public NotificationPresenter(Display display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getDismissButton();

      HasClickHandlers getClearButton();

      void clearMessages();

      void setModal(boolean modal);

      void setAutoHideEnabled(boolean autoHide);
      
      void setAnimationEnabled(boolean enable);

      void hide(boolean autoClosed);

      void appendMessage(Severity severity, String message);

      void setMessagesToKeep(int count);

      void appendMessage(String message);

      void show();

      int getMessageCount();

      void setPopupTopRightCorner();

      void show(int delayMillisToClose);
   }

   private HasNotificationLabel listener;

   private static final int MESSAGE_TO_KEEP = 6;

   @Override
   protected void onBind()
   {
      display.setModal(false);
      display.setAutoHideEnabled(true);
      display.setAnimationEnabled(true);
      display.hide(true);
      display.setMessagesToKeep(MESSAGE_TO_KEEP);
      display.setPopupTopRightCorner();

      registerHandler(display.getDismissButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.hide(true);
         }
      }));

      registerHandler(display.getClearButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.clearMessages();
            display.hide(true);
            listener.setNotificationLabel(display.getMessageCount(), Severity.Info);
         }
      }));

      registerHandler(eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler()
      {
         @Override
         public void onNotification(NotificationEvent event)
         {
            appendNotification(event.getSeverity(), event.getMessage());
            Log.info("Notification:" + event.getMessage());
            listener.setNotificationLabel(display.getMessageCount(), event.getSeverity());
         }
      }));
   }

   public void setNotificationListener(HasNotificationLabel listener)
   {
      this.listener = listener;
      listener.setNotificationLabel(0, Severity.Info);
   }

   private void showNotification()
   {
      display.show(2500);
   }

   public void showNotificationWithNoTimer()
   {
      display.show();
   }

   private void appendNotification(Severity severity, String msg)
   {
      display.appendMessage(severity, msg);
      if (severity == Severity.Error)
      {
         showNotificationWithNoTimer();
      }
      else
      {
         showNotification();
      }
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub
   }

   @Override
   protected void onRevealDisplay()
   {
      // TODO Auto-generated method stub
   }
}
