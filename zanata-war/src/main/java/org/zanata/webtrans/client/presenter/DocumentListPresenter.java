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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.HasStatsFilter;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesAction;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesResult;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgress;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgressResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListDisplay> implements HasStatsFilter, DocumentListDisplay.Listener, DocumentSelectionHandler, UserConfigChangeHandler, TransUnitUpdatedEventHandler
{
   private final UserWorkspaceContext userworkspaceContext;
   private DocumentInfo currentDocument;
   private DocumentNode currentSelection;
   private final WebTransMessages messages;
   private final History history;
   private final UserOptionsService userOptionsService;

   private ListDataProvider<DocumentNode> dataProvider;
   private HashMap<DocumentId, DocumentNode> nodes;
   
   private final CachingDispatchAsync dispatcher;

   /**
    * For quick lookup of document id by full path (including document name).
    * Primarily for use with history token.
    */
   private HashMap<String, DocumentId> idsByPath;

   private final PathDocumentFilter filter = new PathDocumentFilter();

   private TranslationStats projectStats;

   private final NoSelectionModel<DocumentNode> selectionModel = new NoSelectionModel<DocumentNode>();

   @Inject
   public DocumentListPresenter(DocumentListDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, UserWorkspaceContext userworkspaceContext, final WebTransMessages messages, History history, UserOptionsService userOptionsService)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.userworkspaceContext = userworkspaceContext;
      this.messages = messages;
      this.history = history;
      this.userOptionsService = userOptionsService;

      nodes = new HashMap<DocumentId, DocumentNode>();
   }


   @Override
   protected void onBind()
   {
      dataProvider = display.getDataProvider();
      display.setListener(this);

      display.renderTable(selectionModel);

      setStatsFilter(STATS_OPTION_WORDS);

      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), this));
      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), this));
      registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));

      display.updatePageSize(userOptionsService.getConfigHolder().getState().getDocumentListPageSize());
      display.setThemes(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());
   }
   
   @Override
   public void fireDocumentSelection(DocumentInfo doc)
   {
      // generate history token
      HistoryToken token = history.getHistoryToken();

      currentDocument = doc;
      token.setDocumentPath(doc.getPath() + doc.getName());
      token.setView(MainView.Editor);
      // don't carry searches over to the next document
      token.setSearchText("");
      history.newItem(token);

      userworkspaceContext.setSelectedDoc(doc);
   }

   @Override
   public void fireFilterToken(String value)
   {
      HistoryToken token = HistoryToken.fromTokenString(history.getToken());
      if (!value.equals(token.getDocFilterText()))
      {
         token.setDocFilterText(value);
         history.newItem(token.toTokenString());
      }
   }

   @Override
   public void fireExactSearchToken(boolean value)
   {
      HistoryToken token = HistoryToken.fromTokenString(history.getToken());
      if (value != token.getDocFilterExact())
      {
         token.setDocFilterExact(value);
         history.newItem(token.toTokenString());
      }
   }

   @Override
   public void fireCaseSensitiveToken(boolean value)
   {
      HistoryToken token = history.getHistoryToken();
      if (value != token.isDocFilterCaseSensitive())
      {
         token.setDocFilterCaseSensitive(value);
         history.newItem(token);
      }
   }

   @Override
   public void statsOptionChange(String option)
   {
      setStatsFilter(option);
      dataProvider.refresh();
   }

   @Override
   public void setStatsFilter(String option)
   {
      display.setStatsFilter(option);
   }

   public void updateFilterAndRun(String docFilterText, boolean docFilterExact, boolean docFilterCaseSensitive)
   {
      display.updateFilter(docFilterCaseSensitive, docFilterExact, docFilterText);

      filter.setCaseSensitive(docFilterCaseSensitive);
      filter.setFullText(docFilterExact);
      filter.setPattern(docFilterText);

      runFilter();
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

   public void setDocuments(ArrayList<DocumentInfo> sortedList)
   {
      dataProvider.getList().clear();
      nodes = new HashMap<DocumentId, DocumentNode>(sortedList.size());
      idsByPath = new HashMap<String, DocumentId>(sortedList.size());
      long start = System.currentTimeMillis();
      for (DocumentInfo doc : sortedList)
      {
         idsByPath.put(doc.getPath() + doc.getName(), doc.getId());
         DocumentNode node = new DocumentNode(messages, doc, eventBus);
         node.setVisible(filter.accept(doc));
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

   private void setSelection(final DocumentId documentId)
   {
      if (currentSelection != null && currentSelection.getDocInfo().getId() == documentId)
      {
         Log.info("same selection doc id:" + documentId);
         return;
      }
      currentSelection = null;
      DocumentNode node = nodes.get(documentId);
      if (node != null)
      {
         currentSelection = node;
         userworkspaceContext.setSelectedDoc(node.getDocInfo());
         // required in order to show the document selected in doclist when
         // loading from bookmarked history token
         fireDocumentSelection(node.getDocInfo());
      }
   }

   public void setProjectStats(TranslationStats projectStats)
   {
      this.projectStats = projectStats;
   }

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

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      display.setThemes(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());
      if (event.getView() == MainView.Documents)
      {
         display.updatePageSize(userOptionsService.getConfigHolder().getState().getDocumentListPageSize());
      }
   }

   @Override
   public void downloadAllFiles()
   {
      WorkspaceId workspaceId = userworkspaceContext.getWorkspaceContext().getWorkspaceId();
      dispatcher.execute(new DownloadAllFilesAction(workspaceId.getProjectIterationId().getProjectSlug(), workspaceId.getProjectIterationId().getIterationSlug(), workspaceId.getLocaleId().getId()), new AsyncCallback<DownloadAllFilesResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Unable generate all files to download"));
            display.hideConfirmation();
         }

         @Override
         public void onSuccess(DownloadAllFilesResult result)
         {
            if (result.isPrepared())
            {
               processId = result.getProcessId();
               display.updateFileDownloadProgress(0, 0);
               display.setDownloadInProgress(true);
               display.startGetDownloadStatus(1000);
            }
            else
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Permission denied for this action"));
               display.hideConfirmation();
            }
         }
      });
   }

   @Override
   public void cancelDownloadAllFiles()
   {
      display.hideConfirmation();
   }

   private String processId;

   @Override
   public void updateDownloadFileProgress()
   {
      dispatcher.execute(new GetDownloadAllFilesProgress(processId), new AsyncCallback<GetDownloadAllFilesProgressResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Unable get progress of file preparation"));
            display.hideConfirmation();
         }

         @Override
         public void onSuccess(GetDownloadAllFilesProgressResult result)
         {
            display.updateFileDownloadProgress(result.getCurrentProgress(), result.getMaxProgress());

            if (result.isDone())
            {
               display.stopGetDownloadStatus();
               final String url = Application.getAllFilesDownloadURL(result.getDownloadId());
               display.setAndShowFilesDownloadLink(url);
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, "File ready to download",  display.getDownloadAllFilesInlineLink(url)));
            }
         }
      });
   }

   @Override
   public void showUploadDialog(DocumentInfo docInfo)
   {
      display.showUploadDialog(docInfo, userworkspaceContext.getWorkspaceContext().getWorkspaceId());
   }

   @Override
   public void cancelFileUpload()
   {
      display.closeFileUpload();
   }

   @Override
   public void onFileUploadComplete(SubmitCompleteEvent event)
   {
      display.closeFileUpload();
      if(event.getResults().contains("200"))
      {
         if(event.getResults().contains("Warning"))
         {
            eventBus.fireEvent(new NotificationEvent(Severity.Warning, "File uploaded.", event.getResults(), true, null));
         }
         else
         {
            eventBus.fireEvent(new NotificationEvent(Severity.Info, "File uploaded.", event.getResults(), true, null));
         }
      }
      else
      {
         eventBus.fireEvent(new NotificationEvent(Severity.Error, "File upload failed.", event.getResults(), true, null));
      }
   }

   @Override
   public void onUploadFile()
   {
      if (!Strings.isNullOrEmpty(display.getSelectedUploadFileName()))
      {
         display.submitUploadForm();
      }
   }

}
