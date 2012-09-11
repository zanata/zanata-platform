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
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.PresenterRevealedEvent;
import net.customware.gwt.presenter.client.PresenterRevealedHandler;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.ShowSideMenuEvent;
import org.zanata.webtrans.client.events.ShowSideMenuEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;

public class AppPresenter extends WidgetPresenter<AppPresenter.Display>
{
   // javac seems confused about which Display is which.
   // somehow, qualifying WidgetDisplay helps!
   public interface Display extends net.customware.gwt.presenter.client.widget.WidgetDisplay
   {
      void showInMainView(MainView editor);

      HasClickHandlers getProjectLink();

      HasClickHandlers getIterationFilesLink();

      HasClickHandlers getDocumentsLink();

      void setProjectLinkLabel(String workspaceNameLabel);

      void setDocumentLabel(String docPath, String docName);

      void setStats(TranslationStats transStats);

      void setReadOnlyVisible(boolean visible);

      HasClickHandlers getKeyShortcutButton();

      HasClickHandlers getSearchAndReplaceButton();

      HasClickHandlers getDocumentListButton();

      HasClickHandlers getResizeButton();

      boolean getAndToggleResizeButton();

      void setResizeVisible(boolean visible);

      void showSideMenu(boolean isShowing);

      void setIterationFilesLabel(String iterationSlug);
   }

   private final KeyShortcutPresenter keyShortcutPresenter;
   private final DocumentListPresenter documentListPresenter;
   private final TranslationPresenter translationPresenter;
   private final SearchResultsPresenter searchResultsPresenter;
   private final SideMenuPresenter sideMenuPresenter;

   private final History history;
   private final Window window;
   private final Window.Location windowLocation;
   private final UserWorkspaceContext userWorkspaceContext;
   private final WebTransMessages messages;

   private DocumentInfo selectedDocument;

   private final TranslationStats selectedDocumentStats = new TranslationStats();
   private final TranslationStats projectStats = new TranslationStats();
   private TranslationStats currentDisplayStats = new TranslationStats();
   private MainView currentView = null;

   private static final String WORKSPACE_TITLE_QUERY_PARAMETER_KEY = "title";

   @Inject
   public AppPresenter(Display display, EventBus eventBus, final SideMenuPresenter sideMenuPresenter, final KeyShortcutPresenter keyShortcutPresenter, final TranslationPresenter translationPresenter, final DocumentListPresenter documentListPresenter, final SearchResultsPresenter searchResultsPresenter, final UserWorkspaceContext userWorkspaceContext, final WebTransMessages messages, final History history, final Window window, final Window.Location windowLocation)
   {
      super(display, eventBus);
      this.userWorkspaceContext = userWorkspaceContext;
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.history = history;
      this.messages = messages;
      this.documentListPresenter = documentListPresenter;
      this.translationPresenter = translationPresenter;
      this.searchResultsPresenter = searchResultsPresenter;
      this.sideMenuPresenter = sideMenuPresenter;
      this.window = window;
      this.windowLocation = windowLocation;
   }

