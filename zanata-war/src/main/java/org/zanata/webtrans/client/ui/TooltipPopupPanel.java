/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class TooltipPopupPanel extends PopupPanel
{
   private final Grid table;

   public TooltipPopupPanel(boolean autoHide)
   {
      super(autoHide);
      this.setStyleName("transUnitCountTooltip");

      table = new Grid(3, 5);
      table.setStyleName("transUnitCountTooltipTable");
      setTopAndSideHeader();
      setStyle();

      this.setWidget(table);
   }

   public void refreshData(TransUnitCountBar stats)
   {
      table.setText(1, 1, String.valueOf(stats.getWordsApproved()));
      table.setText(1, 2, String.valueOf(stats.getWordsNeedReview()));
      table.setText(1, 3, String.valueOf(stats.getWordsUntranslated()));
      table.setText(1, 4, String.valueOf(stats.getWordsTotal()));

      table.setText(2, 1, String.valueOf(stats.getUnitApproved()));
      table.setText(2, 2, String.valueOf(stats.getUnitNeedReview()));
      table.setText(2, 3, String.valueOf(stats.getUnitUntranslated()));
      table.setText(2, 4, String.valueOf(stats.getUnitTotal()));
   }

   private void setTopAndSideHeader()
   {
      table.setText(0, 0, "");
      table.setText(0, 1, "Translated");
      table.setText(0, 2, "Need Review");
      table.setText(0, 3, "Untranslated");
      table.setText(0, 4, "Total");

      table.setText(1, 0, "Words");
      table.setText(2, 0, "Msg");
   }

   private void setStyle()
   {
      table.getCellFormatter().setStyleName(0, 1, "approved-header");
      table.getCellFormatter().setStyleName(0, 2, "needReview-header");
      table.getCellFormatter().setStyleName(0, 3, "untranslated-header");
      table.getCellFormatter().setStyleName(0, 4, "topHeader");

      table.getCellFormatter().setStyleName(0, 0, "sideHeader");
      table.getCellFormatter().setStyleName(1, 0, "sideHeader");
      table.getCellFormatter().setStyleName(2, 0, "sideHeader");

      table.getCellFormatter().setStyleName(1, 1, "approved");
      table.getCellFormatter().setStyleName(2, 1, "approved");

      table.getCellFormatter().setStyleName(1, 2, "needReview");
      table.getCellFormatter().setStyleName(2, 2, "needReview");

      table.getCellFormatter().setStyleName(1, 3, "untranslated");
      table.getCellFormatter().setStyleName(2, 3, "untranslated");
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
