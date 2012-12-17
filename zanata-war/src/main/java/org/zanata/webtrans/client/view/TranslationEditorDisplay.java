package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.ui.HasPager;

import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author aeng
 *
 */
public interface TranslationEditorDisplay extends WidgetDisplay
{
   void setEditorView(Widget widget);

   void setTransUnitNavigation(Widget widget);

   void setFilterView(Widget filterView);

   HasPager getPageNavigation();

   boolean isPagerFocused();
   
   void setListener(Listener listener);

   boolean getAndToggleResizeButton();

   interface Listener
   {
      void refreshCurrentPage();

      void onResizeClicked();

      void onPagerFocused();

      void onPagerBlurred();
   }

   HasVisibility getResizeButton();

}
