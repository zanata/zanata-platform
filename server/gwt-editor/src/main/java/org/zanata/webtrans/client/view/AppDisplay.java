package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.ui.UnorderedListWidget;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface AppDisplay extends WidgetDisplay {
    void showInMainView(MainView view);

    void setDocumentLabel(String docPath, String docName);

    void setStats(ContainerTranslationStatistics transStats,
            boolean statsByWords);

    void setReadOnlyVisible(boolean visible);

    void setObsoleteVisible(boolean showObsolete);

    void showSideMenu(boolean isShowing);

    void setProjectLinkLabel(String text);

    void setFilesLinkLabel(String text);

    void setVersionLinkLabel(String text);

    void setListener(Listener listener);

    void showNotification(NotificationEvent event);

    interface Listener {
        void onSearchAndReplaceClicked();

        void onDocumentListClicked();

        void onKeyShortcutsClicked();

        void onEditorClicked();
    }

    void enableTab(MainView view, boolean enable);

    void setKeyboardShorcutColor(boolean aliasKeyListening);
}
