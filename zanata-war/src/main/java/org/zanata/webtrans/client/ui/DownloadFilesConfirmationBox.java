package org.zanata.webtrans.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class DownloadFilesConfirmationBox extends DialogBox
{
   private final PushButton cancelButton;
   private final PushButton okButton;
   private final Label message;
   private final Label progressMessage;

   public DownloadFilesConfirmationBox(boolean autoHide, final HasDownloadFileHandler handler)
   {
      super(autoHide);
      setText("Download All Files");
      setGlassEnabled(true);
      setStyleName("DownloadFileDialogBox");

      VerticalPanel panel = new VerticalPanel();

      message = new Label("Your download will be prepared and may take a few minutes to complete. Is this ok?");
      progressMessage = new Label();
      cancelButton = new PushButton("Cancel");
      cancelButton.addStyleName("button");
      okButton = new PushButton("OK");
      okButton.addStyleName("button");

      HorizontalPanel infoPanel = new HorizontalPanel();
      infoPanel.setStyleName("img");
      InlineLabel infoImg = new InlineLabel();
      infoImg.setStyleName("icon-help-circle");
      infoPanel.add(infoImg);
      infoPanel.add(message);
      infoPanel.setCellVerticalAlignment(message, HasVerticalAlignment.ALIGN_MIDDLE);

      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setStyleName("buttonPanel");
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      
      panel.add(infoPanel);
      panel.add(progressMessage);
      panel.add(buttonPanel);
      
      panel.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

      add(panel);
      
      cancelButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            handler.onCancelButtonClicked();
         }
      });
      
      okButton.addClickHandler(new ClickHandler()
      {
         
         @Override
         public void onClick(ClickEvent event)
         {
            handler.onOkButtonClicked();
         }
      });
   }

   public void setProgressMessage(String text)
   {
      progressMessage.setText(text);
   }

   public void hide()
   {
      setProgressMessage("");
      super.hide();
   }
}
