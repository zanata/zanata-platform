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
package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class NotificationPanel extends PopupPanel implements HasNotificationData
{

   private static NotificationPanelUiBinder uiBinder = GWT.create(NotificationPanelUiBinder.class);

   interface NotificationPanelUiBinder extends UiBinder<Widget, NotificationPanel>
   {
   }

   @UiField
   VerticalPanel messagePanel;

   @UiField
   Anchor dismissLink;

   private int messagesToKeep = 1;

   public NotificationPanel(boolean modal, boolean autoHide, boolean animation)
   {
      setWidget(uiBinder.createAndBindUi(this));
      this.setModal(modal);
      this.setAutoHideEnabled(autoHide);
      this.setAnimationEnabled(animation);

      this.setStyleName("notificationPanel");
   }

   @Override
   public void appendMessage(String msg)
   {
      messagePanel.insert(new Label(msg), 0);

      while (messagePanel.getWidgetCount() > messagesToKeep)
      {
         messagePanel.remove(messagePanel.getWidgetCount() - 1);
      }
   }

   public HasClickHandlers getDismissButton()
   {
      return dismissLink;
   }

   @Override
   public void setMessagesToKeep(int count)
   {
      messagesToKeep = count;
   }
}
