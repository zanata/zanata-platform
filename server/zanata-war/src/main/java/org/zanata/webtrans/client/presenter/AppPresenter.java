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
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEventHandler;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;

public class AppPresenter extends WidgetPresenter<AppPresenter.Display>
{
   // javac seems confused about which Display is which.
   // somehow, qualifying WidgetDisplay helps!
   public interface Display extends net.customware.gwt.presenter.client.widget.WidgetDisplay
   {

      void showInMainView(MainView editor);

      HasClickHandlers getSignOutLink();

      HasClickHandlers getLeaveWorkspaceLink();

      HasClickHandlers getHelpLink();

      HasClickHandlers getDocumentsLink();

      void setUserLabel(String userLabel);

      void setWorkspaceNameLabel(String workspaceNameLabel, String workspaceTitle);

      void setDocumentLabel(String docPath, String docName);

      void setNotificationMessage(String message, NotificationEvent.Severity severity);

      HasClickHandlers getDismiss();
      HasVisibility getDismissVisibility();

      void setStats(TranslationStats transStats);

      void setReadOnlyVisible(boolean visible);
   }

   private final DocumentListPresenter documentListPresenter;
   private final TranslationPresenter translationPresenter;
   private final SearchResultsPresenter searchResultsPresenter;
   private final History history;
   private final Identity identity;
   private final Window window;
   private final Window.Location windowLocation;
   private final WorkspaceContext workspaceContext;

   private final WebTransMessages messages;

   private DocumentInfo selectedDocument;

   private final TranslationStats selectedDocumentStats = new TranslationStats();
   private final TranslationStats projectStats = new TranslationStats();
   private TranslationStats currentDisplayStats = new TranslationStats();
   private MainView currentView = null;

   private static final String WORKSPACE_TITLE_QUERY_PARAMETER_KEY = "title";

   @Inject
   public AppPresenter(Display display, EventBus eventBus, final TranslationPresenter translationPresenter, final DocumentListPresenter documentListPresenter, final SearchResultsPresenter searchResultsPresenter, final Identity identity, final WorkspaceContext workspaceContext, final WebTransMessages messages, final History history, final Window window, final Window.Location windowLocation)
   {
      super(display, eventBus);
      this.history = history;
      this.identity = identity;
      this.messages = messages;
      this.documentListPresenter = documentListPresenter;
      this.translationPresenter = translationPresenter;
      this.searchResultsPresenter = searchResultsPresenter;
      this.window = window;
      this.windowLocation = windowLocation;
      this.workspaceContext = workspaceContext;
   }

   @Override
   protected void onBind()
   {
      documentListPresenter.bind();
      translationPresenter.bind();
      searchResultsPresenter.bind();

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {

         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            display.setReadOnlyVisible(event.isReadOnly());
         }
      }));

      registerHandler(eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler()
      {

         @Override
         public void onNotification(NotificationEvent event)
         {
            display.setNotificationMessage(event.getMessage(), event.getSeverity());
            display.getDismissVisibility().setVisible(true);
            Log.info("Notification:" + event.getMessage());
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

      registerHandler(display.getDismiss().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.setNotificationMessage("", NotificationEvent.Severity.Info);
            display.getDismissVisibility().setVisible(false);
         }
      }));

      display.getDismissVisibility().setVisible(false);

      registerHandler(display.getSignOutLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.redirectToLogout();
         }
      }));

      registerHandler(display.getLeaveWorkspaceLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            // use when opening workspace in new window
            // Application.closeWindow();

            // use when opening workspace in same window
            Application.redirectToZanataProjectHome(workspaceContext.getWorkspaceId());
         }
      }));

      registerHandler(display.getDocumentsLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());

            if (token.getView().equals(MainView.Documents))
            {
               if (selectedDocument == null)
                  return; // abort if no doc to edit
               token.setView(MainView.Editor);
            }
            else
            {
               token.setView(MainView.Documents);
            }
            history.newItem(token.toTokenString());
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

      display.setUserLabel(identity.getPerson().getName());
      String workspaceTitle = windowLocation.getParameter(WORKSPACE_TITLE_QUERY_PARAMETER_KEY);
      display.setWorkspaceNameLabel(workspaceContext.getWorkspaceName(), workspaceTitle);
      window.setTitle(messages.windowTitle(workspaceContext.getWorkspaceName(), workspaceContext.getLocaleName()));

      display.setReadOnlyVisible(workspaceContext.isReadOnly());

      showView(MainView.Documents);

      history.fireCurrentHistoryState();
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
      if (docId == null)
      {
         token.setView(MainView.Documents);
      }

      showView(token.getView());
   }

   private void showView(MainView viewToShow)
   {
      if (currentView != viewToShow)
      {
         switch (viewToShow)
         {
         case Editor:
            if (selectedDocument != null)
            {
               display.setDocumentLabel(selectedDocument.getPath(), selectedDocument.getName());
            }
            currentDisplayStats = selectedDocumentStats;
            break;
         //Documents or Search
         default:
            if (currentView == MainView.Editor)
               translationPresenter.saveEditorPendingChange();
            display.setDocumentLabel("", messages.noDocumentSelected());
            currentDisplayStats = projectStats;
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
