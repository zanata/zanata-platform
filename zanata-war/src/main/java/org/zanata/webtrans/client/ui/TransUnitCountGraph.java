package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class TransUnitCountGraph extends TransUnitCountBar
{
   private static TransUnitCountGraphUiBinder uiBinder = GWT.create(TransUnitCountGraphUiBinder.class);

   interface TransUnitCountGraphUiBinder extends UiBinder<Widget, TransUnitCountGraph>
   {
   }


   public TransUnitCountGraph(WebTransMessages messages, boolean animate)
   {
      super(messages, animate);
      labelFormat = LabelFormat.PERCENT_COMPLETE;
      initWidget(uiBinder.createAndBindUi(this));
   }

   public void onMouseOver()
   {
      tooltipPanel.showRelativeTo(this);
   }

   public void onMouseOut()
   {
      tooltipPanel.hide(true);
   }


}
