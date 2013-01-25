package org.zanata.webtrans.client.view;

import java.util.Date;
import java.util.List;

import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.TextContentsDisplay;
import org.zanata.webtrans.client.util.DateUtil;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
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
   SimplePanel sourceTextContainer;
   @UiField
   SimplePanel targetTextContainer;

   private final UiMessages messages;
   
   @Inject
   public TransMemoryDetailsView(UiMessages messages)
   {
      dialogBox = uiBinder.createAndBindUi(this);
      dialogBox.setText(messages.translationMemoryDetails());
      this.messages = messages;
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
   public void setLastModified(String lastModifiedBy, Date lastModifiedDate)
   {
      if (!Strings.isNullOrEmpty(lastModifiedBy))
      {
         lastModified.setText(messages.lastModified(DateUtil.formatShortDate(lastModifiedDate), lastModifiedBy));
      }
      else
      {
         lastModified.setText(messages.lastModifiedOn(DateUtil.formatShortDate(lastModifiedDate)));
      }
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
      SafeHtml safeHtml = TextContentsDisplay.asSyntaxHighlight(sourceContents).toSafeHtml();
      sourceTextContainer.setWidget(new InlineHTML(safeHtml));
   }

   @Override
   public void setTarget(List<String> targetContents)
   {
      SafeHtml safeHtml = TextContentsDisplay.asSyntaxHighlight(targetContents).toSafeHtml();
      targetTextContainer.setWidget(new InlineHTML(safeHtml));
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
