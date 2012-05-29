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
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.AppPresenter;
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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
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
      String notificationInfo();
      String notificationWarning();
      String notificationError();

      String userName();

      String hasError();

      String image();
   }

   private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

   @UiField(provided = true)
   TransUnitCountBar translationStatsBar;

   @UiField
   Label notificationMessage, dismissLink;
   
   @UiField
   InlineLabel readOnlyLabel, documentsLink;

   @UiField
   SpanElement selectedDocumentSpan, selectedDocumentPathSpan;

   @UiField
   LayoutPanel container, topPanel;
   
   // NotificationPanel notificationPanel;

   @UiField(provided = true)
   final Resources resources;

   @UiField
   Styles style;

   @UiField
   MenuBar topMenuBar;

   @UiField
   PushButton errorNotificationBtn;

   @UiField
   Anchor searchAndReplace;

   MenuBar menuBar;

   MenuItem helpMenuItem;
   
   MenuItem leaveWorkspaceMenuItem;

   MenuItem signOutMenuItem;
   
   MenuItem layoutMenuItem;

   // TODO may be able to make these provided=true widgets
   private Widget documentListView;
   private Widget translationView;
   private Widget searchResultsView;

   private final WebTransMessages messages;
   
   private final String userAvatarUrl;

   @Inject
   public AppView(Resources resources, WebTransMessages messages, DocumentListPresenter.Display documentListView, SearchResultsPresenter.Display searchResultsView, TranslationPresenter.Display translationView, final Identity identity)
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

      searchAndReplace.setText(messages.searchAndReplace());
      userAvatarUrl = identity.getPerson().getAvatarUrl();

      this.documentListView = documentListView.asWidget();
      this.container.add(this.documentListView);

      this.translationView = translationView.asWidget();
      this.container.add(this.translationView);

      this.searchResultsView = searchResultsView.asWidget();
      this.container.add(this.searchResultsView);

      errorNotificationBtn.setTitle(messages.errorNotification());

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
         break;
      case Search:
         setWidgetVisible(documentListView, false);
         setWidgetVisible(searchResultsView, true);
         setWidgetVisible(translationView, false);
         break;
      case Editor:
         setWidgetVisible(documentListView, false);
         setWidgetVisible(searchResultsView, false);
         setWidgetVisible(translationView, true);
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
   public HasClickHandlers getDismiss()
   {
      return dismissLink;
   }

   @Override
   public HasVisibility getDismissVisibility()
   {
      return dismissLink;
   }

   @Override
   public HasClickHandlers getDocumentsLink()
   {
      return documentsLink;
   }

   @Override
   public void initMenuList(String userLabel, Command helpMenuCommand, Command leaveWorkspaceMenuCommand, Command signOutMenuCommand, Command layoutMenuCommand)
   {
      menuBar = new MenuBar(true);
      helpMenuItem = new MenuItem(messages.help(), helpMenuCommand);
      leaveWorkspaceMenuItem = new MenuItem(messages.leaveWorkspace(), leaveWorkspaceMenuCommand);
      signOutMenuItem = new MenuItem(messages.signOut(), signOutMenuCommand);

      menuBar.addItem(helpMenuItem);
      menuBar.addSeparator();

      ImageLabel layoutImageLabel = new ImageLabel(resources.viewChoose(), messages.viewSelection());
      layoutMenuItem = menuBar.addItem(layoutImageLabel.getElement().getInnerHTML(), true, layoutMenuCommand);

      menuBar.addSeparator();
      menuBar.addItem(leaveWorkspaceMenuItem);
      menuBar.addItem(signOutMenuItem);

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
   public void setNotificationMessage(String message, NotificationEvent.Severity severity)
   {
      notificationMessage.setText(message);
      notificationMessage.setTitle(message);

      // TODO use setStyleDependentName (notification-severity.name())
      switch (severity)
      {
      case Info:
         notificationMessage.setStyleName(style.notificationInfo());
         break;
      case Warning:
         notificationMessage.setStyleName(style.notificationWarning());
         break;
      case Error:
         notificationMessage.setStyleName(style.notificationError());
         break;
      }
      dismissLink.setVisible(!message.isEmpty());
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
   public HasClickHandlers getSearchAndReplaceLink()
   {
      return searchAndReplace;
   }

   @Override
   public HasClickHandlers getErrorNotificationBtn()
   {
      return errorNotificationBtn;
   }

   @Override
   public void setErrorNotificationText(int count)
   {
      errorNotificationBtn.setText(String.valueOf(count));
      errorNotificationBtn.getDownFace().setText(String.valueOf(count));
      errorNotificationBtn.getDownDisabledFace().setText(String.valueOf(count));
      errorNotificationBtn.getDownHoveringFace().setText(String.valueOf(count));
      errorNotificationBtn.getUpDisabledFace().setText(String.valueOf(count));
      errorNotificationBtn.getUpFace().setText(String.valueOf(count));
      errorNotificationBtn.getUpHoveringFace().setText(String.valueOf(count));

      if (count == 0)
      {
         errorNotificationBtn.removeStyleName(style.hasError());
      }
      else
      {
         errorNotificationBtn.addStyleName(style.hasError());
      }
   }

}
