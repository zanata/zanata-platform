package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.ui.table.cell.ClickableImageResourceCell;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;

public class DetailsColumn extends Column<TranslationMemoryGlossaryItem, ImageResource>
{
   private final Resources imageResource;
   
   public DetailsColumn(Resources imageResource)
   {
      super(new ClickableImageResourceCell());
      this.imageResource = imageResource;
   }

   @Override
   public ImageResource getValue(TranslationMemoryGlossaryItem object)
   {
      return imageResource.informationImage();
   }

}
