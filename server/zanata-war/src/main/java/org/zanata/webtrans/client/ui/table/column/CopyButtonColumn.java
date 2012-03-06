package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.user.cellview.client.Column;

public class CopyButtonColumn extends Column<TranslationMemoryGlossaryItem, String>
{

   public CopyButtonColumn()
   {
      super(new ButtonCell());
   }

   @Override
   public String getValue(TranslationMemoryGlossaryItem object)
   {
      return "Copy";
   }
}
