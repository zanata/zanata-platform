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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.ProjectType;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.DocValidationResultEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageChangeEventHandler;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.RunDocValidationEvent;
import org.zanata.webtrans.client.events.RunDocValidationEventHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
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
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesAction;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesResult;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgress;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgressResult;
import org.zanata.webtrans.shared.rpc.RunDocValidationAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListDisplay> implements PageChangeEventHandler, HasStatsFilter, DocumentListDisplay.Listener, DocumentSelectionHandler, UserConfigChangeHandler, TransUnitUpdatedEventHandler, WorkspaceContextUpdateEventHandler, RunDocValidationEventHandler
{
   private final UserWorkspaceContext userWorkspaceContext;
   private DocumentInfo currentDocument;
   private DocumentNode currentSelection;
   private final WebTransMessages messages;
   private final History history;
   private final UserOptionsService userOptionsService;

   private ListDataProvider<DocumentNode> dataProvider;
   private HashMap<DocumentId, DocumentNode> nodes;
   private HashMap<DocumentId, Integer> pageRows;

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
      this.userWorkspaceContext = userworkspaceContext;
      this.messages = messages;
      this.history = history;
      this.userOptionsService = userOptionsService;

      nodes = new HashMap<DocumentId, DocumentNode>();
      pageRows = new HashMap<DocumentId, Integer>();
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
      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this));
      registerHandler(eventBus.addHandler(RunDocValidationEvent.getType(), this));

      registerHandler(display.getPageNavigation().addValueChangeHandler(new ValueChangeHandler<Integer>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Integer> event)
         {
            gotoPage(event.getValue());
         }
      }));

      display.updatePageSize(userOptionsService.getConfigHolder().getState().getDocumentListPageSize());
      display.setLayout(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());

      setupDownloadZipButton(getProjectType());
   }

   private ProjectType getProjectType()
   {
      return userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getProjectIterationId().getProjectType();
   }

   public void setupDownloadZipButton(ProjectType projectType)
   {
      if (isZipFileDownloadAllowed(projectType))
      {
         display.setEnableDownloadZip(true);
         if (isPoProject(projectType))
         {
            display.setDownloadZipButtonText(messages.downloadAllAsZip());
            display.setDownloadZipButtonTitle(messages.downloadAllAsZipDescription());
         }
         else
         {
            display.setDownloadZipButtonText(messages.downloadAllAsOfflinePoZip());
            display.setDownloadZipButtonTitle(messages.downloadAllAsOfflinePoZipDescription());
         }
      }
      else
      {
         display.setEnableDownloadZip(false);
         display.setDownloadZipButtonText(messages.downloadAllAsZip());
         display.setDownloadZipButtonTitle(messages.projectTypeNotSet());
      }
   }

   private boolean isPoProject(ProjectType projectType)
   {
      return projectType == ProjectType.Gettext || projectType == ProjectType.Podir;
   }

   protected boolean isZipFileDownloadAllowed(ProjectType projectType)
   {
      return projectType != null;
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

      userWorkspaceContext.setSelectedDoc(doc);
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

      for (DocumentId docId : pageRows.keySet())
      {
         display.setStatsFilters2(option, nodes.get(docId));
      }
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
         DocumentNode node = new DocumentNode(doc);
         node.setVisible(filter.accept(doc));
         if (node.isVisible())
         {
            dataProvider.getList().add(node);
         }
         nodes.put(doc.getId(), node);
      }

      updatePageCount();

      Log.info("Time to create DocumentNodes: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
      dataProvider.refresh();
   }

   private void updatePageCount()
   {
      int pageCount = (int) Math.ceil(nodes.size() * 1.0 / userOptionsService.getConfigHolder().getState().getDocumentListPageSize());
      display.getPageNavigation().setPageCount(pageCount);
      display.getPageNavigation().setValue(1, true);
   }

   private void gotoPage(Integer page)
   {
      ArrayList<DocumentNode> list = Lists.newArrayList(nodes.values());
      gotoPage(page.intValue(), list);
   }

   private void gotoPage(int page, List<DocumentNode> list)
   {
      int pageSize = userOptionsService.getConfigHolder().getState().getDocumentListPageSize();

      int fromIndex = (page - 1) * pageSize;
      int toIndex = (fromIndex + pageSize) > list.size() ? list.size() : fromIndex + pageSize;

      pageRows = display.buildContent(list.subList(fromIndex, toIndex));
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
         userWorkspaceContext.setSelectedDoc(node.getDocInfo());
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
      updateLastTranslatedInfo(updatedDoc, event.getUpdateInfo().getTransUnit());

      Integer row = pageRows.get(updatedDoc.getId());
      if (row != null)
      {
         display.updateStats(row.intValue(), updatedDoc.getStats());
         display.updateLastTranslatedInfo(row.intValue(), event.getUpdateInfo().getTransUnit());
      }

      eventBus.fireEvent(new DocumentStatsUpdatedEvent(updatedDoc.getId(), updatedDoc.getStats()));

      // refresh document list table
      dataProvider.refresh();

      // update project stats, forward to AppPresenter
      adjustStats(projectStats, updateInfo);
      eventBus.fireEvent(new ProjectStatsUpdatedEvent(projectStats));
   }

   private void updateLastTranslatedInfo(DocumentInfo doc, TransUnit updatedTransUnit)
   {
      doc.setLastTranslatedBy(updatedTransUnit.getLastModifiedBy());
      doc.setLastTranslatedDate(updatedTransUnit.getLastModifiedTime());
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
      display.setLayout(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());
      if (event.getView() == MainView.Documents)
      {
         display.updatePageSize(userOptionsService.getConfigHolder().getState().getDocumentListPageSize());
         updatePageCount();
      }
   }

   @Override
   public void downloadAllFiles()
   {
      WorkspaceId workspaceId = userWorkspaceContext.getWorkspaceContext().getWorkspaceId();
      dispatcher.execute(new DownloadAllFilesAction(workspaceId.getProjectIterationId().getProjectSlug(), workspaceId.getProjectIterationId().getIterationSlug(), workspaceId.getLocaleId().getId(), !isPoProject(getProjectType())), new AsyncCallback<DownloadAllFilesResult>()
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
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, "File ready to download", display.getDownloadAllFilesInlineLink(url)));
            }
         }
      });
   }

   @Override
   public void showUploadDialog(DocumentInfo docInfo)
   {
      display.showUploadDialog(docInfo, userWorkspaceContext.getWorkspaceContext().getWorkspaceId());
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
      if (event.getResults().contains("200"))
      {
         if (event.getResults().contains("Warning"))
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

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getProjectIterationId().setProjectType(event.getProjectType());
      setupDownloadZipButton(event.getProjectType());
   }

   @Override
   public void onPageChange(PageChangeEvent event)
   {
      display.getPageNavigation().setValue(event.getPageNumber());
   }

   @Override
   public void onRunDocValidation(RunDocValidationEvent event)
   {
      if (event.getView() == MainView.Documents)
      {
         display.showLoading(true);
         ArrayList<Long> docIds = new ArrayList<Long>();
         for (DocumentNode node : display.getDocumentListTable().getVisibleItems())
         {
            docIds.add(node.getDocInfo().getId().getId());
         }

         List<ValidationId> valIds = userOptionsService.getConfigHolder().getState().getEnabledValidationIds();

         if (!valIds.isEmpty() && !docIds.isEmpty())
         {
            Log.debug("Run doc validation");
            dispatcher.execute(new RunDocValidationAction(valIds, docIds), new AsyncCallback<RunDocValidationResult>()
            {
               @Override
               public void onFailure(Throwable caught)
               {
                  eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, "Unable to run validation"));
                  display.showLoading(false);
                  eventBus.fireEvent(new DocValidationResultEvent(new Date(), null));
               }

               @Override
               public void onSuccess(RunDocValidationResult result)
               {
                  Log.debug("Success docs validation - " + result.getResult().size());
                  Map<DocumentId, Boolean> resultMap = result.getResult();
                  ArrayList<DocumentId> hasErrorDocs = new ArrayList<DocumentId>();

                  for (Map.Entry<DocumentId, Boolean> entry : resultMap.entrySet())
                  {
                     Boolean hasError = entry.getValue();
                     DocumentNode node = nodes.get(entry.getKey());
                     Integer row = pageRows.get(entry.getKey());

                     if (hasError != null && node != null)
                     {

                        display.updateRowHasError(row.intValue(), hasError.booleanValue());

                        node.getDocInfo().setHasValidationError(hasError.booleanValue());

                        if (hasError.booleanValue())
                        {
                           hasErrorDocs.add(node.getDocInfo().getId());
                        }
                     }
                  }
                  dataProvider.refresh();
                  display.showLoading(false);
                  eventBus.fireEvent(new DocValidationResultEvent(new Date(), hasErrorDocs));
               }
            });
         }
      }
   }

   @Override
   public void sortList(String header, boolean asc)
   {
      ArrayList<DocumentNode> list = Lists.newArrayList(nodes.values());
      HeaderComparator comparator = new HeaderComparator(header);
      Collections.sort(list, comparator);
      if (!asc)
      {
         Collections.reverse(list);
      }
      gotoPage(1, list);
   }

   private class HeaderComparator implements Comparator<DocumentNode>
   {
      private String header;

      public HeaderComparator(String header)
      {
         this.header = header;
      }

      @Override
      public int compare(DocumentNode o1, DocumentNode o2)
      {
         if (header.equals(DocumentListDisplay.PATH_HEADER))
         {
            return comparePath(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.DOC_HEADER))
         {
            return compareDoc(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.STATS_HEADER))
         {
            return compareStats(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.TRANSLATED_HEADER))
         {
            return compareTranslated(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.UNTRANSLATED_HEADER))
         {
            return compareUntranslated(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.REMAINING_HEADER))
         {
            return compareRemaining(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.LAST_UPLOAD_HEADER))
         {
            return compareLastUpload(o1, o2);
         }
         else if (header.equals(DocumentListDisplay.LAST_TRANSLATED_HEADER))
         {
            return compareLastTranslated(o1, o2);
         }
         return 0;
      }

      private int comparePath(DocumentNode o1, DocumentNode o2)
      {
         if (o1.getDocInfo().getPath() == null || o2.getDocInfo().getPath() == null)
         {
            return (o1.getDocInfo().getPath() == null) ? -1 : 1;
         }
         else
         {
            return o1.getDocInfo().getPath().compareTo(o2.getDocInfo().getPath());
         }
      }

      private int compareDoc(DocumentNode o1, DocumentNode o2)
      {
         return o1.getDocInfo().getName().compareTo(o2.getDocInfo().getName());
      }

      private int compareStats(DocumentNode o1, DocumentNode o2)
      {
         boolean statsByWords = true;
         if (display.getSelectedStatsOption().equals(STATS_OPTION_MESSAGE))
         {
            statsByWords = false;
         }
         return o1.getDocInfo().getStats().getApprovedPercent(statsByWords) - o2.getDocInfo().getStats().getApprovedPercent(statsByWords);
      }

      private int compareTranslated(DocumentNode o1, DocumentNode o2)
      {
         if (display.getSelectedStatsOption().equals(STATS_OPTION_MESSAGE))
         {
            return o1.getDocInfo().getStats().getUnitCount().getApproved() - o2.getDocInfo().getStats().getUnitCount().getApproved();
         }
         else
         {
            return o1.getDocInfo().getStats().getWordCount().getApproved() - o2.getDocInfo().getStats().getWordCount().getApproved();
         }
      }

      private int compareUntranslated(DocumentNode o1, DocumentNode o2)
      {
         if (display.getSelectedStatsOption().equals(STATS_OPTION_MESSAGE))
         {
            return o1.getDocInfo().getStats().getUnitCount().getUntranslated() - o2.getDocInfo().getStats().getUnitCount().getUntranslated();
         }
         else
         {
            return o1.getDocInfo().getStats().getWordCount().getUntranslated() - o2.getDocInfo().getStats().getWordCount().getUntranslated();
         }
      }

      private int compareRemaining(DocumentNode o1, DocumentNode o2)
      {
         if (o1.getDocInfo().getStats().getRemainingHours() == o2.getDocInfo().getStats().getRemainingHours())
         {
            return 0;
         }
         return o1.getDocInfo().getStats().getRemainingHours() > o2.getDocInfo().getStats().getRemainingHours() ? 1 : -1;
      }

      private int compareLastUpload(DocumentNode o1, DocumentNode o2)
      {
         if (o1.getDocInfo().getLastChanged() == o2.getDocInfo().getLastChanged())
         {
            return 0;
         }
         if (o1.getDocInfo().getLastChanged() == null)
         {
            return -1;
         }
         if (o2.getDocInfo().getLastChanged() == null)
         {
            return 1;
         }
         return o1.getDocInfo().getLastChanged().after(o2.getDocInfo().getLastChanged()) ? 1 : -1;
      }

      private int compareLastTranslated(DocumentNode o1, DocumentNode o2)
      {
         if (o1.getDocInfo().getLastChanged() == o2.getDocInfo().getLastChanged())
         {
            return 0;
         }
         if (o1.getDocInfo().getLastChanged() == null)
         {
            return -1;
         }
         if (o2.getDocInfo().getLastChanged() == null)
         {
            return 1;
         }
         return o1.getDocInfo().getLastChanged().after(o2.getDocInfo().getLastChanged()) ? 1 : -1;
      }
   }
}
