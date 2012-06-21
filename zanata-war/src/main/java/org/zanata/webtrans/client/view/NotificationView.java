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

import java.util.Date;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.NotificationPresenter;
import org.zanata.webtrans.client.resources.Resources;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
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

      String timeLabel();
   }

   private final Timer hidePopupTimer = new Timer()
   {
      @Override
      public void run()
      {
         hide(true);
      }
   };
   
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
      return 320;
   }

   @Override
   public void appendMessage(Severity severity, String msg, String linkText, ClickHandler linkClickHandler)
   {
      HorizontalPanel panel = new HorizontalPanel();
      Image severityImg;
      panel.setWidth("100%");

      severityImg = createSeverityImage(severity);
      panel.add(severityImg);

      Label timeLabel = new Label("[" + DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT).format(new Date()) + "]");
      timeLabel.setStyleName(style.timeLabel());
      panel.add(timeLabel);

      Label msgLabel = new Label(msg);
      panel.add(msgLabel);

      if (!Strings.isNullOrEmpty(linkText) && linkClickHandler != null)
      {
         Anchor link = new Anchor(linkText);
         link.addClickHandler(linkClickHandler);
         panel.add(link);
         panel.setCellHorizontalAlignment(link, HasHorizontalAlignment.ALIGN_RIGHT);
      }
      panel.setCellWidth(severityImg, "20px");
      panel.setCellWidth(timeLabel, "50px");

      panel.setCellVerticalAlignment(severityImg, HasVerticalAlignment.ALIGN_MIDDLE);
      panel.setCellVerticalAlignment(timeLabel, HasVerticalAlignment.ALIGN_MIDDLE);
      panel.setCellVerticalAlignment(msgLabel, HasVerticalAlignment.ALIGN_MIDDLE);
      panel.setCellHorizontalAlignment(msgLabel, HasHorizontalAlignment.ALIGN_LEFT);

      messagePanel.insert(panel, 0);
      messagePanel.getWidget(0).setStyleName(style.messageRow());

      while (messagePanel.getWidgetCount() > messagesToKeep)
      {
         messagePanel.remove(messagePanel.getWidgetCount() - 1);
      }
   }

   private Image createSeverityImage(Severity severity)
   {
      Image severityImg;
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
      return severityImg;
   }

   @Override
   public int getMessageCount()
   {
      return messagePanel.getWidgetCount();
   }
   
   @Override
   public void setPopupTopRightCorner()
   {
      super.setPopupPosition(Window.getClientWidth() - (getWidth() + 5), 38);
   }
   
   @Override
   public void hide(boolean autoClosed) 
   {
      hidePopupTimer.cancel();
      super.hide(autoClosed);
   }
   
   @Override
   public void show(int delayMillisToClose)
   {
      hidePopupTimer.cancel();
      super.show();
      hidePopupTimer.schedule(delayMillisToClose);
   }
}
