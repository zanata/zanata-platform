package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.TransUnitCountBar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TooltipPopupPanel extends PopupPanel
{
   final VerticalPanel popUpPanelContents = new VerticalPanel();

   public void refreshData(TransUnitCountBar stats)
   {
      HTML message = new HTML(getHTMLTooltip(stats));
      popUpPanelContents.clear();
      popUpPanelContents.add(message);
   }

   public TooltipPopupPanel(boolean autoHide)
   {
      super(autoHide);
      this.setWidget(popUpPanelContents);
   }

   private String getHTMLTooltip(TransUnitCountBar graph)
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<table class='transUnitCountGraphTooltipTable'>");

      // header
      sb.append("<tr>");
      sb.append("<th>");
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
      sb.append("<th>");
      sb.append("Total");
      sb.append("</th>");
      sb.append("</tr>");

      sb.append("<tr>");
      sb.append("<th>");
      sb.append("Words");
      sb.append("</th>");
      sb.append("<td>");
      sb.append(graph.getWordsApproved());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(graph.getWordsNeedReview());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(graph.getWordsUntranslated());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(graph.getWordsTotal());
      sb.append("</td>");
      sb.append("</tr>");

      sb.append("<tr>");
      sb.append("<th>");
      sb.append("Msg");
      sb.append("</th>");
      sb.append("<td>");
      sb.append(graph.getUnitApproved());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(graph.getUnitNeedReview());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(graph.getUnitUntranslated());
      sb.append("</td>");
      sb.append("<td>");
      sb.append(graph.getUnitTotal());
      sb.append("</td>");
      sb.append("</tr>");

      sb.append("</table>");

      return sb.toString();
   }

   public final void showRelativeTo(final Element target)
   {
      // Set the position of the popup right before it is shown.
      setPopupPositionAndShow(new PositionCallback()
      {
         public void setPosition(int offsetWidth, int offsetHeight)
         {
            position(target, offsetWidth, offsetHeight);
         }
      });
   }
   
   private void position(final Element relativeObject, int offsetWidth, int offsetHeight)
   {
      // Calculate left position for the popup. The computation for
      // the left position is bidi-sensitive.

      int textBoxOffsetWidth = relativeObject.getOffsetWidth();

      // Compute the difference between the popup's width and the
      // textbox's width
      int offsetWidthDiff = offsetWidth - textBoxOffsetWidth;

      int left;

      if (LocaleInfo.getCurrentLocale().isRTL())
      { // RTL case

         int textBoxAbsoluteLeft = relativeObject.getAbsoluteLeft();

         // Right-align the popup. Note that this computation is
         // valid in the case where offsetWidthDiff is negative.
         left = textBoxAbsoluteLeft - offsetWidthDiff;

         // If the suggestion popup is not as wide as the text box, always
         // align to the right edge of the text box. Otherwise, figure out
         // whether
         // to right-align or left-align the popup.
         if (offsetWidthDiff > 0)
         {

            // Make sure scrolling is taken into account, since
            // box.getAbsoluteLeft() takes scrolling into account.
            int windowRight = Window.getClientWidth() + Window.getScrollLeft();
            int windowLeft = Window.getScrollLeft();

            // Compute the left value for the right edge of the textbox
            int textBoxLeftValForRightEdge = textBoxAbsoluteLeft + textBoxOffsetWidth;

            // Distance from the right edge of the text box to the right edge
            // of the window
            int distanceToWindowRight = windowRight - textBoxLeftValForRightEdge;

            // Distance from the right edge of the text box to the left edge of
            // the
            // window
            int distanceFromWindowLeft = textBoxLeftValForRightEdge - windowLeft;

            // If there is not enough space for the overflow of the popup's
            // width to the right of the text box and there IS enough space for
            // the
            // overflow to the right of the text box, then left-align the popup.
            // However, if there is not enough space on either side, stick with
            // right-alignment.
            if (distanceFromWindowLeft < offsetWidth && distanceToWindowRight >= offsetWidthDiff)
            {
               // Align with the left edge of the text box.
               left = textBoxAbsoluteLeft;
            }
         }
      }
      else
      { // LTR case

         // Left-align the popup.
         left = relativeObject.getAbsoluteLeft();

         // If the suggestion popup is not as wide as the text box, always align
         // to
         // the left edge of the text box. Otherwise, figure out whether to
         // left-align or right-align the popup.
         if (offsetWidthDiff > 0)
         {
            // Make sure scrolling is taken into account, since
            // box.getAbsoluteLeft() takes scrolling into account.
            int windowRight = Window.getClientWidth() + Window.getScrollLeft();
            int windowLeft = Window.getScrollLeft();

            // Distance from the left edge of the text box to the right edge
            // of the window
            int distanceToWindowRight = windowRight - left;

            // Distance from the left edge of the text box to the left edge of
            // the
            // window
            int distanceFromWindowLeft = left - windowLeft;

            // If there is not enough space for the overflow of the popup's
            // width to the right of hte text box, and there IS enough space for
            // the
            // overflow to the left of the text box, then right-align the popup.
            // However, if there is not enough space on either side, then stick
            // with
            // left-alignment.
            if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff)
            {
               // Align with the right edge of the text box.
               left -= offsetWidthDiff;
            }
         }
      }

      // Calculate top position for the popup

      int top = relativeObject.getAbsoluteTop();

      // Make sure scrolling is taken into account, since
      // box.getAbsoluteTop() takes scrolling into account.
      int windowTop = Window.getScrollTop();
      int windowBottom = Window.getScrollTop() + Window.getClientHeight();

      // Distance from the top edge of the window to the top edge of the
      // text box
      int distanceFromWindowTop = top - windowTop;

      // Distance from the bottom edge of the window to the bottom edge of
      // the text box
      int distanceToWindowBottom = windowBottom - (top + relativeObject.getOffsetHeight());

      // If there is not enough space for the popup's height below the text
      // box and there IS enough space for the popup's height above the text
      // box, then then position the popup above the text box. However, if there
      // is not enough space on either side, then stick with displaying the
      // popup below the text box.
      if (distanceToWindowBottom < offsetHeight && distanceFromWindowTop >= offsetHeight)
      {
         top -= offsetHeight;
      }
      else
      {
         // Position above the text box
         top += relativeObject.getOffsetHeight();
      }
      setPopupPosition(left, top);
   }

}
