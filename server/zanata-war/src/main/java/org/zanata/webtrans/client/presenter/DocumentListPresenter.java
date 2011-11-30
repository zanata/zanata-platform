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
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.editor.filter.ContentFilter;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.ProjectStatsRetrievedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.AppPresenter.Display.MainView;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> implements HasDocumentSelectionHandlers
{

   public interface Display extends WidgetDisplay
   {
      void setList(ArrayList<DocumentInfo> sortedList);

      void clearSelection();

      void setSelection(DocumentId documentId);

      void setFilter(ContentFilter<DocumentInfo> filter);

      void removeFilter();

      HasValue<String> getFilterTextBox();

      HasSelectionHandlers<DocumentInfo> getDocumentList();

      TransUnitUpdatedEventHandler getDocumentNode(DocumentId docId);

      DocumentInfo getDocumentInfo(DocumentId docId);
   }

   private final DispatchAsync dispatcher;
   private final WorkspaceContext workspaceContext;
   private DocumentInfo currentDocument;
   private final WebTransMessages messages;

   @Inject
   public DocumentListPresenter(Display display, EventBus eventBus, WorkspaceContext workspaceContext, CachingDispatchAsync dispatcher, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.workspaceContext = workspaceContext;
      this.dispatcher = dispatcher;
      this.messages = messages;
      Log.info("DocumentListPresenter()");
   }

   @Override
   protected void onBind()
   {

      registerHandler(display.getDocumentList().addSelectionHandler(new SelectionHandler<DocumentInfo>()
      {
         @Override
         public void onSelection(SelectionEvent<DocumentInfo> event)
         {
            // generate history token
            HistoryToken token = HistoryToken.fromTokenString(History.getToken());

            // prevent feedback loops between history and selection
            boolean isNewSelection;
            if (token.hasDocumentId())
            {
               try
               {
                  isNewSelection = event.getSelectedItem().getId().getId() != token.getDocumentId().getId();
               }
               catch (Throwable t)
               {
                  Log.info("got exception determining whether selection is new", t);
                  isNewSelection = false;
               }
            }
            else
            {
               isNewSelection = true;
            }

            if (isNewSelection)
            {
               currentDocument = event.getSelectedItem();
               token.setDocumentId(currentDocument.getId());
               token.setView(MainView.Editor);
               History.newItem(token.toTokenString());
            }
         }
      }));

      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            // match bookmarked selection, but prevent selection feedback loop
            // from history
            if (event.getDocumentId() != (currentDocument == null ? null : currentDocument.getId()))
            {
               display.setSelection(event.getDocumentId());
            }
         }
      }));

      registerHandler(display.getFilterTextBox().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            if (event.getValue().isEmpty())
            {
               display.removeFilter();
            }
            else
            {
               basicContentFilter.setPattern(event.getValue());
               display.setFilter(basicContentFilter);
            }
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            DocumentId docId = event.getDocumentId();
            TransUnitUpdatedEventHandler handler = display.getDocumentNode(docId);
            if (handler != null)
               handler.onTransUnitUpdated(event);
         }
      }));
      loadDocumentList();
   }

   final class BasicContentFilter implements ContentFilter<DocumentInfo>
   {
      private String pattern = "";

      @Override
      public boolean accept(DocumentInfo value)
      {
         return value.getName().contains(pattern);
      }

      public void setPattern(String pattern)
      {
         this.pattern = pattern;
      }
   }

   private final BasicContentFilter basicContentFilter = new BasicContentFilter();

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public HandlerRegistration addDocumentSelectionHandler(DocumentSelectionHandler handler)
   {
      return eventBus.addHandler(DocumentSelectionEvent.getType(), handler);
   }

   @Override
   public void fireEvent(GwtEvent<?> event)
   {
      eventBus.fireEvent(event);
   }

   private void loadDocumentList()
   {
      // switch doc list to the new project
      dispatcher.execute(new GetDocumentList(workspaceContext.getWorkspaceId().getProjectIterationId()), new AsyncCallback<GetDocumentListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.loadDocFailed()));
         }

         @Override
         public void onSuccess(GetDocumentListResult result)
         {
            long start = System.currentTimeMillis();
            final ArrayList<DocumentInfo> documents = result.getDocuments();
            Log.info("Received doc list for " + result.getProjectIterationId() + ": " + documents.size() + " elements");
            display.setList(documents);
            Log.info("Time to load docs into DocListView: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            History.fireCurrentHistoryState();

            TranslationStats projectStats = new TranslationStats(); // projStats
                                                                    // = 0
            for (DocumentInfo doc : documents)
            {
               projectStats.add(doc.getStats());
            }

            // re-use these stats for the project stats
            eventBus.fireEvent(new ProjectStatsRetrievedEvent(projectStats));
            Log.info("Time to calculate project stats: " + String.valueOf(System.currentTimeMillis() - start));
         }
      });
   }

}
