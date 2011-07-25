package org.zanata.webtrans.client;

import org.zanata.webtrans.client.ui.TooltipPopupPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitCountGraph extends TransUnitCountBar
{
   private static TransUnitCountGraphUiBinder uiBinder = GWT.create(TransUnitCountGraphUiBinder.class);

   final TooltipPopupPanel tooltipPanel = new TooltipPopupPanel(true);

   interface TransUnitCountGraphUiBinder extends UiBinder<Widget, TransUnitCountGraph>
   {
   }

   @Inject
   public TransUnitCountGraph(WebTransMessages messages)
   {
      super(messages, true);
      this.labelFormat = LabelFormat.PERCENT_COMPLETE;
      tooltipPanel.setStyleName("transUnitCountGraphTooltipPanel");

      initWidget(uiBinder.createAndBindUi(this));
   }

   public void onMouseOver(Element target)
   {
      tooltipPanel.showRelativeTo(target);
   }

   public void onMouseOut()
   {
      tooltipPanel.hide(true);
   }

   @Override
   protected void refreshDisplay(int duration)
   {
      tooltipPanel.refreshData(this);
      layoutPanel.animate(0);
   }


   @Override
   @UiHandler("label")
   public void onLabelClick(ClickEvent event)
   {
      // do nothing
   }

   @Override
   protected void setLabelText(int total, int approved, int needReview, int untranslated)
   {
      label.setText(messages.statusGraphLabelPercentage(approved * 100 / total, needReview * 100 / total, untranslated * 100 / total));
   }

   @Override
   public int getOffsetWidth()
   {
      return 115;
   }

   public String getLabelText()
   {
      return label.getText();
   }
}
