/**
 * 
 */
package org.zanata.webtrans.client.ui.table.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 * @author aeng
 *
 */
public abstract class TooltipColumn<T,C> extends Column<T,C>
{
   private final Cell<C> cell; 
   
   public TooltipColumn(Cell<C> cell)
   {
      super(cell);
      this.cell = cell;
   }

   @Override
   public void render(Context context, T object, SafeHtmlBuilder sb)
   {
      SafeHtmlBuilder shb = new SafeHtmlBuilder(); 
      
      String tooltip = getTitle(object);
      SafeHtml span = SafeHtmlUtils.fromTrustedString("<span title=\"" + tooltip + "\">");
      
      shb.append(span);
      cell.render(context, getValue(object), shb);
      shb.appendHtmlConstant("</span>"); 
      
      sb.append(shb.toSafeHtml());
   }
   
   public abstract String getTitle(T object); 

}
