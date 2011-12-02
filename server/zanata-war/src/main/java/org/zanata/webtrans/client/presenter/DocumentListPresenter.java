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
import java.util.HashMap;
import java.util.HashSet;

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
import org.zanata.webtrans.client.ui.DocumentNode;
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
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> implements HasDocumentSelectionHandlers
{

   public interface Display extends WidgetDisplay
   {
      void setPageSize(int pageSize);

      HasValue<String> getFilterTextBox();

      HasValue<Boolean> getExactSearchCheckbox();

      HasSelectionHandlers<DocumentInfo> getDocumentList();

      HasData<DocumentNode> getDocumentListTable();

      ListDataProvider<DocumentNode> getDataProvider();
   }

   private final DispatchAsync dispatcher;
   private final WorkspaceContext workspaceContext;
   private DocumentInfo currentDocument;
   private DocumentNode currentSelection;
   private final WebTransMessages messages;

   private ListDataProvider<DocumentNode> dataProvider;
   private HashMap<DocumentId, DocumentNode> nodes;

   // private ContentFilter<DocumentInfo> filter;
   private final PathDocumentFilter filter = new PathDocumentFilter();

   @Inject
   public DocumentListPresenter(Display display, EventBus eventBus, WorkspaceContext workspaceContext, CachingDispatchAsync dispatcher, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.workspaceContext = workspaceContext;
      this.dispatcher = dispatcher;
      this.messages = messages;

      dataProvider = display.getDataProvider();
      nodes = new HashMap<DocumentId, DocumentNode>();

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
               setSelection(event.getDocumentId());
            }
         }
      }));

      registerHandler(display.getFilterTextBox().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            filter.setPattern(event.getValue());
            runFilter();
         }
      }));

      registerHandler(display.getExactSearchCheckbox().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            filter.setFullText(event.getValue());
            runFilter();
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            DocumentId docId = event.getDocumentId();
            TransUnitUpdatedEventHandler handler = nodes.get(docId);
            if (handler != null)
               handler.onTransUnitUpdated(event);
         }
      }));

      loadDocumentList();
   }

   /**
    * Filters documents by their full path + name, with substring and exact
    * modes.
    * 
    * If there is no pattern set, this filter will accept all documents.
    * 
    * @author David Mason, damason@redhat.com
    * 
    */
   final class PathDocumentFilter implements ContentFilter<DocumentInfo>
   {
      private static final String DOCUMENT_FILTER_LIST_DELIMITER = ",";

      private HashSet<String> patterns = new HashSet<String>();
      private boolean isFullText = false;

      @Override
      public boolean accept(DocumentInfo value)
      {
         if (patterns.isEmpty())
            return true;
         String fullPath = value.getPath() + value.getName();
         for (String pattern : patterns)
         {
            if (isFullText)
            {
               if (fullPath.equals(pattern))
                  return true;
            }
            else if (fullPath.contains(pattern))
            {
               return true;
            }
         }
         return false; // didn't match any patterns
      }

      public void setPattern(String pattern)
      {
         patterns.clear();
         String[] patternCandidates = pattern.split(DOCUMENT_FILTER_LIST_DELIMITER);
         for (String candidate : patternCandidates)
         {
            // TODO check whether trimming is appropriate
            candidate = candidate.trim();
            if (candidate.length() != 0)
            {
               patterns.add(candidate);
            }
         }
      }

      public void setFullText(boolean fullText)
      {
         isFullText = fullText;
      }
   }

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
            setList(documents);
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

   private void setList(ArrayList<DocumentInfo> sortedList)
   {
      dataProvider.getList().clear();
      nodes = new HashMap<DocumentId, DocumentNode>(sortedList.size());
      int counter = 0;
      long start = System.currentTimeMillis();
      for (DocumentInfo doc : sortedList)
      {
         Log.info("Loading document: " + ++counter + " ");
         DocumentNode node = new DocumentNode(messages, doc, eventBus, dataProvider);
         if (filter != null)
         {
            node.setVisible(filter.accept(doc));
         }
         if (node.isVisible())
         {
            dataProvider.getList().add(node);
         }
         nodes.put(doc.getId(), node);
      }
      Log.info("Time to create DocumentNodes: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
      display.setPageSize(dataProvider.getList().size());
      dataProvider.addDataDisplay(display.getDocumentListTable());
   }

   /**
    * Filter the document list based on the current filter patterns. Empty
    * filter patterns will show all documents.
    */
   private void runFilter()
   {
      dataProvider.getList().clear();
      for (DocumentNode docNode : nodes.values())
      {
         docNode.setVisible(filter.accept(docNode.getDocInfo()));
         if (docNode.isVisible())
         {
            dataProvider.getList().add(docNode);
         }
      }
      dataProvider.refresh();
   }

   public DocumentInfo getDocumentInfo(DocumentId docId)
   {
      DocumentNode node = nodes.get(docId);
      return (node == null ? null : node.getDocInfo());
   }

   private void clearSelection()
   {
      if (currentSelection == null)
      {
         return;
      }
      currentSelection = null;
   }

   private void setSelection(final DocumentId documentId)
   {
      if (currentSelection != null && currentSelection.getDocInfo().getId() == documentId)
      {
         return;
      }
      clearSelection();
      DocumentNode node = nodes.get(documentId);
      if (node != null)
      {
         currentSelection = node;
         // required in order to show the document selected in doclist when
         // loading from bookmarked history token
         display.getDocumentListTable().getSelectionModel().setSelected(node, true);
      }
   }
}
