package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.WebTransMessages;

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
      initWidget(uiBinder.createAndBindUi(this));
   }
}
