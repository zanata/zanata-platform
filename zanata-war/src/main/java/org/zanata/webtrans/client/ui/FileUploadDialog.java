package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FileUploadDialog extends DialogBox
{
   private final FileUpload upload;
   private final CheckBox merge;
   private final PushButton cancelButton;
   private final PushButton uploadButton;
   private final Image loadingIcon;
   private final FormPanel form;

   private final Hidden projectSlug;
   private final Hidden versionSlug;
   private final Hidden docId;
   private final Hidden fileName;
   private final Hidden targetLocale;
   private final Hidden mergeTranslation;

   public FileUploadDialog(Resources resources)
   {
      setText("File upload");
      setGlassEnabled(true);
      setAutoHideEnabled(true);
      setStyleName("gwt-DialogBox-NoFixedSize");

      VerticalPanel panel = new VerticalPanel();
      
      loadingIcon = new Image(resources.spinner());
      loadingIcon.setVisible(false);

      upload = new FileUpload();
      upload.setName("uploadFileElement");

      merge = new CheckBox("Merge?");
      merge.setValue(true);

      cancelButton = new PushButton("Cancel");
      cancelButton.addStyleName("button");
      uploadButton = new PushButton("Upload");
      uploadButton.addStyleName("button");

      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setStyleName("buttonPanel");
      buttonPanel.add(loadingIcon);
      buttonPanel.add(cancelButton);
      buttonPanel.add(uploadButton);

      panel.add(upload);
      panel.add(merge);
      panel.add(buttonPanel);

      projectSlug = new Hidden("projectSlug");
      versionSlug = new Hidden("versionSlug");
      docId = new Hidden("docId");
      fileName = new Hidden("fileName");
      targetLocale = new Hidden("targetLocale");
      mergeTranslation = new Hidden("merge");

      panel.add(projectSlug);
      panel.add(versionSlug);
      panel.add(docId);
      panel.add(fileName);
      panel.add(targetLocale);
      panel.add(mergeTranslation);

      panel.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

      // Because we're going to add a FileUpload widget, we'll need to set
      // the
      // form to use the POST method, and multipart MIME encoding.
      form = new FormPanel();
      form.setEncoding(FormPanel.ENCODING_MULTIPART);
      form.setMethod(FormPanel.METHOD_POST);
      form.setWidget(panel);

      add(form);
   }

   public void registerHandler(final DocumentListDisplay.Listener listener, String uploadFileURL)
   {
      form.setAction(uploadFileURL);
      cancelButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.cancelFileUpload();
         }
      });

      uploadButton.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.onUploadFile();
         }
      });

      form.addSubmitCompleteHandler(new SubmitCompleteHandler()
      {
         @Override
         public void onSubmitComplete(SubmitCompleteEvent event)
         {
            listener.onFileUploadComplete(event);
         }
      });
   }

   public void submitForm()
   {
      fileName.setValue(getUploadFileName());
      mergeTranslation.setValue(merge.getValue().toString());
      loadingIcon.setVisible(true);
      form.submit();
   }

   public String getUploadFileName()
   {
      String fileName = upload.getFilename();

      if (fileName.contains("/"))
      {
         return fileName.substring(fileName.lastIndexOf("/") + 1);
      }
      else if (fileName.contains("\\"))
      {
         return fileName.substring(fileName.lastIndexOf("\\") + 1);
      }

      return fileName;
   }

   public void setDocumentInfo(DocumentInfo docInfo, WorkspaceId workspaceId)
   {
      projectSlug.setValue(workspaceId.getProjectIterationId().getProjectSlug());
      versionSlug.setValue(workspaceId.getProjectIterationId().getIterationSlug());
      docId.setValue(docInfo.getId().getDocId());
      targetLocale.setValue(workspaceId.getLocaleId().toString());
   }

   @Override
   public void hide()
   {
      loadingIcon.setVisible(false);
      DOM.setElementProperty(upload.getElement(), "value", "");
      super.hide();
   }

}