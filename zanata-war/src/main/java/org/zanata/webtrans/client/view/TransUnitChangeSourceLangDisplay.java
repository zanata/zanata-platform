package org.zanata.webtrans.client.view;

import java.util.List;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import org.zanata.webtrans.shared.model.Locale;

/**
 * @author Hannes Eskebaek
 */
public interface TransUnitChangeSourceLangDisplay extends WidgetDisplay
{
   void buildListBox(List<Locale> locales);

   void setSelectedLocale(Locale locale);

   void setListener(Listener listener);

   public void showReferenceList();

   public void hideReferenceList();

   interface Listener
   {
      public void onShowReference(Locale selectedLocale);

      public void onHideReference();

     void onSourceLangListBoxOptionChanged(String selectedLocale);
   }
}
