package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public interface TransFilterDisplay extends WidgetDisplay
{

   boolean isFocused();

   void setListener(Listener listener);

   void setSearchTerm(String searchTerm);

   interface Listener
   {
      void searchTerm(String searchTerm);
   }
}
