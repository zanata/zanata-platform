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

import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.NotificationPresenter.DisplayOrder;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.ui.NotificationDetailsBox;
import org.zanata.webtrans.client.util.DateUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class NotificationView extends Composite implements NotificationDisplay
{

   private static NotificationPanelUiBinder uiBinder = GWT.create(NotificationPanelUiBinder.class);

   interface NotificationPanelUiBinder extends UiBinder<Widget, NotificationView>
   {
   }

   interface Styles extends CssResource
   {
      String messageRow();

      String severity();

      String timeLabel();

      String inlineLink();

      String messagePanel();

      String mainPanel();

      String link();

      String msgLabel();

      String disabledInlineLink();
   }

   @UiField
   VerticalPanel messagePanel;

   @UiField
   Anchor clearLink;

   @UiField
   Resources resources;

   @UiField
   Styles style;

   @UiField
   ScrollPanel scrollPanel;

   private int messagesToKeep;
   private Listener listener;
   
   private final NotificationDetailsBox detailBox;

   private DisplayOrder displayOrder = DisplayOrder.ASCENDING;

   @Inject
   public NotificationView()
   {
      initWidget(uiBinder.createAndBindUi(this));
      detailBox = new NotificationDetailsBox();
   }

   @UiHandler("clearLink")
   public void onClearButtonClick(ClickEvent event)
   {
      listener.onClearClick();
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

   @Override
   public void appendMessage(final Severity severity, final String summary, final String msg, final boolean displayAsHtml, final InlineLink inlineLink)
   {
      HorizontalPanel panel = new HorizontalPanel();
      panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

      InlineLabel severityImg;

      panel.setWidth("100%");

      severityImg = createSeverityImage(severity);
      panel.add(severityImg);

      final String time = "[" + DateUtil.formatTime(new Date()) + "]";
      Label timeLabel = new Label(time);
      timeLabel.setStyleName(style.timeLabel());
      panel.add(timeLabel);

      Label msgLabel = new Label(summary);
      msgLabel.setStyleName(style.msgLabel());
      msgLabel.addStyleName("pointer");
      
      msgLabel.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            detailBox.setMessageDetails(severity, summary, time, msg, displayAsHtml);
            detailBox.center();
         }
      });
      
      panel.add(msgLabel);
      if (inlineLink != null)
      {
         inlineLink.setLinkStyle(style.inlineLink());
         inlineLink.setDisabledStyle(style.disabledInlineLink());
         panel.add(inlineLink);
         panel.setCellWidth(inlineLink, "16px");
      }

      panel.setCellWidth(severityImg, "20px");
      panel.setCellWidth(timeLabel, "42px");
      panel.setCellHorizontalAlignment(msgLabel, HasHorizontalAlignment.ALIGN_LEFT);
      
      if (displayOrder == DisplayOrder.ASCENDING)
      {
         messagePanel.insert(panel, 0);

         while (messagePanel.getWidgetCount() > messagesToKeep)
         {
            messagePanel.remove(messagePanel.getWidgetCount() - 1);
         }
         scrollPanel.scrollToTop();
      }
      else if (displayOrder == DisplayOrder.DESCENDING)
      {
         messagePanel.add(panel);

         while (messagePanel.getWidgetCount() > messagesToKeep)
         {
            messagePanel.remove(0);
         }
         scrollPanel.scrollToBottom();
      }

      messagePanel.getWidget(messagePanel.getWidgetIndex(panel)).setStyleName(style.messageRow());
   }

   private InlineLabel createSeverityImage(Severity severity)
   {
      InlineLabel severityImg = new InlineLabel();

      severityImg.setStyleName("icon-info-circle-2");
      severityImg.addStyleName(style.severity());

      if (severity == Severity.Error)
      {
         severityImg.addStyleName("severity_error");
      }
      else if (severity == Severity.Warning)
      {
         severityImg.addStyleName("severity_warning");
      }
      else
      {
         severityImg.addStyleName("severity_info");
      }

      return severityImg;
   }

   @Override
   public int getMessageCount()
   {
      return messagePanel.getWidgetCount();
   }

   @Override
   public void setMessageOrder(DisplayOrder displayOrder)
   {
      this.displayOrder = displayOrder;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }
}
