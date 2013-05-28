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

   void setTranslatedFilter(boolean filterByTranslated);

   void setNeedReviewFilter(boolean filterByNeedReview);

   void setUntranslatedFilter(boolean filterByUntranslated);

   void setHasErrorFilter(boolean filterByHasError);

   interface Listener 
   {
      void searchTerm(String searchTerm);

      void messageFilterOptionChanged(Boolean translatedChkValue, Boolean draftChkValue, Boolean untranslatedChkValue, Boolean hasErrorChkValue);
   }

   void setOptionsState(ConfigurationState state);

}
