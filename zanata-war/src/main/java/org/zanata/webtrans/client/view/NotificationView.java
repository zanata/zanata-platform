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

import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.NotificationPresenter;
import org.zanata.webtrans.client.resources.Resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class NotificationView extends PopupPanel implements NotificationPresenter.Display
{

   private static NotificationPanelUiBinder uiBinder = GWT.create(NotificationPanelUiBinder.class);

   interface NotificationPanelUiBinder extends UiBinder<Widget, NotificationView>
   {
   }

   interface Styles extends CssResource
   {
      String messageRow();

      String image();
   }

   @UiField
   VerticalPanel messagePanel;

   @UiField
   Anchor dismissLink, clearLink;

   @UiField
   Resources resources;

   @UiField
   Styles style;

   private int messagesToKeep = 1;

   @Inject
   public NotificationView()
   {
      setWidget(uiBinder.createAndBindUi(this));
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

   public HasClickHandlers getClearButton()
   {
      return clearLink;
   }

   @Override
   public void setMessagesToKeep(int count)
   {
      messagesToKeep = count;
   }

   @Override
   public void clearMessages()
   {
      messagePanel.clear();
   }

   public int getWidth()
   {
      // return width of the notification panel, see
      // Style@Notification.ui.xml.mainPanel
      return 400;
   }

   @Override
   public void appendMessage(Severity severity, String msg)
   {
      HorizontalPanel panel = new HorizontalPanel();
      Image severityImg;
      panel.setWidth("100%");
      if (severity == Severity.Error)
      {
         severityImg = new Image(resources.errorMsg());
      }
      else if (severity == Severity.Warning)
      {
         severityImg = new Image(resources.warnMsg());
      }
      else
      {
         severityImg = new Image(resources.infoMsg());
      }
      severityImg.addStyleName(style.image());
      Label msgLabel = new Label(msg);

      panel.add(severityImg);
      panel.add(msgLabel);
      panel.setCellWidth(severityImg, "20px");
      panel.setCellVerticalAlignment(severityImg, HasVerticalAlignment.ALIGN_MIDDLE);
      panel.setCellVerticalAlignment(msgLabel, HasVerticalAlignment.ALIGN_MIDDLE);

      messagePanel.insert(panel, 0);
      messagePanel.getWidget(0).setStyleName(style.messageRow());

      while (messagePanel.getWidgetCount() > messagesToKeep)
      {
         messagePanel.remove(messagePanel.getWidgetCount() - 1);
      }
   }

   @Override
   public int getMessageCount()
   {
      return messagePanel.getWidgetCount();
   }
}
