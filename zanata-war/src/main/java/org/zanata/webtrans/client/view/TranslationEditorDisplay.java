package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.ui.HasPager;

import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author aeng
 *
 */
public interface TranslationEditorDisplay extends WidgetDisplay
{
   void setEditorView(IsWidget widget);

   void setTransUnitNavigation(IsWidget widget);

   void setFilterView(IsWidget filterView);

   HasPager getPageNavigation();

   boolean isPagerFocused();
   
   void setListener(Listener listener);

   boolean getAndToggleResizeButton();

   void setReviewMode(boolean isReviewMode);

   void setReviewActionView(IsWidget widget);

   interface Listener
   {
      void refreshCurrentPage();

      void onResizeClicked();

      void onPagerFocused();

      void onPagerBlurred();
   }

   HasVisibility getResizeButton();

}
