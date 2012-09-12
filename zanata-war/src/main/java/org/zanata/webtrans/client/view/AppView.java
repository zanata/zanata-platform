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
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.SideMenuPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
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
   }

   private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

   @UiField(provided = true)
   TransUnitCountBar translationStatsBar;

   @UiField
   InlineLabel projectLink, iterationFilesLink, resize, keyShortcuts;
   
   @UiField
   InlineLabel readOnlyLabel, searchAndReplace, documentList;

   @UiField
   SpanElement selectedDocumentSpan, selectedDocumentPathSpan;

   @UiField
   LayoutPanel editorContainer, sideMenuContainer, rootContainer;
   
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
   public AppView(Resources resources, WebTransMessages messages, DocumentListPresenter.Display documentListView, SearchResultsPresenter.Display searchResultsView, TranslationPresenter.Display translationView, SideMenuPresenter.Display sideMenuView, final Identity identity)
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

      readOnlyLabel.setText("[" + messages.readOnly() + "]");

      keyShortcuts.setTitle(messages.availableKeyShortcutsTitle());
      searchAndReplace.setTitle(messages.projectWideSearchAndReplace());
      documentList.setTitle(messages.documentListTitle());

      resize.setTitle(messages.maximize());
      resize.addStyleName(STYLE_MAXIMIZE);

      this.documentListView = documentListView.asWidget();
      this.editorContainer.add(this.documentListView);

      this.translationView = translationView.asWidget();
      this.editorContainer.add(this.translationView);

      this.searchResultsView = searchResultsView.asWidget();
      this.editorContainer.add(this.searchResultsView);
      
      sideMenuContainer.add(sideMenuView.asWidget());
      
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
      return projectLink;
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
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, MINIMISED_EDITOR_RIGHT, Unit.PX);
         rootContainer.setWidgetRightWidth(sideMenuContainer, 0.0, Unit.PX, EXPENDED_MENU_RIGHT, Unit.PX);
      }
      else
      {
         rootContainer.setWidgetLeftRight(editorContainer, 0.0, Unit.PX, 0.0, Unit.PX);
         rootContainer.setWidgetRightWidth(sideMenuContainer, 0.0, Unit.PX, MIN_MENU_WIDTH, Unit.PX);
      }
      rootContainer.animate(ANIMATE_DURATION);
   }

   @Override
   public HasClickHandlers getProjectLink()
   {
      return projectLink;
   }

   @Override
   public HasClickHandlers getIterationFilesLink()
   {
      return iterationFilesLink;
   }
}
