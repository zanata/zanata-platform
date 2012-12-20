package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.shared.rpc.ThemesOption;

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

   void setDisplayTheme(ThemesOption displayTheme);

   interface Listener extends CommonOptionsListener
   {
      void onShowErrorsOptionChanged(Boolean showErrorChkValue);

      void onThemesChanged(String value);
   }

   interface CommonOptionsListener
   {
      void persistOptionChange();

      void loadOptions();

      void loadDefaultOptions();

   }


}
