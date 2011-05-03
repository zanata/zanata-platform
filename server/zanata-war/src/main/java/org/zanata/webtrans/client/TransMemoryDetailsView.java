package org.zanata.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class TransMemoryDetailsView implements TransMemoryDetailsPresenter.Display
{

   interface TMIUiBinder extends UiBinder<DialogBox, TransMemoryDetailsView>
   {
   }

   private static TMIUiBinder uiBinder = GWT.create(TMIUiBinder.class);

   DialogBox dialogBox;

   @UiField
   TextArea sourceText;
   @UiField
   TextArea targetText;
   @UiField
   TextArea sourceComment;
   @UiField
   TextArea targetComment;
   @UiField
   Label projectName;
   @UiField
   Label iterationName;
   @UiField
   Label docName;
   @UiField
   Button dismissButton;

   @UiField
   ListBox documentListBox;

   public TransMemoryDetailsView()
   {
      dialogBox = uiBinder.createAndBindUi(this);
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
   public HasText getProjectName()
   {
      return projectName;
   }

   @Override
   public HasText getIterationName()
   {
      return iterationName;
   }

   @Override
   public HasText getDocumentName()
   {
      return docName;
   }

   @Override
   public HasText getSourceComment()
   {
      return sourceComment;
   }

   @Override
   public HasText getSourceText()
   {
      return sourceText;
   }

   @Override
   public HasText getTargetComment()
   {
      return targetComment;
   }

   @Override
   public HasText getTargetText()
   {
      return targetText;
   }

   @Override
   public HasChangeHandlers getDocumentListBox()
   {
      return documentListBox;
   }

   @Override
   public HasClickHandlers getDismissButton()
   {
      return dismissButton;
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
