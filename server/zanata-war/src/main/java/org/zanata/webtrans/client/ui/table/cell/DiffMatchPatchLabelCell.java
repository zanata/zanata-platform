package org.zanata.webtrans.client.ui.table.cell;

import org.zanata.webtrans.client.ui.DiffMatchPatchLabel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class DiffMatchPatchLabelCell extends AbstractCell<DiffMatchPatchLabel>
{

   public DiffMatchPatchLabelCell()
   {
      super();
   }

   @Override
   public void render(Context context, DiffMatchPatchLabel value, SafeHtmlBuilder sb)
   {
      sb.appendHtmlConstant(value.getElement().getString());
   }
}
