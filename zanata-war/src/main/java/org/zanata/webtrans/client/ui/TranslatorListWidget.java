package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TranslatorListWidget extends Composite
{
   private static TranslatorListWidgetUiBinder ourUiBinder = GWT.create(TranslatorListWidgetUiBinder.class);
   @UiField
   Styles style;
   @UiField
   HTMLPanel container;

   private boolean isEmpty = true;


   public TranslatorListWidget()
   {
      initWidget(ourUiBinder.createAndBindUi(this));

   }

   public void addTranslator(String name, String color)
   {
      Label nameLabel = new Label(name);
      nameLabel.setStyleName(style.userLabel());

      nameLabel.getElement().getStyle().setProperty("backgroundColor", color);
      nameLabel.getElement().getStyle().setProperty("borderColor", color);
      nameLabel.getElement().getStyle().setProperty("borderWidth", "1px");
      nameLabel.getElement().getStyle().setProperty("borderStyle", "solid");

      container.add(nameLabel);
      isEmpty = false;
   }

   public void clearTranslatorList()
   {
      container.clear();
      isEmpty = true;
   }

   public void removeTranslator(String name, String color)
   {

      for (int i = 0; i < container.getWidgetCount(); i++)
      {
         Label translatorLabel = (Label) container.getWidget(i);

         if (translatorLabel.getText().equals(name) && removeFormat(translatorLabel.getElement().getStyle().getProperty("backgroundColor")).equals(removeFormat(color)))
         {
            container.remove(i);
         }
      }

      isEmpty = container.getWidgetCount() == 0;
   }

   public boolean isEmpty()
   {
      return isEmpty;
   }

   /**
    * Color string return from userSessionService rgb(xx,xx,xx), Color string
    * return from browser is formatted rgb(xx, xx, xx). Method needed to
    * unformat all color
    *
    * @param color color
    */
   private String removeFormat(String color)
   {
      return color.replace(" ", "");
   }

   interface TranslatorListWidgetUiBinder extends UiBinder<Widget, TranslatorListWidget>
   {
   }

   interface Styles extends CssResource
   {

      String translatorList();

      String userLabel();
   }
}