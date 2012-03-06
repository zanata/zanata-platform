package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.client.ui.DiffMatchPatchLabel;
import org.zanata.webtrans.client.ui.table.cell.DiffMatchPatchLabelCell;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;

import com.google.gwt.user.cellview.client.Column;

public class DiffMatchPatchLabelColumn extends Column<TranslationMemoryGlossaryItem, DiffMatchPatchLabel>
{

   private final boolean displaySource;
   private final boolean displayTarget;

   public DiffMatchPatchLabelColumn(boolean displaySource, boolean displayTarget)
   {
      super(new DiffMatchPatchLabelCell());
      this.displaySource = displaySource;
      this.displayTarget = displayTarget;
   }

   @Override
   public DiffMatchPatchLabel getValue(TranslationMemoryGlossaryItem object)
   {
      DiffMatchPatchLabel label = new DiffMatchPatchLabel();
      label.setOriginal(object.getQuery());
      if (displaySource)
      {
         label.setText(object.getSource());
      }
      else if (displayTarget)
      {
         label.setText(object.getTarget());
      }
      return label;
   }

}
