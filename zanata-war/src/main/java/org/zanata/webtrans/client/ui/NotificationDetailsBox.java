package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.events.NotificationEvent.Severity;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class NotificationDetailsBox extends DialogBox
{

   private final HTML messageWidget;
   private final PushButton closeButton;
   private final HorizontalPanel buttonPanel;
   private final HorizontalPanel topPanel;
   private final InlineLabel severityLabel;
   private final InlineLabel timeLabel;

   public NotificationDetailsBox()
   {
      super(true);
      setStyleName("gwt-DialogBox-NoFixedSize");

      topPanel = new HorizontalPanel();
      severityLabel = new InlineLabel();
      timeLabel = new InlineLabel();
      timeLabel.setStyleName("time_label");
      topPanel.add(severityLabel);
      topPanel.add(timeLabel);
      
      buttonPanel = new HorizontalPanel();
      buttonPanel.setStyleName("buttonPanel");
      
      closeButton = new PushButton("Close");
      closeButton.addStyleName("button");
      
      VerticalPanel panel = new VerticalPanel();
      panel.setWidth("100%");
      
      messageWidget = new HTML();
      messageWidget.addStyleName("message");
      
      buttonPanel.add(closeButton);

      panel.add(topPanel);
      panel.add(messageWidget);
      panel.add(buttonPanel);
      
      panel.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);
      
      add(panel);
      
      registerHandler();
   }
   
   private void registerHandler()
   {
      closeButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            hide();
         }
      });
   }

   public void setMessageDetails(Severity severity, String title, String time, String message, boolean isSafeHtml)
   {
      timeLabel.setText(time);
      severityLabel.setStyleName("icon-info-circle-2");
      severityLabel.addStyleName("severity_icon");
      if(severity == Severity.Error)
      {
         severityLabel.addStyleName("severity_error");
      }
      else if (severity == Severity.Warning)
      {
         severityLabel.addStyleName("severity_warning");
      }
      else 
      {
         severityLabel.addStyleName("severity_info");
      }
      
      messageWidget.setHTML("");
      messageWidget.setText("");
      setText(title);
      if (isSafeHtml)
      {
         SafeHtmlBuilder builder = new SafeHtmlBuilder();
         builder.appendHtmlConstant(message);
         messageWidget.setHTML(builder.toSafeHtml());
      }
      else
      {
         messageWidget.setText(message);
      }
      
      buttonPanel.clear();
      buttonPanel.add(closeButton);
   }
}
