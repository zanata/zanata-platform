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
import org.zanata.webtrans.client.presenter.EditorOptionsPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppPresenter.Display
{

   interface AppViewUiBinder extends UiBinder<LayoutPanel, AppView>
   {
   }

   interface Styles extends CssResource
   {
      String hasWarning();
   }

   private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

   @UiField(provided = true)
   TransUnitCountBar translationStatsBar;

   @UiField
   InlineLabel readOnlyLabel, documentsLink, resize, notification, keyShortcuts, userChat;
   
   @UiField
   InlineLabel notificationLabel;

   @UiField
   InlineLabel searchAndReplace, documentList;

   @UiField
   SpanElement selectedDocumentSpan, selectedDocumentPathSpan;

   @UiField
   LayoutPanel editorContainer, editorOptionsContainer, validationOptionsContainer, chatContainer, rootContainer;
   
   @UiField(provided = true)
   final Resources resources;

   @UiField
   Styles style;

   // TODO may be able to make these provided=true widgets
   private Widget documentListView;
   private Widget translationView;
   private Widget searchResultsView;

   private final WebTransMessages messages;
   
   private final static String STYLE_MAXIMIZE = "icon-resize-full-3";
   private final static String STYLE_MINIMIZE = "icon-resize-small-2";

   @Inject
   public AppView(Resources resources, WebTransMessages messages, DocumentListPresenter.Display documentListView, SearchResultsPresenter.Display searchResultsView, TranslationPresenter.Display translationView, EditorOptionsPresenter.Display editorOptionsView, ValidationOptionsPresenter.Display validationOptionsView, WorkspaceUsersPresenter.Display workspaceUsersView, final Identity identity)
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

      keyShortcuts.setTitle(messages.availableKeyShortcutsTitle());
      searchAndReplace.setTitle(messages.projectWideSearchAndReplace());
      documentList.setTitle(messages.documentListTitle());
      notification.setTitle(messages.notification());

      userChat.setTitle(messages.chatRoom());
      resize.setTitle(messages.maximize());
      resize.addStyleName(STYLE_MAXIMIZE);

      this.documentListView = documentListView.asWidget();
      this.editorContainer.add(this.documentListView);

      this.translationView = translationView.asWidget();
      this.editorContainer.add(this.translationView);

      this.searchResultsView = searchResultsView.asWidget();
      this.editorContainer.add(this.searchResultsView);
      
      editorOptionsContainer.add(editorOptionsView.asWidget());
      validationOptionsContainer.add(validationOptionsView.asWidget());
      chatContainer.add(workspaceUsersView.asWidget());
      
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
         editorContainer.setWidgetTopBottom(widget, 0, Unit.PX, 0, Unit.PX);
      }
      else
      {
         editorContainer.setWidgetTopHeight(widget, 0, Unit.PX, 0, Unit.PX);
      }
   }

   @Override
   public HasClickHandlers getDocumentsLink()
   {
      return documentsLink;
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
   public HasClickHandlers getDocumentListButton()
   {
      return documentList;
   }

   @Override
   public HasClickHandlers getNotificationBtn()
   {
      return notification;
   }

   @Override
   public HasClickHandlers getChatRoomButton()
   {
      return userChat;
   }

   @Override
   public HasClickHandlers getResizeButton()
   {
      return resize;
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
   public void setNotificationText(int count, Severity severity)
   {
      notificationLabel.setText(String.valueOf(count));
   }

   @Override
   public void showNotificationAlert()
   {
      notification.addStyleName(style.hasWarning());
   }

   @Override
   public void cancelNotificationAlert()
   {
      notification.removeStyleName(style.hasWarning());
   }

   @Override
   public void setEditorOptionsVisible(boolean visible)
   {
      editorOptionsContainer.setVisible(visible);
   }

   @Override
   public void setValidationOptionsVisible(boolean visible)
   {
      validationOptionsContainer.setVisible(visible);
   }


   @Override
   public void setResizeVisible(boolean visible)
   {
      resize.setVisible(visible);
   }

   private final static double MENU_TOP = 35.0;
   private final static double MIN_VALIDATION_OPTION_HEIGHT = 50.0;
   private final static double MIN_CHAT_HEIGHT = 76.0;
   private final static double MIN_EDITOR_OPTION_HEIGHT = 24.0;

   private final static double MIN_MENU_WIDTH = 24.0;
   private final static double MAX_MENU_BOTTOM = 10.0;
   private final static double EXPENDED_MENU_RIGHT = 304.0;

   private final static double MINIMISED_EDITOR_RIGHT = 280.0;
   private final static int ANIMATE_DURATION = 300;

   @Override
   public void onEditorOptionsExpend(boolean expend)
   {
      rootContainer.forceLayout();
      if (expend)
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, MINIMISED_EDITOR_RIGHT, Unit.PX);
         rootContainer.setWidgetRightWidth(editorOptionsContainer, 0.0, Unit.PX, EXPENDED_MENU_RIGHT, Unit.PX);
         rootContainer.setWidgetTopBottom(editorOptionsContainer, MENU_TOP, Unit.PX, MAX_MENU_BOTTOM, Unit.PX);
      }
      else
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, 0.0, Unit.PX);
         rootContainer.setWidgetRightWidth(editorOptionsContainer, 0.0, Unit.PX, MIN_MENU_WIDTH, Unit.PX);
         rootContainer.setWidgetTopHeight(editorOptionsContainer, MENU_TOP, Unit.PX, MIN_EDITOR_OPTION_HEIGHT, Unit.PX);
      }
      rootContainer.animate(ANIMATE_DURATION);
   }


   @Override
   public void onValidationOptionsExpend(boolean expend)
   {
      rootContainer.forceLayout();
      if (expend)
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, MINIMISED_EDITOR_RIGHT, Unit.PX);
         rootContainer.setWidgetRightWidth(validationOptionsContainer, 0.0, Unit.PX, EXPENDED_MENU_RIGHT, Unit.PX);
         rootContainer.setWidgetTopBottom(validationOptionsContainer, MENU_TOP, Unit.PX, MAX_MENU_BOTTOM, Unit.PX);
      }
      else
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, 0.0, Unit.PX);
         rootContainer.setWidgetRightWidth(validationOptionsContainer, 0.0, Unit.PX, MIN_MENU_WIDTH, Unit.PX);
         rootContainer.setWidgetTopHeight(validationOptionsContainer, MENU_TOP, Unit.PX, MIN_VALIDATION_OPTION_HEIGHT, Unit.PX);
      }
      rootContainer.animate(ANIMATE_DURATION);
   }
   
   @Override
   public void onChatContainerExpend(boolean expend)
   {
      rootContainer.forceLayout();
      if (expend)
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, MINIMISED_EDITOR_RIGHT, Unit.PX);
         rootContainer.setWidgetRightWidth(chatContainer, 0.0, Unit.PX, EXPENDED_MENU_RIGHT, Unit.PX);
         rootContainer.setWidgetTopBottom(chatContainer, MENU_TOP, Unit.PX, MAX_MENU_BOTTOM, Unit.PX);
      }
      else
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, 0.0, Unit.PX);
         rootContainer.setWidgetRightWidth(chatContainer, 0.0, Unit.PX, MIN_MENU_WIDTH, Unit.PX);
         rootContainer.setWidgetTopHeight(chatContainer, MENU_TOP, Unit.PX, MIN_CHAT_HEIGHT, Unit.PX);
      }
      rootContainer.animate(ANIMATE_DURATION);
   }

   @Override
   public void setChatVisible(boolean visible)
   {
      chatContainer.setVisible(visible);
   }
}
