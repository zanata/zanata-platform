package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.ui.HasPager;

import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public interface TranslationEditorDisplay extends WidgetDisplay
{
   void setEditorView(IsWidget widget);

   void setTransUnitNavigation(IsWidget widget);

   void setFilterView(IsWidget filterView);
   
   void setSourceLangView(IsWidget filterView);

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

      void onPagerValueChanged(Integer pageNumber);
   }

   HasVisibility getResizeButton();

}
