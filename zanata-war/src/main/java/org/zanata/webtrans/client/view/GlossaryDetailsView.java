package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.presenter.GlossaryDetailsPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GlossaryDetailsView implements GlossaryDetailsPresenter.Display
{

   interface GlossaryIUiBinder extends UiBinder<DialogBox, GlossaryDetailsView>
   {
   }

   interface Styles extends CssResource
   {
      String targetCommentListButton();

      String targetCommentTextArea();
   }

   private static GlossaryIUiBinder uiBinder = GWT.create(GlossaryIUiBinder.class);

   DialogBox dialogBox;

   @UiField
   TextArea srcRef, sourceText, targetText, newTargetComment;

   @UiField
   Label sourceLabel, targetLabel, lastModified;

   @UiField
   ListBox sourceComment, entryListBox;

   @UiField
   Button dismissButton, saveButton, addNewCommentButton;

   @UiField
   FlexTable targetCommentsTable;

   @UiField
   ScrollPanel targetCommentScrollTable;

   @UiField
   Image loadingIcon;

   @UiField
   Styles style;

   private final int VISIBLE_COMMENTS = 4;

   private boolean hasGlossaryUpdateAccess;

   private class DeleteRowHandler implements ClickHandler
   {
      private final Widget panel;

      public DeleteRowHandler(Widget panel)
      {
         this.panel = panel;
      }

      @Override
      public void onClick(ClickEvent event)
      {
         targetCommentsTable.remove(panel);

         // Clean up empty <tr> tag in the table
         for (int i = 0; i < targetCommentsTable.getRowCount(); i++)
         {
            Widget widget = targetCommentsTable.getWidget(i, 0);
            if (widget == null)
            {
               targetCommentsTable.removeRow(i);
            }
         }
      }
   }

   @Inject
   public GlossaryDetailsView(UiMessages messages, Resources resources)
   {
      dialogBox = uiBinder.createAndBindUi(this);
      dialogBox.setText(messages.glossaryDetails());
      dismissButton.setText(messages.dismiss());
      saveButton.setText(messages.save());
      sourceComment.setVisibleItemCount(VISIBLE_COMMENTS);

      sourceComment.setEnabled(false);
      sourceText.setEnabled(false);
      srcRef.setEnabled(false);

      targetCommentsTable.setCellPadding(0);
      targetCommentsTable.setCellSpacing(1);

      addNewCommentButton.addStyleName("icon-plus-1");
      targetCommentScrollTable.setAlwaysShowScrollBars(true);

      loadingIcon.setResource(resources.spinner());
      loadingIcon.setVisible(false);
   }

   public void hide()
   {
      dialogBox.hide();
   }

   public void show()
   {
      dialogBox.center();
      targetCommentScrollTable.scrollToBottom();
   }

   @Override
   public Widget asWidget()
   {
      return dialogBox;
   }

   @Override
   public void setSourceComment(List<String> comments)
   {
      sourceComment.clear();
      for (String comment : comments)
      {
         sourceComment.addItem(comment);
      }
   }

   @Override
   public HasText getSourceText()
   {
      return sourceText;
   }

   private FlowPanel getTargetCommentRow(String comment)
   {
      FlowPanel panel = new FlowPanel();

      TextArea commentArea = new TextArea();
      commentArea.setStyleName(style.targetCommentTextArea());
      commentArea.setVisibleLines(2);
      commentArea.setValue(comment);

      if (!hasGlossaryUpdateAccess)
      {
         commentArea.setEnabled(false);
      }
      panel.add(commentArea);

      if (hasGlossaryUpdateAccess)
      {
         Button deleteButton = new Button();
         deleteButton.setStyleName("icon-minus-1");
         deleteButton.addStyleName(style.targetCommentListButton());
         deleteButton.addClickHandler(new DeleteRowHandler(panel));
         panel.add(deleteButton);
      }
      return panel;
   }

   @Override
   public void setTargetComment(List<String> comments)
   {
      targetCommentsTable.clear();
      for (int i = 0; i < comments.size(); i++)
      {
         String comment = comments.get(i);
         targetCommentsTable.setWidget(i, 0, getTargetCommentRow(comment));
      }
   }

   @Override
   public List<String> getCurrentTargetComments()
   {
      ArrayList<String> currentComments = new ArrayList<String>();

      for (int i = 0; i < targetCommentsTable.getRowCount(); i++)
      {
         FlowPanel panel = (FlowPanel) targetCommentsTable.getWidget(i, 0);
         TextArea textArea = (TextArea) panel.getWidget(0);
         if (!Strings.isNullOrEmpty(textArea.getText()))
         {
            currentComments.add(textArea.getText());
         }
      }
      return currentComments;
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
   public HasClickHandlers getSaveButton()
   {
      return saveButton;
   }

   @Override
   public HasClickHandlers getAddNewCommentButton()
   {
      return addNewCommentButton;
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

   @Override
   public HasText getLastModified()
   {
      return lastModified;
   }

   @Override
   public HasText getNewCommentText()
   {
      return newTargetComment;
   }

   @Override
   public void addRowIntoTargetComment(int row, String comment)
   {
      targetCommentsTable.setWidget(row, 0, getTargetCommentRow(comment));
      targetCommentScrollTable.scrollToBottom();
   }

   @Override
   public int getTargetCommentRowCount()
   {
      return targetCommentsTable.getRowCount();
   }

   @Override
   public void showLoading(boolean visible)
   {
      loadingIcon.setVisible(visible);
   }

   @Override
   public void setHasUpdateAccess(boolean hasGlossaryUpdateAccess)
   {
      saveButton.setEnabled(hasGlossaryUpdateAccess);
      newTargetComment.setEnabled(hasGlossaryUpdateAccess);
      targetText.setEnabled(hasGlossaryUpdateAccess);
      addNewCommentButton.setVisible(hasGlossaryUpdateAccess);
      this.hasGlossaryUpdateAccess = hasGlossaryUpdateAccess;
   }

}
