package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.view.DocumentListDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
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

   private final HorizontalPanel infoPanel;
   private final HorizontalPanel progressPanel;
   private final Label progressMessage;
   private final Anchor downloadLink;
   private final Image progressImage;

   private final PushButton cancelButton;
   private final PushButton okButton;

   private final String defaultMessage = "Your download will be prepared and may take a few minutes to complete. Is this ok?";

   public DownloadFilesConfirmationBox(boolean autoHide, final Resources resources)
   {
      super(autoHide);
      setText("Download All Files");
      setGlassEnabled(true);
      setStyleName("gwt-DialogBox-NoFixedSize");

      VerticalPanel panel = new VerticalPanel();

      Label infoMessage = new Label(defaultMessage);
      InlineLabel infoImg = new InlineLabel();
      infoImg.setStyleName("icon-help-circle");

      infoPanel = new HorizontalPanel();
      infoPanel.setStyleName("info");
      infoPanel.add(infoImg);
      infoPanel.add(infoMessage);
      infoPanel.setCellVerticalAlignment(infoMessage, HasVerticalAlignment.ALIGN_MIDDLE);

      cancelButton = new PushButton("Cancel");
      cancelButton.addStyleName("button");

      okButton = new PushButton("OK");
      okButton.addStyleName("button");

      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setStyleName("buttonPanel");
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);

      progressImage = new Image(resources.progressLoading());
      progressMessage = new Label();

      progressPanel = new HorizontalPanel();
      progressPanel.setSpacing(5);
      progressPanel.setStyleName("progress");
      progressPanel.setVisible(false);
      showDownloadLink(false);


      panel.add(infoPanel);
      panel.add(progressPanel);
      panel.add(buttonPanel);

      panel.setCellHorizontalAlignment(infoPanel, HasHorizontalAlignment.ALIGN_CENTER);
      panel.setCellHorizontalAlignment(progressPanel, HasHorizontalAlignment.ALIGN_CENTER);
      panel.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

      add(panel);

      downloadLink = new Anchor("Click here to download");
      downloadLink.setTarget("_blank");
   }

   public void registerHandler(final DocumentListDisplay.Listener listener)
   {
      cancelButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.cancelDownloadAllFiles();
         }
      });

      okButton.addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            listener.downloadAllFiles();
         }
      });
   }

   public void showDownloadLink(boolean show)
   {
      progressPanel.clear();
      if(show)
      {
         progressPanel.add(downloadLink);
      }
      else
      {
         progressPanel.add(progressImage);
         progressPanel.add(progressMessage);

         progressPanel.setCellVerticalAlignment(progressImage, HasVerticalAlignment.ALIGN_MIDDLE);
         progressPanel.setCellVerticalAlignment(progressMessage, HasVerticalAlignment.ALIGN_MIDDLE);
      }
   }

   public void setProgressMessage(String text)
   {
      progressMessage.setText(text);
   }

   public void setInProgress(boolean inProgress)
   {
      progressPanel.setVisible(inProgress);
   }

   public void hide()
   {
      setInProgress(false);
      showDownloadLink(false);
     super.hide();
   }

   public void setDownloadLink(String url)
   {
      downloadLink.setHref(url);
   }
}
