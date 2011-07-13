package org.zanata.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitCountGraph extends TransUnitCountBar
{
   @UiField
   Styles style;

   private static TransUnitCountGraphUiBinder uiBinder = GWT.create(TransUnitCountGraphUiBinder.class);

   PopupPanel popupPanel;

   interface TransUnitCountGraphUiBinder extends UiBinder<Widget, TransUnitCountGraph>
   {
   }

   interface Styles extends CssResource
   {
      String tooltipPanel();

      String toolTipTable();
   }

   @Inject
   public TransUnitCountGraph(WebTransMessages messages)
   {
      super(messages, 135);
      this.labelFormat = LabelFormat.PERCENT_COMPLETE;
      initWidget(uiBinder.createAndBindUi(this));
      initLayoutPanelHandler();
   }

   private void initLayoutPanelHandler()
   {
      layoutPanel.addHandler(new MouseOverHandler()
      {
         @Override
         public void onMouseOver(MouseOverEvent event)
         {
            popupPanel.showRelativeTo(layoutPanel);
         }
      }, MouseOverEvent.getType());

      layoutPanel.addHandler(new MouseOutHandler()
      {
         @Override
         public void onMouseOut(MouseOutEvent event)
         {
            popupPanel.hide(true);
         }
      }, MouseOutEvent.getType());

      layoutPanel.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
   }

   @Override
   public void refreshPopupPanel()
   {
      HTML message = new HTML(getHTMLTooltip());
      VerticalPanel popUpPanelContents = new VerticalPanel();
      popUpPanelContents.add(message);

      popupPanel = new PopupPanel(true);

      popupPanel.addStyleName(style.tooltipPanel());
      popupPanel.setWidget(popUpPanelContents);
   }

   @Override
   @UiHandler("label")
   public void onLabelClick(ClickEvent event)
   {
      // do nothing
   }

   private String getHTMLTooltip()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<table class='" + style.toolTipTable() + "'>");

      // header
      sb.append("<tr>");
      sb.append("<th>");
      sb.append("</th>");
      sb.append("<th>");
      sb.append("Total");
      sb.append("</th>");
      sb.append("<th>");
      sb.append("Translated");
      sb.append("</th>");
      sb.append("<th>");
      sb.append("Need review");
      sb.append("</th>");
      sb.append("<th>");
      sb.append("Untranslated");
      sb.append("</th>");
      sb.append("</tr>");

      sb.append("<tr>");
      sb.append("<th>");
      sb.append("Words");
      sb.append("</th>");
      sb.append("<td>");
      sb.append(getWordsTotal());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(getWordsApproved());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(getWordsNeedReview());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(getWordsUntranslated());
      sb.append("</td>");
      sb.append("</tr>");

      sb.append("<tr>");
      sb.append("<th>");
      sb.append("Units");
      sb.append("</th>");
      sb.append("<td>");
      sb.append(getUnitTotal());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(getUnitApproved());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(getUnitNeedReview());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(getUnitUntranslated());
      sb.append("</td>");
      sb.append("</tr>");

      sb.append("</table>");

      return sb.toString();
   }
}