   @Override
   protected void onBind()
   {
      keyShortcutPresenter.bind();
      documentListPresenter.bind();
      translationPresenter.bind();
      searchResultsPresenter.bind();
      sideMenuPresenter.bind();

      registerHandler(eventBus.addHandler(ShowSideMenuEvent.getType(), new ShowSideMenuEventHandler()
      {
         @Override
         public void onShowSideMenu(ShowSideMenuEvent event)
         {
            display.showSideMenu(event.isShowing());
         }
      }));
      
      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            userWorkspaceContext.setProjectActive(event.isProjectActive());
            if (userWorkspaceContext.hasReadOnlyAccess())
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyReadOnlyWorkspace()));
            }
            else
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyEditableWorkspace()));
            }
            display.setReadOnlyVisible(userWorkspaceContext.hasReadOnlyAccess());
         }
      }));

      registerHandler(eventBus.addHandler(DocumentStatsUpdatedEvent.getType(), new DocumentStatsUpdatedEventHandler()
      {
         @Override
         public void onDocumentStatsUpdated(DocumentStatsUpdatedEvent event)
         {
            if (selectedDocument != null && event.getDocId().equals(selectedDocument.getId()))
            {
               selectedDocumentStats.set(event.getNewStats());
               if (currentView.equals(MainView.Editor))
               {
                  refreshStatsDisplay();
               }
            }
         }
      }));

      registerHandler(eventBus.addHandler(ProjectStatsUpdatedEvent.getType(), new ProjectStatsUpdatedEventHandler()
      {
         @Override
         public void onProjectStatsRetrieved(ProjectStatsUpdatedEvent event)
         {
            projectStats.set(event.getProjectStats());
            if (currentView.equals(MainView.Documents))
            {
               refreshStatsDisplay();
            }
         }
      }));

      registerHandler(display.getDocumentsLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.redirectToIterationFiles(userWorkspaceContext.getWorkspaceContext().getWorkspaceId());
         }
      }));

      registerHandler(display.getProjectLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.redirectToZanataProjectHome(userWorkspaceContext.getWorkspaceContext().getWorkspaceId());
         }
      }));

      registerHandler(display.getIterationFilesLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.redirectToIterationFiles(userWorkspaceContext.getWorkspaceContext().getWorkspaceId());
         }
      }));

      registerHandler(history.addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            processHistoryEvent(event);
         }
      }));

      display.getSearchAndReplaceButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (!token.getView().equals(MainView.Search))
            {
               token.setView(MainView.Search);
               history.newItem(token.toTokenString());
            }
         }
      });

      display.getDocumentListButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            gotoDocumentListView();
         }
      });

      display.getKeyShortcutButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            keyShortcutPresenter.showShortcuts();
         }
      });

      display.getResizeButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            boolean expended = display.getAndToggleResizeButton();
            translationPresenter.setSouthPanelExpanded(expended);
         }
      });

      registerHandler(eventBus.addHandler(PresenterRevealedEvent.getType(), new PresenterRevealedHandler()
      {

         @Override
         public void onPresenterRevealed(PresenterRevealedEvent event)
         {
            // TODO disabled until tests are updated
            // if (event.getPresenter() == searchResultsPresenter)
            // {
            // display.setDocumentLabel("",
            // messages.projectWideSearchAndReplace());
            // currentDisplayStats = projectStats;
            // }
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'L'), ShortcutContext.Application, messages.showDocumentListKeyShortcut(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            HistoryToken token = history.getHistoryToken();
            token.setView(MainView.Documents);
            history.newItem(token.toTokenString());
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'O'), ShortcutContext.Application, messages.showEditorKeyShortcut(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (selectedDocument == null)
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Warning, messages.noDocumentSelected()));
            }
            else
            {
               HistoryToken token = history.getHistoryToken();
               token.setView(MainView.Editor);
               history.newItem(token.toTokenString());
            }
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'P'), ShortcutContext.Application, messages.showProjectWideSearch(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            HistoryToken token = history.getHistoryToken();
            token.setView(MainView.Search);
            history.newItem(token.toTokenString());
         }
      }));

      display.setProjectLinkLabel(userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getProjectSlug());
      display.setIterationFilesLabel(userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getIterationSlug() + "(" + userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId().getId() + ")");

      String workspaceTitle = windowLocation.getParameter(WORKSPACE_TITLE_QUERY_PARAMETER_KEY);
      if (!Strings.isNullOrEmpty(workspaceTitle))
      {
         window.setTitle(messages.windowTitle2(userWorkspaceContext.getWorkspaceContext().getWorkspaceName(), userWorkspaceContext.getWorkspaceContext().getLocaleName(), workspaceTitle));
      }
      else
      {
         window.setTitle(messages.windowTitle(userWorkspaceContext.getWorkspaceContext().getWorkspaceName(), userWorkspaceContext.getWorkspaceContext().getLocaleName()));
      }

      display.setReadOnlyVisible(userWorkspaceContext.hasReadOnlyAccess());

      // this may be redundant with the following history event line
      showView(MainView.Documents);

      history.fireCurrentHistoryState();
   }

   private void gotoDocumentListView()
   {
      HistoryToken token = HistoryToken.fromTokenString(history.getToken());

      if (token.getView().equals(MainView.Documents))
      {
         if (selectedDocument == null)
         {
            return; // abort if no doc to edit
         }
         token.setView(MainView.Editor);
      }
      else
      {
         token.setView(MainView.Documents);
      }
      history.newItem(token.toTokenString());
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   private void processHistoryEvent(ValueChangeEvent<String> event)
   {
      Log.info("Responding to history token: " + event.getValue());

      HistoryToken token = HistoryToken.fromTokenString(event.getValue());

      DocumentId docId = documentListPresenter.getDocumentId(token.getDocumentPath());

      if (docId != null && (selectedDocument == null || !selectedDocument.getId().equals(docId)))
      {
         selectDocument(docId);
         eventBus.fireEvent(new DocumentSelectionEvent(docId));
      }

      // if there is no valid document, don't show the editor
      // default to document list instead
      if (docId == null && token.getView() == MainView.Editor)
      {
         token.setView(MainView.Documents);
      }

      showView(token.getView());
   }

   private void showView(MainView viewToShow)
   {
      if (currentView != viewToShow)
      {
         if (currentView == MainView.Editor)
         {
            translationPresenter.saveEditorPendingChange();
         }
         switch (viewToShow)
         {
         // TODO use revealDisplay/concealDisplay for editor and document views
         case Editor:
            if (selectedDocument != null)
            {
               display.setDocumentLabel(selectedDocument.getPath(), selectedDocument.getName());
            }
            currentDisplayStats = selectedDocumentStats;
            translationPresenter.revealDisplay();
            searchResultsPresenter.concealDisplay();
            sideMenuPresenter.showEditorMenu(true);
            display.setResizeVisible(true);
            break;
         case Search:
            // these two lines temporarily here until PresenterRevealedHandler
            // is
            // fully functional
            display.setDocumentLabel("", messages.projectWideSearchAndReplace());
            currentDisplayStats = projectStats;
            translationPresenter.concealDisplay();
            searchResultsPresenter.revealDisplay();
            sideMenuPresenter.showEditorMenu(false);
            display.setResizeVisible(false);
            break;
         case Documents:
         default:
            // TODO may want to continue showing selected document name
            // if a document is selected.
            display.setDocumentLabel("", messages.noDocumentSelected());
            currentDisplayStats = projectStats;
            translationPresenter.concealDisplay();
            searchResultsPresenter.concealDisplay();
            sideMenuPresenter.showEditorMenu(false);
            display.setResizeVisible(false);
            break;
         }
         display.showInMainView(viewToShow);
         currentView = viewToShow;
         refreshStatsDisplay();
      }
   }

   /**
    * Set selected document to the given document, update name and stats to
    * match the newly selected document.
    * 
    * @param docId id of the document to select
    */
   private void selectDocument(DocumentId docId)
   {

      if (selectedDocument == null || !docId.equals(selectedDocument.getId()))
      {
         DocumentInfo docInfo = documentListPresenter.getDocumentInfo(docId);
         if (docInfo != null)
         {
            selectedDocument = docInfo;
            selectedDocumentStats.set(selectedDocument.getStats());
            if (currentView == MainView.Editor)
            {
               display.setDocumentLabel(selectedDocument.getPath(), selectedDocument.getName());
               refreshStatsDisplay();
            }
         }
      }
   }

   /**
    * Ensure current stats are displayed in the display.
    */
   private void refreshStatsDisplay()
   {
      display.setStats(currentDisplayStats);
   }
}
