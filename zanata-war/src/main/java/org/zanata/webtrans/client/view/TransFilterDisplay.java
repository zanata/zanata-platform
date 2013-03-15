package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.presenter.UserConfigHolder.ConfigurationState;
import org.zanata.webtrans.client.ui.SearchFieldListener;

import com.google.gwt.user.client.ui.HasValue;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public interface TransFilterDisplay extends WidgetDisplay, SearchFieldListener
{
   boolean isFocused();

   void setListener(Listener listener);

   void setSearchTerm(String searchTerm);

   interface Listener 
   {
      void searchTerm(String searchTerm);
   }

   HasValue<Boolean> getTranslatedChk();

   HasValue<Boolean> getNeedReviewChk();

   HasValue<Boolean> getUntranslatedChk();

   void setOptionsState(ConfigurationState state);

   HasValue<Boolean> getHasErrorChk();
}
