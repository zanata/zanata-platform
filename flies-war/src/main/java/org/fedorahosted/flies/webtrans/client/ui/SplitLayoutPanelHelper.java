package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A trick to allow resizing SplitLayoutPanel automagically
 * 
 * http://code.google.com/p/google-web-toolkit/issues/detail?id=4489
 * 
 */
public abstract class SplitLayoutPanelHelper extends SplitLayoutPanel
{

   public static void setSplitPosition(SplitLayoutPanel splitPanel, Widget widgetBeforeTheSplitter, double size)
   {
      LayoutData layout = (LayoutData) widgetBeforeTheSplitter.getLayoutData();
      layout.oldSize = layout.size;
      layout.size = size;
   }

   public static Widget getAssociatedSplitter(SplitLayoutPanel splitPanel, Widget child)
   {
      // If a widget has a next sibling, it must be a splitter, because the only
      // widget that *isn't* followed by a splitter must be the CENTER, which
      // has
      // no associated splitter.
      int idx = splitPanel.getWidgetIndex(child);
      if (idx < splitPanel.getWidgetCount() - 2)
      {
         Widget splitter = splitPanel.getWidget(idx + 1);
         return splitter;
      }
      return null;
   }

}
