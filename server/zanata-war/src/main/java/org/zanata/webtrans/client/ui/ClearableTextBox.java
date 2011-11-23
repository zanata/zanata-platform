package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ClearableTextBox extends Composite
{

   private static ClearableTextBoxUiBinder uiBinder = GWT.create(ClearableTextBoxUiBinder.class);

   interface ClearableTextBoxUiBinder extends UiBinder<Widget, ClearableTextBox>
   {
   }

   interface Styles extends CssResource
   {
      String emptyBox();
   }

   private boolean isFocused;
   String emptyText;

   @UiField
   TextBox textBox;

   @UiField
   Image xButton;

   @UiField(provided = true)
   final Resources resources;

   @UiField
   Styles style;

   @Inject
   public ClearableTextBox(final Resources resources, final UiMessages messages)
   {
      this.resources = resources;
      emptyText = messages.typeToEnter();
      initWidget(uiBinder.createAndBindUi(this));
      xButton.setVisible(!textBox.getValue().isEmpty());
      textBox.setText(emptyText);
      textBox.addStyleName(style.emptyBox());
   }

   @UiHandler("xButton")
   public void onXButtonClick(ClickEvent event)
   {
      textBox.setValue("", true);
      textBox.setValue(emptyText);
      textBox.addStyleName(style.emptyBox());
   }

   @UiHandler("textBox")
   public void onTextBoxValueChange(ValueChangeEvent<String> event)
   {
      xButton.setVisible(!event.getValue().isEmpty());
   }

   @UiHandler("textBox")
   public void onTextBoxFocus(FocusEvent event)
   {
      isFocused = true;
      if (textBox.getStyleName().contains(style.emptyBox()))
      {
         textBox.setValue("");
         textBox.removeStyleName(style.emptyBox());
      }
   }

   @UiHandler("textBox")
   public void onTextBoxBlur(BlurEvent event)
   {
      isFocused = false;
      refresh();
   }

   private void refresh()
   {
      if (textBox.getText().isEmpty() || textBox.getStyleName().contains(style.emptyBox()))
      {
         textBox.setValue("");
         textBox.addStyleName(style.emptyBox());
         textBox.setValue(emptyText);
      }
   }

   public void setEmptyText(String emptyText)
   {
      this.emptyText = emptyText;
      refresh();
   }

   public TextBox getTextBox()
   {
      return textBox;
   }

   public boolean isFocused()
   {
      return isFocused;
   }

}
