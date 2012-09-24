package org.zanata.webtrans.client.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.presenter.MainView;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public interface AppDisplay extends WidgetDisplay
{
   void showInMainView(MainView view);

   void setProjectLinkLabel(String workspaceNameLabel);

   void setDocumentLabel(String docPath, String docName);

   void setStats(TranslationStats transStats);

   void setReadOnlyVisible(boolean visible);

   boolean getAndToggleResizeButton();

   void setResizeVisible(boolean visible);

   void showSideMenu(boolean isShowing);

   void setIterationFilesLabel(String iterationSlug);

   void setListener(Listener listener);

   interface Listener
   {
      void onProjectLinkClicked();

      void onIterationFilesLinkClicked();

      void onSearchAndReplaceClicked();

      void onDocumentListClicked();

      void onKeyShortcutsClicked();

      void onResizeClicked();

      void onEditorClicked();
   }

   void enableTab(MainView view, boolean enable);
}
