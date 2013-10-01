package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.shared.rpc.NavOption;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface TransUnitNavigationDisplay extends WidgetDisplay {
    void setNavModeTooltip(NavOption navOption);

    void setListener(Listener listener);

    interface Listener {

        void goToFirstEntry();

        void goToLastEntry();

        void goToPreviousState();

        void goToNextState();
    }
}
