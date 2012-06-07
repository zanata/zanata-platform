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
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HasCommand;
import org.zanata.webtrans.client.ui.MenuCommandItem;
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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
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

      String hasError();

      String hasWarning();

      String image();
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
   
   // NotificationPanel notificationPanel;

   @UiField(provided = true)
   final Resources resources;

   @UiField
   Styles style;

   @UiField
   MenuBar topMenuBar;

   @UiField
   PushButton notificationBtn;

   @UiField
   Anchor searchAndReplace;

   Image userImg;

   MenuBar menuBar;

   MenuCommandItem helpMenuItem;

   MenuCommandItem leaveWorkspaceMenuItem;

   MenuCommandItem signOutMenuItem;

   // TODO may be able to make these provided=true widgets
   private Widget documentListView;
   private Widget translationView;
   private Widget searchResultsView;

   private final WebTransMessages messages;

   private Command emptyCommand = new Command()
   {
      public void execute()
      {
      }
   };

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

      menuBar = new MenuBar(true);
      helpMenuItem = new MenuCommandItem(messages.help(), emptyCommand);
      leaveWorkspaceMenuItem = new MenuCommandItem(messages.leaveWorkspace(), emptyCommand);
      signOutMenuItem = new MenuCommandItem(messages.signOut(), emptyCommand);

      searchAndReplace.setText(messages.searchAndReplace());

      menuBar.addItem(helpMenuItem);
      menuBar.addSeparator();
      menuBar.addItem(leaveWorkspaceMenuItem);
      menuBar.addItem(signOutMenuItem);
      userImg = new Image(identity.getPerson().getAvatarUrl());
      userImg.setStyleName(style.image());

      this.documentListView = documentListView.asWidget();
      this.container.add(this.documentListView);

      this.translationView = translationView.asWidget();
      this.container.add(this.translationView);

      this.searchResultsView = searchResultsView.asWidget();
      this.container.add(this.searchResultsView);

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
   public HasClickHandlers getDocumentsLink()
   {
      return documentsLink;
   }

   @Override
   public void setUserLabel(String userLabel)
   {
      HorizontalPanel userImageAndLabel = new HorizontalPanel();
      userImageAndLabel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      userImageAndLabel.add(userImg);
      
      Label userNameLabel = new Label(userLabel);
      userNameLabel.setStyleName(style.userName());
      userImageAndLabel.add(userNameLabel);
      Label downArrowLabel = new Label(messages.downArrow());
      userImageAndLabel.add(downArrowLabel);
      
      userImageAndLabel.setCellHorizontalAlignment(downArrowLabel, HasHorizontalAlignment.ALIGN_RIGHT);

      topMenuBar.addItem(userImageAndLabel.getElement().getString(), true, menuBar);
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
   public HasCommand getHelpMenuItem()
   {
      return helpMenuItem;
   }

   @Override
   public HasCommand getLeaveWorkspaceMenuItem()
   {
      return leaveWorkspaceMenuItem;
   }

   @Override
   public HasCommand getSignOutMenuItem()
   {
      return signOutMenuItem;
   }

   @Override
   public HasClickHandlers getSearchAndReplaceLink()
   {
      return searchAndReplace;
   }

   @Override
   public HasClickHandlers getNotificationBtn()
   {
      return notificationBtn;
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

      if (severity == Severity.Error)
      {
         notificationBtn.removeStyleName(style.hasWarning());
         notificationBtn.addStyleName(style.hasError());
      }
      else if (severity == Severity.Warning)
      {
         notificationBtn.addStyleName(style.hasWarning());
         notificationBtn.removeStyleName(style.hasError());
      }
      else
      {
         notificationBtn.removeStyleName(style.hasError());
         notificationBtn.removeStyleName(style.hasWarning());
      }
   }

}
