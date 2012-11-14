package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.presenter.UserConfigHolder.ConfigurationState;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface DocumentListOptionsDisplay extends WidgetDisplay
{
   interface Listener
   {
      void onPageSizeClick(int pageSize);
   }

   void setListener(Listener listener);

   void setOptionsState(ConfigurationState state);
}
