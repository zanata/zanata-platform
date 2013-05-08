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

   void setDocumentLabel(String docPath, String docName);

   void setStats(TranslationStats transStats, boolean statsByWords);

   void setReadOnlyVisible(boolean visible);

   void showSideMenu(boolean isShowing);
   
   void setProjectLinkLabel(String text);

   void setFilesLinkLabel(String text);
   
   void setVersionLinkLabel(String text);

   void setListener(Listener listener);
   
   interface Listener
   {
      void onSearchAndReplaceClicked();

      void onDocumentListClicked();

      void onKeyShortcutsClicked();

      void onEditorClicked();

      void onReviewCLicked();
   }

   void enableTab(MainView view, boolean enable);

   void setKeyboardShorcutColor(boolean aliasKeyListening);
}
