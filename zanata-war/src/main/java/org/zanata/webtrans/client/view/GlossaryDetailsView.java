package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.GlossaryDetailsPresenter;

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

public class GlossaryDetailsView implements GlossaryDetailsPresenter.Display
{

   interface TMIUiBinder extends UiBinder<DialogBox, GlossaryDetailsView>
   {
   }

   private static TMIUiBinder uiBinder = GWT.create(TMIUiBinder.class);

   DialogBox dialogBox;

   @UiField
   TextArea srcRef;

   @UiField
   TextArea sourceText;

   @UiField
   TextArea targetText;

   @UiField
   TextArea sourceComment;

   @UiField
   Label sourceLabel;

   @UiField
   Label targetLabel;

   @UiField
   TextArea targetComment;

   @UiField
   Button dismissButton;

   @UiField
   ListBox entryListBox;

   public GlossaryDetailsView()
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
   public HasChangeHandlers getEntryListBox()
   {
      return entryListBox;
   }

   @Override
   public HasClickHandlers getDismissButton()
   {
      return dismissButton;
   }

   @Override
   public int getSelectedDocumentIndex()
   {
      return entryListBox.getSelectedIndex();
   }

   @Override
   public void addEntry(String text)
   {
      entryListBox.addItem(text);
   }

   @Override
   public void clearEntries()
   {
      entryListBox.clear();
   }

   @Override
   public HasText getSourceLabel()
   {
      return sourceLabel;
   }

   @Override
   public HasText getTargetLabel()
   {
      return targetLabel;
   }

   @Override
   public HasText getSrcRef()
   {
      return srcRef;
   }

}
