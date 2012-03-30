package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.ui.table.cell.ClickableImageResourceCell;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;

public class DetailsColumn<T> extends Column<T, ImageResource>
{
   private final Resources imageResource;
   
   public DetailsColumn(Resources imageResource)
   {
      super(new ClickableImageResourceCell());
      this.imageResource = imageResource;
   }

   @Override
   public ImageResource getValue(T object)
   {
      return imageResource.informationImage();
   }

}
