package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.client.ui.table.cell.HighlightingLabelCell;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;

import com.google.gwt.user.cellview.client.Column;

public class HighlightingLabelColumn extends Column<TranslationMemoryGlossaryItem, HighlightingLabel>
{
   private final boolean displaySource;
   private final boolean displayTarget;

   public HighlightingLabelColumn(boolean displaySource, boolean displayTarget)
   {
      super(new HighlightingLabelCell());
      this.displaySource = displaySource;
      this.displayTarget = displayTarget;
   }

   @Override
   public HighlightingLabel getValue(TranslationMemoryGlossaryItem object)
   {
      HighlightingLabel label = new HighlightingLabel();
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
