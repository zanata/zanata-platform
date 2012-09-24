package org.zanata.webtrans.client.view;

import org.zanata.webtrans.shared.rpc.NavOption;
import com.google.gwt.event.dom.client.HasClickHandlers;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public interface TransUnitNavigationDisplay extends WidgetDisplay
{
   void setNavModeTooltip(NavOption navOption);

   void setListener(Listener listener);

   interface Listener {

      void goToFirstEntry();

      void goToLastEntry();

      void goToPreviousState();

      void goToNextState();

      void goToPreviousEntry();

      void goToNextEntry();
   }
}
