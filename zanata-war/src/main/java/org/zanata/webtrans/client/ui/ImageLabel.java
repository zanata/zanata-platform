
package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;


public class ImageLabel extends Composite implements HasClickHandlers
{

   private static ImageLabelBinder uiBinder = GWT.create(ImageLabelBinder.class);

   interface ImageLabelBinder extends UiBinder<HorizontalPanel, ImageLabel>
   {
   }
   
   @UiField
   Image image, image2;
   
   @UiField
   InlineLabel label;
   
   @UiField
   HorizontalPanel container;

   public ImageLabel(ImageResource imageResource, String label, ImageResource imageResource2)
   {
      initWidget(uiBinder.createAndBindUi(this));

      if (imageResource == null)
      {
         image.setVisible(false);
      }
      else
      {
         image.setResource(imageResource);
      }

      this.label.setText(label);

      if (imageResource2 == null)
      {
         image2.setVisible(false);
      }
      else
      {
         image2.setResource(imageResource2);
      }
      sinkEvents(Event.ONCLICK);
   }
   
   public ImageLabel(String imageUrl, String label, ImageResource imageResource2)
   {
      initWidget(uiBinder.createAndBindUi(this));

      if (imageUrl == null)
      {
         image.setVisible(false);
      }
      else
      {
         image.setUrl(imageUrl);
      }

      this.label.setText(label);

      if (imageResource2 == null)
      {
         image2.setVisible(false);
      }
      else
      {
         image2.setResource(imageResource2);
      }
      sinkEvents(Event.ONCLICK);
   }

   public ImageLabel(String imageUrl, String label)
   {
      initWidget(uiBinder.createAndBindUi(this));
      this.image.setUrl(imageUrl);
      this.label.setText(label);
      sinkEvents(Event.ONCLICK);
   }

   public void setLabelStyle(String style)
   {
      this.label.setStyleName(style);
   }

   public void setImageStyle(String style)
   {
      this.image.setStyleName(style);
      this.image2.setStyleName(style);
   }

   @Override
   public HandlerRegistration addClickHandler(ClickHandler handler)
   {
      return addHandler(handler, ClickEvent.getType());
   }
}
