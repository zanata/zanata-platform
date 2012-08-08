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

import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window.Location;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void setPageSize(int pageSize);

      HasValue<String> getFilterTextBox();

      HasValue<Boolean> getExactSearchCheckbox();

      HasValue<Boolean> getCaseSensitiveCheckbox();

      HasSelectionHandlers<DocumentInfo> getDocumentList();

      HasData<DocumentNode> getDocumentListTable();

      ListDataProvider<DocumentNode> getDataProvider();

      void renderTable(SingleSelectionModel<DocumentNode> selectionModel);
   }

   private static final int PAGE_SIZE = 20;

   private final DispatchAsync dispatcher;
   private final UserWorkspaceContext userworkspaceContext;
   private DocumentInfo currentDocument;
   private DocumentNode currentSelection;
   private final WebTransMessages messages;
   private final History history;
   private final Location windowLocation;

   private ListDataProvider<DocumentNode> dataProvider;
   private HashMap<DocumentId, DocumentNode> nodes;

   /**
    * For quick lookup of document id by full path (including document name).
    * Primarily for use with history token.
    */
   private HashMap<String, DocumentId> idsByPath;

   private final PathDocumentFilter filter = new PathDocumentFilter();

   // used to determine whether to re-run filter
   private HistoryToken currentHistoryState = null;

   private TranslationStats projectStats;

   private final SingleSelectionModel<DocumentNode> selectionModel = new SingleSelectionModel<DocumentNode>()
   {
      @Override
      public void setSelected(DocumentNode object, boolean selected)
      {
         if (selected && (super.getSelectedObject() != null))
         {
            if (object.getDocInfo().getId().equals(super.getSelectedObject().getDocInfo().getId()))
            {
               // switch to editor (via history) on re-selection
               HistoryToken token = history.getHistoryToken();
               token.setView(MainView.Editor);
               token.setDocumentPath(object.getDocInfo().getPath() + object.getDocInfo().getName());
               history.newItem(token);
               userworkspaceContext.setSelectedDoc(object.getDocInfo());
            }
         }
         super.setSelected(object, selected);
      }
   };

   @Inject
   public DocumentListPresenter(final Display display, EventBus eventBus, UserWorkspaceContext userworkspaceContext, CachingDispatchAsync dispatcher, final WebTransMessages messages, History history, Location windowLocation)
   {
      super(display, eventBus);
      this.userworkspaceContext = userworkspaceContext;
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.history = history;
      this.windowLocation = windowLocation;

      nodes = new HashMap<DocumentId, DocumentNode>();
   }

   @Override
   protected void onBind()
   {
      dataProvider = display.getDataProvider();
      display.renderTable(selectionModel);

      selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler()
      {
         public void onSelectionChange(SelectionChangeEvent event)
         {
            DocumentNode selectedNode = selectionModel.getSelectedObject();
            if (selectedNode != null)
            {
               SelectionEvent.fire(display.getDocumentList(), selectedNode.getDocInfo());
            }
         }
      });

      registerHandler(display.getDocumentList().addSelectionHandler(new SelectionHandler<DocumentInfo>()
      {
         @Override
         public void onSelection(SelectionEvent<DocumentInfo> event)
         {
            // generate history token
            HistoryToken token = history.getHistoryToken();

            // prevent feedback loops between history and selection
            boolean isNewSelection;
            DocumentId docId = getDocumentId(token.getDocumentPath());
            if (docId == null)
            {
               isNewSelection = true;
            }
            else
            {
               isNewSelection = !docId.equals(event.getSelectedItem().getId());
            }

            if (isNewSelection)
            {
               currentDocument = event.getSelectedItem();
               token.setDocumentPath(event.getSelectedItem().getPath() + event.getSelectedItem().getName());
               token.setView(MainView.Editor);
               // don't carry searches over to the next document
               token.setSearchText("");
               history.newItem(token);
            }
            userworkspaceContext.setSelectedDoc(event.getSelectedItem());
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
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (!event.getValue().equals(token.getDocFilterText()))
            {
               token.setDocFilterText(event.getValue());
               history.newItem(token.toTokenString());
            }
         }
      }));

      registerHandler(display.getExactSearchCheckbox().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (event.getValue() != token.getDocFilterExact())
            {
               token.setDocFilterExact(event.getValue());
               history.newItem(token.toTokenString());
            }
         }
      }));

      registerHandler(display.getCaseSensitiveCheckbox().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            HistoryToken token = history.getHistoryToken();
            if (event.getValue() != token.isDocFilterCaseSensitive())
            {
               token.setDocFilterCaseSensitive(event.getValue());
               history.newItem(token);
            }
         }
      }));

      history.addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            if (currentHistoryState == null)
               currentHistoryState = new HistoryToken(); // default values

            boolean filterChanged = false;
            HistoryToken token = HistoryToken.fromTokenString(event.getValue());
            // update textbox to match new history state
            if (!token.getDocFilterText().equals(display.getFilterTextBox().getValue()))
            {
               display.getFilterTextBox().setValue(token.getDocFilterText(), true);
            }

            if (!token.getDocFilterText().equals(currentHistoryState.getDocFilterText()))
            {
               // different pattern
               filter.setPattern(token.getDocFilterText());
               filterChanged = true;
            }

            // update exact-match checkbox to match new history state
            if (token.getDocFilterExact() != display.getExactSearchCheckbox().getValue())
            {
               display.getExactSearchCheckbox().setValue(token.getDocFilterExact());
            }

            if (token.getDocFilterExact() != currentHistoryState.getDocFilterExact())
            {
               filter.setFullText(token.getDocFilterExact());
               filterChanged = true;
            }

            // update case-sensitive checkbox to match new history state
            if (token.isDocFilterCaseSensitive() != display.getCaseSensitiveCheckbox().getValue())
            {
               display.getCaseSensitiveCheckbox().setValue(token.isDocFilterCaseSensitive());
            }
            if (token.isDocFilterCaseSensitive() != currentHistoryState.isDocFilterCaseSensitive())
            {
               filter.setCaseSensitive(token.isDocFilterCaseSensitive());
               filterChanged = true;
            }

            currentHistoryState = token;

            if (filterChanged)
               runFilter();
         }
      });

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            TransUnitUpdateInfo updateInfo = event.getUpdateInfo();
            // update stats for containing document
            DocumentInfo updatedDoc = getDocumentInfo(updateInfo.getDocumentId());
            adjustStats(updatedDoc.getStats(), updateInfo);
            eventBus.fireEvent(new DocumentStatsUpdatedEvent(updatedDoc.getId(), updatedDoc.getStats()));

            // refresh document list table
            dataProvider.refresh();

            // update project stats, forward to AppPresenter
            adjustStats(projectStats, updateInfo);
            eventBus.fireEvent(new ProjectStatsUpdatedEvent(projectStats));
         }

         /**
          * @param stats the stats object to update
          * @param updateInfo info describing the change in translations
          */
         private void adjustStats(TranslationStats stats, TransUnitUpdateInfo updateInfo)
         {
            TransUnitCount unitCount = stats.getUnitCount();
            TransUnitWords wordCount = stats.getWordCount();
            unitCount.decrement(updateInfo.getPreviousState());
            unitCount.increment(updateInfo.getTransUnit().getStatus());
            wordCount.decrement(updateInfo.getPreviousState(), updateInfo.getSourceWordCount());
            wordCount.increment(updateInfo.getTransUnit().getStatus(), updateInfo.getSourceWordCount());
         }
      }));

      loadDocumentList();
      display.setPageSize(PAGE_SIZE);

      history.fireCurrentHistoryState();
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
   final class PathDocumentFilter
   {
      private static final String DOCUMENT_FILTER_LIST_DELIMITER = ",";

      private HashSet<String> patterns = new HashSet<String>();
      private boolean isFullText = false;
      private boolean caseSensitive = false;

      public boolean accept(DocumentInfo value)
      {
         if (patterns.isEmpty())
            return true;
         String fullPath = value.getPath() + value.getName();
         if (!caseSensitive)
         {
            fullPath = fullPath.toLowerCase();
         }
         for (String pattern : patterns)
         {
            if (!caseSensitive)
            {
               pattern = pattern.toLowerCase();
            }
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

      public void setCaseSensitive(boolean caseSensitive)
      {
         this.caseSensitive = caseSensitive;
      }
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
      // Auto-generated method stub
   }

   private void loadDocumentList()
   {
      dispatcher.execute(new GetDocumentList(userworkspaceContext.getWorkspaceContext().getWorkspaceId().getProjectIterationId(), windowLocation.getQueryDocuments()), new AsyncCallback<GetDocumentListResult>()
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
            Log.info("Received doc list for " + result.getProjectIterationId() + ": " + documents.size() + " elements, loading time: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
            setList(documents);
            start = System.currentTimeMillis();

            history.fireCurrentHistoryState();

            projectStats = new TranslationStats(); // = 0
            for (DocumentInfo doc : documents)
            {
               projectStats.add(doc.getStats());
            }

            // re-use these stats for the project stats
            eventBus.fireEvent(new ProjectStatsUpdatedEvent(projectStats));
            Log.info("Time to calculate project stats: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
         }
      });
   }

   private void setList(ArrayList<DocumentInfo> sortedList)
   {
      dataProvider.getList().clear();
      nodes = new HashMap<DocumentId, DocumentNode>(sortedList.size());
      idsByPath = new HashMap<String, DocumentId>(sortedList.size());
      long start = System.currentTimeMillis();
      for (DocumentInfo doc : sortedList)
      {
         idsByPath.put(doc.getPath() + doc.getName(), doc.getId());
         DocumentNode node = new DocumentNode(messages, doc, eventBus);
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
      dataProvider.refresh();
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

   /**
    * 
    * @param docId the id of the document
    * @return document info corresponding to the id, or null if the document is
    *         not in the document list
    */
   public DocumentInfo getDocumentInfo(DocumentId docId)
   {
      DocumentNode node = nodes.get(docId);
      return (node == null ? null : node.getDocInfo());
   }

   /**
    * 
    * @param fullPathAndName document path + document name
    * @return the id for the document, or null if the document is not in the
    *         document list or there is no document list
    */
   public DocumentId getDocumentId(String fullPathAndName)
   {
      if (idsByPath != null)
      {
         return idsByPath.get(fullPathAndName);
      }
      return null;
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
