package org.zanata.webtrans.client.ui;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ReviewContentWidget extends Composite implements ReviewContentWrapper
{
   private static ReviewContentWidgetUiBinder ourUiBinder = GWT.create(ReviewContentWidgetUiBinder.class);

   @UiField
   HTMLPanel root;
   @UiField
   CodeMirrorReadOnlyWidget textArea;
   @UiField
   Style style;

   public ReviewContentWidget()
   {
      initWidget(ourUiBinder.createAndBindUi(this));
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      if (errors.isEmpty())
      {
         root.removeStyleName(style.hasValidationError());
      }
      else
      {
         root.addStyleName(style.hasValidationError());
      }
   }

   @Override
   public String getText()
   {
      return textArea.getText();
   }

   @Override
   public void setText(String text)
   {
      textArea.setText(text);
   }

   @Override
   public void refresh()
   {
      textArea.refresh();
   }

   interface ReviewContentWidgetUiBinder extends UiBinder<HTMLPanel, ReviewContentWidget>
   {
   }

   interface Style extends CssResource
   {

      String hasValidationError();
   }
}