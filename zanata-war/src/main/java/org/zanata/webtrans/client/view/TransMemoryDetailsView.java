package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.resources.UiMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransMemoryDetailsView implements TransMemoryDetailsDisplay
{

   private Listener listener;

   interface TMIUiBinder extends UiBinder<DialogBox, TransMemoryDetailsView>
   {
   }

   private static TMIUiBinder uiBinder = GWT.create(TMIUiBinder.class);

   DialogBox dialogBox;

   @UiField
   Label sourceComment, targetComment;
   
   @UiField
   InlineLabel projectIterationName, docName;
   
   @UiField
   Label lastModified;
   
   @UiField
   Button dismissButton;

   @UiField
   ListBox documentListBox;
   @UiField
   HTMLPanel sourceTextContainer;
   @UiField
   HTMLPanel targetTextContainer;

   @Inject
   public TransMemoryDetailsView(UiMessages messages)
   {
      dialogBox = uiBinder.createAndBindUi(this);
      dialogBox.setText(messages.translationMemoryDetails());
      dismissButton.setText(messages.dismiss());
   }

   public void hide()
   {
      dialogBox.hide();
   }

   public void show()
   {
      dialogBox.center();
   }

   @Override
   public Widget asWidget()
   {
      return dialogBox;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public void setSourceComment(String sourceComment)
   {
      this.sourceComment.setText(sourceComment);
   }

   @Override
   public void setTargetComment(String targetComment)
   {
      this.targetComment.setText(targetComment);
   }

   @Override
   public void setProjectIterationName(String projectIterationName)
   {
      this.projectIterationName.setText(projectIterationName);
   }

   @Override
   public void setDocumentName(String documentName)
   {
      docName.setText(documentName);
   }

   @Override
   public void setLastModified(String lastModified)
   {
      this.lastModified.setText(lastModified);
   }

   @Override
   public void clearSourceAndTarget()
   {
      sourceTextContainer.clear();
      targetTextContainer.clear();
   }

   @Override
   public void setSource(List<String> sourceContents)
   {
      for (String sourceContent : sourceContents)
      {
         InlineHTML source = new InlineHTML(sourceContent);
         sourceTextContainer.add(source);
      }
   }

   @Override
   public void setTarget(List<String> targetContents)
   {
      for (String targetContent : targetContents)
      {
         InlineHTML target = new InlineHTML(targetContent);
         targetTextContainer.add(target);
      }
   }

   @UiHandler("dismissButton")
   public void onDismissButtonClick(ClickEvent event)
   {
      listener.dismissTransMemoryDetails();
   }

   @UiHandler("documentListBox")
   public void onDocumentListBoxChange(ChangeEvent event)
   {
      listener.onDocumentListBoxChanged();
   }

   @Override
   public int getSelectedDocumentIndex()
   {
      return documentListBox.getSelectedIndex();
   }

   @Override
   public void addDoc(String text)
   {
      documentListBox.addItem(text);
   }

   @Override
   public void clearDocs()
   {
      documentListBox.clear();
   }
}
