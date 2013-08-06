package org.zanata.webtrans.client.view;

import java.util.List;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import org.zanata.webtrans.shared.model.Locale;

/**
 * @author Hannes Eskebaek
 */
public interface TransUnitChangeSourceLangDisplay extends WidgetDisplay {

    void buildListBox(List<Locale> locales);

    void setListener(Listener listener);

    interface Listener {

        public void onShowButtonClick(Locale selectedLocale);

        public void onHideButtonClick();
    }
}
