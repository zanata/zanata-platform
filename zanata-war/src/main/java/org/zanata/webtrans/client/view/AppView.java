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
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DashboardPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.ImageLabel;
import org.zanata.webtrans.client.ui.TransUnitCountBar;
import org.zanata.webtrans.shared.auth.Identity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppPresenter.Display
{

   interface AppViewUiBinder extends UiBinder<LayoutPanel, AppView>
   {
   }

   interface Styles extends CssResource
   {
      String userName();

      String hasWarning();

      String image();

      String menuBar();

      String menuItem();
   }

   private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

   @UiField(provided = true)
   TransUnitCountBar translationStatsBar;

   @UiField
   InlineLabel readOnlyLabel, documentsLink;

   @UiField
   SpanElement selectedDocumentSpan, selectedDocumentPathSpan;

   @UiField
   LayoutPanel container, topPanel;

   @UiField(provided = true)
   final Resources resources;

   @UiField
   Styles style;

   @UiField
   MenuBar topMenuBar;

   @UiField
   PushButton keyShortcuts, searchAndReplace, notificationBtn;


   MenuItem helpMenuItem;
   
   MenuItem reportProblemMenuItem;

   MenuItem leaveWorkspaceMenuItem;

   MenuItem signOutMenuItem;
   
   MenuItem layoutMenuItem;
   
   MenuItemSeparator layoutMenuSeperator;

   // TODO may be able to make these provided=true widgets
   private Widget documentListView;
   private Widget translationView;
   private Widget searchResultsView;
   private Widget dashboardView;
   

   private final WebTransMessages messages;
   
   private final String userAvatarUrl;

   @Inject
   public AppView(Resources resources, WebTransMessages messages, DocumentListPresenter.Display documentListView, SearchResultsPresenter.Display searchResultsView, TranslationPresenter.Display translationView, DashboardPresenter.Display dashboardView, final Identity identity)
   {
      this.resources = resources;
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);

      // this must be initialized before uiBinder.createAndBindUi(), or an
      // exception will be thrown at runtime
      translationStatsBar = new TransUnitCountBar(messages, true);
      translationStatsBar.setVisible(false); // hide until there is a value to
                                             // display

      initWidget(uiBinder.createAndBindUi(this));

//      searchAndReplace.setText(messages.searchAndReplace());
      userAvatarUrl = identity.getPerson().getAvatarUrl();

      keyShortcuts.setTitle(messages.availableKeyShortcutsTitle());
      searchAndReplace.setTitle(messages.projectWideSearchAndReplace());

      this.documentListView = documentListView.asWidget();
      this.container.add(this.documentListView);

      this.translationView = translationView.asWidget();
      this.container.add(this.translationView);

      this.searchResultsView = searchResultsView.asWidget();
      this.container.add(this.searchResultsView);
      
      this.dashboardView = dashboardView.asWidget();
      this.container.add(this.dashboardView);

      notificationBtn.setTitle(messages.notification());

      Window.enableScrolling(false);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void showInMainView(MainView view)
   {
      switch (view)
      {
      case Documents:
         setWidgetVisible(documentListView, true);
         setWidgetVisible(searchResultsView, false);
         setWidgetVisible(translationView, false);
         setWidgetVisible(dashboardView, false);
         break;
      case Search:
         setWidgetVisible(documentListView, false);
         setWidgetVisible(searchResultsView, true);
         setWidgetVisible(translationView, false);
         setWidgetVisible(dashboardView, false);
         break;
      case Dashboard:
         setWidgetVisible(documentListView, false);
         setWidgetVisible(searchResultsView, false);
         setWidgetVisible(translationView, false);
         setWidgetVisible(dashboardView, true);
         break;
      case Editor:
         setWidgetVisible(documentListView, false);
         setWidgetVisible(searchResultsView, false);
         setWidgetVisible(translationView, true);
         setWidgetVisible(dashboardView, false);
         break;
      }
   }

   private void setWidgetVisible(Widget widget, boolean visible)
   {
      if (visible)
      {
         container.setWidgetTopBottom(widget, 0, Unit.PX, 0, Unit.PX);
      }
      else
      {
         container.setWidgetTopHeight(widget, 0, Unit.PX, 0, Unit.PX);
      }
   }

   @Override
   public HasClickHandlers getDocumentsLink()
   {
      return documentsLink;
   }

   @Override
   public void initMenuList(String userLabel, Command helpMenuCommand, Command reportProblemMenuCommand, Command leaveWorkspaceMenuCommand, Command signOutMenuCommand, Command layoutMenuCommand)
   {
      MenuBar menuBar = new MenuBar(true);
      menuBar.addStyleName(style.menuBar());

      ImageLabel helpImageLabel = new ImageLabel(resources.help(), messages.help());
      helpImageLabel.setImageStyle(style.image());

      ImageLabel reportProblemImageLabel = new ImageLabel(resources.bug(), messages.reportAProblem());
      reportProblemImageLabel.setImageStyle(style.image());

      ImageLabel layoutImageLabel = new ImageLabel(resources.viewChoose(), messages.layoutSelection());
      layoutImageLabel.setImageStyle(style.image());

      ImageLabel signOutImageLabel = new ImageLabel(resources.logout(), messages.signOut());
      signOutImageLabel.setImageStyle(style.image());

      ImageLabel leaveWorkspaceImageLabel = new ImageLabel("", messages.leaveWorkspace());
      leaveWorkspaceImageLabel.setImageStyle(style.image());

      helpMenuItem = menuBar.addItem(helpImageLabel.getElement().getString(), true, helpMenuCommand);
      helpMenuItem.addStyleName(style.menuItem());
      menuBar.addSeparator();

      reportProblemMenuItem = menuBar.addItem(reportProblemImageLabel.getElement().getString(), true, reportProblemMenuCommand);
      reportProblemMenuItem.addStyleName(style.menuItem());
      menuBar.addSeparator();

      layoutMenuItem = menuBar.addItem(layoutImageLabel.getElement().getString(), true, layoutMenuCommand);
      layoutMenuItem.addStyleName(style.menuItem());
      layoutMenuSeperator = menuBar.addSeparator();

      leaveWorkspaceMenuItem = menuBar.addItem(leaveWorkspaceImageLabel.getElement().getString(), true, leaveWorkspaceMenuCommand);
      leaveWorkspaceMenuItem.addStyleName(style.menuItem());

      signOutMenuItem = menuBar.addItem(signOutImageLabel.getElement().getString(), true, signOutMenuCommand);
      signOutMenuItem.addStyleName(style.menuItem());

      ImageLabel userMenu = new ImageLabel(userAvatarUrl, userLabel + " " + messages.downArrow());
      userMenu.setLabelStyle(style.userName());
      userMenu.setImageStyle(style.image());

      topMenuBar.addItem(userMenu.getElement().getString(), true, menuBar);
   }

   @Override
   public void setWorkspaceNameLabel(String workspaceNameLabel, String workspaceTitle)
   {
      if (workspaceTitle == null || workspaceTitle.length() == 0)
         documentsLink.setText(workspaceNameLabel);
      else
         documentsLink.setText(workspaceNameLabel + " - " + workspaceTitle);
   }

   @Override
   public void setDocumentLabel(String docPath, String docName)
   {
      selectedDocumentPathSpan.setInnerText(docPath);
      selectedDocumentSpan.setInnerText(docName);
   }

   @Override
   public void setStats(TranslationStats transStats)
   {
      translationStatsBar.setStats(transStats);
      translationStatsBar.setVisible(true);
   }

   @Override
   public void setReadOnlyVisible(boolean visible)
   {
      readOnlyLabel.setVisible(visible);
   }

   @Override
   public HasClickHandlers getKeyShortcutButton()
   {
      return keyShortcuts;
   }

   @Override
   public HasClickHandlers getSearchAndReplaceButton()
   {
      return searchAndReplace;
   }

   @Override
   public HasClickHandlers getNotificationBtn()
   {
      return notificationBtn;
   }

   @Override
   public void setLayoutMenuVisible(boolean visible)
   {
      layoutMenuItem.setVisible(visible);
      layoutMenuSeperator.setVisible(visible);
   }

   @Override
   public void setNotificationText(int count, Severity severity)

   {
      notificationBtn.setText(String.valueOf(count));
      notificationBtn.getDownFace().setText(String.valueOf(count));
      notificationBtn.getDownDisabledFace().setText(String.valueOf(count));
      notificationBtn.getDownHoveringFace().setText(String.valueOf(count));
      notificationBtn.getUpDisabledFace().setText(String.valueOf(count));
      notificationBtn.getUpFace().setText(String.valueOf(count));
      notificationBtn.getUpHoveringFace().setText(String.valueOf(count));
   }

   @Override
   public void showNotificationAlert()
   {
      notificationBtn.addStyleName(style.hasWarning());
      
   }

   @Override
   public void cancelNotificationAlert()
   {
      notificationBtn.removeStyleName(style.hasWarning());
   }
}
