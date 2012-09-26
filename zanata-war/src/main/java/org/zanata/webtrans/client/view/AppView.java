/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.view;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.TransUnitCountBar;
import org.zanata.webtrans.shared.auth.Identity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppDisplay
{

   interface AppViewUiBinder extends UiBinder<LayoutPanel, AppView>
   {
   }

   interface Styles extends CssResource
   {
      String disableTab();

      String selectedTab();
   }

   private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

   @UiField(provided = true)
   TransUnitCountBar translationStatsBar;

   @UiField
   InlineLabel projectLink, iterationFilesLink, readOnlyLabel, resize, keyShortcuts;
   
   @UiField
   SpanElement selectedDocumentSpan, selectedDocumentPathSpan;

   @UiField
   LayoutPanel sideMenuContainer, rootContainer, contentContainer;
   
   @UiField(provided = true)
   final Resources resources;

   @UiField
   TabLayoutPanel content;

   @UiField
   Styles style;
   
   @UiField
   Label editorTab, searchAndReplaceTab, documentListTab;

   private Listener listener;

   private final WebTransMessages messages;
   
   private final static String STYLE_MAXIMIZE = "icon-resize-full-3";
   private final static String STYLE_MINIMIZE = "icon-resize-small-2";

   @Inject
   public AppView(Resources resources, WebTransMessages messages, DocumentListPresenter.Display documentListView, SearchResultsPresenter.Display searchResultsView, TranslationPresenter.Display translationView, SideMenuDisplay sideMenuView, final Identity identity)
   {
      this.resources = resources;
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);

      // this must be initialized before uiBinder.createAndBindUi(), or an
      // exception will be thrown at runtime
      translationStatsBar = new TransUnitCountBar(messages);
      translationStatsBar.setVisible(false); // hide until there is a value to
                                             // display
      initWidget(uiBinder.createAndBindUi(this));

      readOnlyLabel.setText("[" + messages.readOnly() + "]");

      keyShortcuts.setTitle(messages.availableKeyShortcutsTitle());

      resize.setTitle(messages.maximize());
      resize.addStyleName(STYLE_MAXIMIZE);

      sideMenuContainer.add(sideMenuView.asWidget());

      searchAndReplaceTab.setTitle(messages.projectWideSearchAndReplace());
      documentListTab.setTitle(messages.documentListTitle());
      editorTab.setTitle(messages.editor());

      content.add(documentListView.asWidget());
      content.add(translationView.asWidget());
      content.add(searchResultsView.asWidget());
      
      Window.enableScrolling(false);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }


   // Order of the tab
   private final static int DOCUMENT_VIEW = 0;
   private final static int EDITOR_VIEW = 1;
   private final static int SEARCH_AND_REPLACE_VIEW = 2;
   
   @Override
   public void showInMainView(MainView view)
   {
      switch (view)
      {
      case Documents:
         content.selectTab(DOCUMENT_VIEW);
         setSelectedTab(documentListTab);
         break;
      case Search:
         content.selectTab(SEARCH_AND_REPLACE_VIEW);
         setSelectedTab(searchAndReplaceTab);
         break;
      case Editor:
         content.selectTab(EDITOR_VIEW);
         setSelectedTab(editorTab);
         break;
      }
   }
   
   private void setSelectedTab(Widget tab)
   {
      editorTab.removeStyleName(style.selectedTab());
      searchAndReplaceTab.removeStyleName(style.selectedTab());
      documentListTab.removeStyleName(style.selectedTab());
      
      tab.addStyleName(style.selectedTab());
   }

   @Override
   public void setProjectLinkLabel(String workspaceNameLabel)
   {
      projectLink.setText(workspaceNameLabel);
   }

   @Override
   public void setIterationFilesLabel(String name)
   {
      iterationFilesLink.setText(name);
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public void setDocumentLabel(String docPath, String docName)
   {
      selectedDocumentPathSpan.setInnerText(docPath);
      selectedDocumentSpan.setInnerText(docName);
   }

   @Override
   public void setStats(TranslationStats transStats, boolean statsByWords)
   {
      translationStatsBar.setStats(transStats, statsByWords);
      translationStatsBar.setVisible(true);
   }

   @Override
   public void setReadOnlyVisible(boolean visible)
   {
      readOnlyLabel.setVisible(visible);
   }

   /**
    * return false if to be maximise, true for minimise
    * 
    */
   @Override
   public boolean getAndToggleResizeButton()
   {
      if (resize.getStyleName().contains(STYLE_MAXIMIZE))
      {
         resize.removeStyleName(STYLE_MAXIMIZE);
         resize.addStyleName(STYLE_MINIMIZE);
         resize.setTitle(messages.minimize());
         return false;
      }
      else
      {
         resize.removeStyleName(STYLE_MINIMIZE);
         resize.addStyleName(STYLE_MAXIMIZE);
         resize.setTitle(messages.maximize());
         return true;
      }
   }

   @Override
   public void setResizeVisible(boolean visible)
   {
      resize.setVisible(visible);
   }

   private final static double MIN_MENU_WIDTH = 24.0;
   private final static double EXPENDED_MENU_RIGHT = 304.0;

   private final static double MINIMISED_EDITOR_RIGHT = 280.0;
   private final static int ANIMATE_DURATION = 300;

   @Override
   public void showSideMenu(boolean isShowing)
   {
      rootContainer.forceLayout();
      if (isShowing)
      {
         rootContainer.setWidgetLeftRight(contentContainer, 0.0, Unit.PX, MINIMISED_EDITOR_RIGHT, Unit.PX);
         rootContainer.setWidgetRightWidth(sideMenuContainer, 0.0, Unit.PX, EXPENDED_MENU_RIGHT, Unit.PX);
      }
      else
      {
         rootContainer.setWidgetLeftRight(contentContainer, 0.0, Unit.PX, 0.0, Unit.PX);
         rootContainer.setWidgetRightWidth(sideMenuContainer, 0.0, Unit.PX, MIN_MENU_WIDTH, Unit.PX);
      }
      rootContainer.animate(ANIMATE_DURATION);
   }

   @UiHandler("projectLink")
   public void onProjectLinkClick(ClickEvent event)
   {
      listener.onProjectLinkClicked();
   }

   @UiHandler("iterationFilesLink")
   public void onIterationFilesLinkClick(ClickEvent event)
   {
      listener.onIterationFilesLinkClicked();
   }

   @UiHandler("keyShortcuts")
   public void onKeyShortcutsIconClick(ClickEvent event)
   {
      listener.onKeyShortcutsClicked();
   }

   @UiHandler("resize")
   public void onResizeIconClick(ClickEvent event)
   {
      listener.onResizeClicked();
   }
   
   @Override
   public void enableTab(MainView view, boolean enable)
   {
      switch (view)
      {
      case Search:
         enableTab(searchAndReplaceTab, enable);
         break;
      case Documents:
         enableTab(documentListTab, enable);
         break;
      case Editor:
         enableTab(editorTab, enable);
         break;
      }
   }

   @UiHandler("documentListTab")
   public void onDocumentListTabClick(ClickEvent event)
   {
      listener.onDocumentListClicked();
   }

   @UiHandler("editorTab")
   public void onEditorTabClick(ClickEvent event)
   {
      listener.onEditorClicked();
   }
   
   @UiHandler("searchAndReplaceTab")
   public void onSearchAndReplaceTabTabClick(ClickEvent event)
   {
      listener.onSearchAndReplaceClicked();
   }

   private void enableTab(Widget tab, boolean enable)
   {
      if (enable)
      {
         tab.removeStyleName(style.disableTab());
      }
      else
      {
         tab.addStyleName(style.disableTab());
      }
   }

}
