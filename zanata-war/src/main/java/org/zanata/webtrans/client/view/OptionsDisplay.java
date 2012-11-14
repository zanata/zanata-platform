package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import com.google.gwt.user.client.ui.Widget;


/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public interface OptionsDisplay extends WidgetDisplay
{
   void setListener(Listener listener);

   void setOptions(Widget optionWidget);

   void setShowErrorChk(boolean showError);

   interface Listener extends CommonOptionsListener
   {
      void onShowErrorsOptionChanged(Boolean showErrorChkValue);
   }

   interface CommonOptionsListener
   {
      void persistOptionChange();

      void loadOptions();

      void loadDefaultOptions();

   }

}
