
package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;


public class ImageLabel extends Composite
{

   private static ImageLabelBinder uiBinder = GWT.create(ImageLabelBinder.class);

   interface ImageLabelBinder extends UiBinder<HorizontalPanel, ImageLabel>
   {
   }
   
   @UiField
   Image image;
   
   @UiField
   InlineLabel label;
   
   public ImageLabel()
   {
      initWidget(uiBinder.createAndBindUi(this));
   }
   
   public ImageLabel(ImageResource imageResource, String label)
   {
      this();
      this.image.setResource(imageResource);
      this.label.setText(label);
   }
   
   public ImageLabel(String imageUrl, String label)
   {
      this();
      this.image.setUrl(imageUrl);
      this.label.setText(label);
   }

   public void setLabelStyle(String style)
   {
      this.label.setStyleName(style);
   }

   public void setImageStyle(String style)
   {
      this.image.setStyleName(style);
   }
}
