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

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.ButtonDisplayChangeEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEventHandler;
import org.zanata.webtrans.client.events.ProjectStatsRetrievedEvent;
import org.zanata.webtrans.client.events.ProjectStatsRetrievedEventHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.presenter.AppPresenter.Display.MainView;
import org.zanata.webtrans.client.presenter.AppPresenter.Display.StatsType;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppPresenter extends WidgetPresenter<AppPresenter.Display>
{
   // javac seems confused about which Display is which.
   // somehow, qualifying WidgetDisplay helps!
   public interface Display extends net.customware.gwt.presenter.client.widget.WidgetDisplay
   {
      enum MainView
      {
         Documents, Editor;
      }

      enum StatsType
      {
         Document, Project;
      }

      void setDocumentListView(Widget documentListView);

      void setTranslationView(Widget translationView);

      void showInMainView(MainView editor);

      MainView getCurrentView();

      HasClickHandlers getSignOutLink();

      HasClickHandlers getLeaveWorkspaceLink();

      HasClickHandlers getHelpLink();

      HasClickHandlers getDocumentsLink();

      HasClickHandlers getEditorButtonsCheckbox();
      
      void setUserLabel(String userLabel);

      void setWorkspaceNameLabel(String workspaceNameLabel);

      void setSelectedDocument(DocumentInfo document);

      void setNotificationMessage(String var);

      /**
       * Set the statistics to display for the current document or project
       * 
       * @param statsFor whether these are document or project stats
       * @param transStats the stats to display for the document/project
       */
      void setStats(StatsType statsFor, TranslationStats transStats);

      /**
       * Choose which statistics to show in the header bar. Does not change
       * visibility of the stats bar.
       * 
       * @param whichStats the type of stats to display
       */
      void showStats(StatsType whichStats);

      /**
       * Set whether the stats bar is visible.
       * 
       * @param visible
       */
      void setStatsVisible(boolean visible);
   }

   private final DocumentListPresenter documentListPresenter;
   private final TranslationPresenter translationPresenter;
   private final WorkspaceContext workspaceContext;
   private final Identity identity;

   private final WebTransMessages messages;

   private DocumentInfo selectedDocument;

   private final DispatchAsync dispatcher;

   private final TranslationStats selectedDocumentStats = new TranslationStats();
   private final TranslationStats projectStats = new TranslationStats();

   @Inject
   public AppPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, final TranslationPresenter translationPresenter, final DocumentListPresenter documentListPresenter, final Identity identity, final WorkspaceContext workspaceContext, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.identity = identity;
      this.messages = messages;
      this.documentListPresenter = documentListPresenter;
      this.translationPresenter = translationPresenter;
      this.workspaceContext = workspaceContext;
   }

   @Override
   protected void onBind()
   {

      registerHandler(eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler()
      {

         @Override
         public void onNotification(NotificationEvent event)
         {
            display.setNotificationMessage(event.getMessage());
         }
      }));

      Window.enableScrolling(false);

      documentListPresenter.bind();
      translationPresenter.bind();

      display.setDocumentListView(documentListPresenter.getDisplay().asWidget());
      display.setTranslationView(translationPresenter.getDisplay().asWidget());

      display.showInMainView(MainView.Documents);


      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            display.setSelectedDocument(event.getDocument());
            if (selectedDocument == null || !event.getDocument().getId().equals(selectedDocument.getId()))
            {
               selectedDocument = event.getDocument();
               requestDocumentStats(selectedDocument.getId());
            }
            display.setSelectedDocument(selectedDocument);
            display.showInMainView(MainView.Editor);
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            if (selectedDocument != null && event.getDocumentId().equals(selectedDocument.getId()))
            {
               adjustStats(selectedDocumentStats, event);
               display.setStats(StatsType.Document, selectedDocumentStats);
            }
            adjustStats(projectStats, event);
            display.setStats(StatsType.Project, projectStats);
         }
      }));

      registerHandler(eventBus.addHandler(ProjectStatsRetrievedEvent.getType(), new ProjectStatsRetrievedEventHandler()
      {

         @Override
         public void onProjectStatsRetrieved(ProjectStatsRetrievedEvent event)
         {
            ArrayList<DocumentStatus> liststatus = event.getProjectDocumentStats();
            // this shouldn't be required at the moment, but will prevent
            // confusing bugs if project stats are retrieved multiple times in
            // future
            projectStats.set(new TranslationStats());
            for (DocumentStatus doc : liststatus)
            {
               projectStats.add(doc.getCount());
            }
            display.setStats(StatsType.Project, projectStats);
         }
      }));

      registerHandler(display.getDocumentsLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (display.getCurrentView().equals(MainView.Documents))
            {
               if (selectedDocument != null)
               {
                  display.setSelectedDocument(selectedDocument);
                  display.showInMainView(MainView.Editor);
               }
            }
            else
            {
               translationPresenter.saveEditorPendingChange();
               display.showInMainView(MainView.Documents);
            }
         }
      }));

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
            Application.closeWindow();
            // As the editor was created at new window, it should be closed
            // rather than redirected to project home.
            // Application.redirectToZanataProjectHome(workspaceContext.getWorkspaceId());
         }
      }));
      
      registerHandler(display.getEditorButtonsCheckbox().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            boolean showButtons = ((CheckBox) display.getEditorButtonsCheckbox()).getValue();
            eventBus.fireEvent(new ButtonDisplayChangeEvent(showButtons));
         }
      }));
      
      

      display.setUserLabel(identity.getPerson().getName());

      display.setWorkspaceNameLabel(workspaceContext.getWorkspaceName());

      Window.setTitle(messages.windowTitle(workspaceContext.getWorkspaceName(), workspaceContext.getLocaleName()));
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   private void requestDocumentStats(final DocumentId newDocumentId)
   {
      dispatcher.execute(new GetStatusCount(newDocumentId), new AsyncCallback<GetStatusCountResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("error fetching GetStatusCount: " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetStatusCountResult result)
         {
            selectedDocumentStats.set(result.getCount());
            display.setStats(StatsType.Document, selectedDocumentStats);
         }
      });
   }

   /**
    * @param Updateevent
    */
   private void adjustStats(TranslationStats statsObject, TransUnitUpdatedEvent Updateevent)
   {
      TransUnitCount unitCount = statsObject.getUnitCount();
      TransUnitWords wordCount = statsObject.getWordCount();

      unitCount.increment(Updateevent.getTransUnit().getStatus());
      unitCount.decrement(Updateevent.getPreviousStatus());
      wordCount.increment(Updateevent.getTransUnit().getStatus(), Updateevent.getWordCount());
      wordCount.decrement(Updateevent.getPreviousStatus(), Updateevent.getWordCount());
   }
}
