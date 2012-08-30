package org.zanata.webtrans.client.ui;

import com.google.gwt.cell.client.TextCell;

//https://groups.google.com/d/msg/google-web-toolkit/G2Jz-FhZCJY/GT7AxrY9w9gJ
public abstract class TooltipTextColumn<T> extends TooltipColumn<T, String> 
{ 

   /** 
    * Construct a new TextColumn. 
    */ 
   public TooltipTextColumn() 
   { 
      super(new TextCell()); 
   } 

} 