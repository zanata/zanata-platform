package org.zanata.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitCountGraph extends TransUnitCountBar
{
   private static TransUnitCountGraphUiBinder uiBinder = GWT.create(TransUnitCountGraphUiBinder.class);

   interface TransUnitCountGraphUiBinder extends UiBinder<Widget, TransUnitCountGraph>
   {
   }

   @Inject
   public TransUnitCountGraph(WebTransMessages messages)
   {
      super(messages, true);
      this.labelFormat = LabelFormat.PERCENT_COMPLETE;
      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public int getOffsetWidth()
   {
      return 115;
   }
}
