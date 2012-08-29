package org.zanata.webtrans.client.ui;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

// See https://groups.google.com/d/msg/google-web-toolkit/G2Jz-FhZCJY/GT7AxrY9w9gJ 
// for original solution which allows SafeHtml for tooltip, not just text 
public abstract class TooltipColumn<T, C> extends Column<T, C>
{ 
   private final Cell<C> cell; 

   public TooltipColumn(Cell<C> cell) 
   { 
      super(cell); 
      this.cell = cell; 
   } 

   public abstract String getTooltipValue(T object); 
   
   @Override 
   public void render(Context context, T object, SafeHtmlBuilder sb) 
   { 
      SafeHtmlBuilder shb = new SafeHtmlBuilder(); 

      String tooltip = getTooltipValue(object);
      SafeHtml span = SafeHtmlUtils.fromTrustedString("<span title=\"" + tooltip + "\">");

      shb.append(span);
      cell.render(context, getValue(object), shb);
      shb.appendHtmlConstant("</span>"); 
      sb.append(shb.toSafeHtml());
   } 
}
