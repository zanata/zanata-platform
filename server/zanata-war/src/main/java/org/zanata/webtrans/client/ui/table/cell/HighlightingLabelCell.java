package org.zanata.webtrans.client.ui.table.cell;

import org.zanata.webtrans.client.ui.HighlightingLabel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class HighlightingLabelCell extends AbstractCell<HighlightingLabel>
{

   public HighlightingLabelCell()
   {
      super();
   }

   @Override
   public void render(Context context, HighlightingLabel value, SafeHtmlBuilder sb)
   {
      sb.appendHtmlConstant(value.getElement().getString());
   }
}
