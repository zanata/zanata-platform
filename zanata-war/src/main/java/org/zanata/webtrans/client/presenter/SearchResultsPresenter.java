/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;
import org.zanata.webtrans.shared.rpc.PreviewReplaceText;
import org.zanata.webtrans.shared.rpc.PreviewReplaceTextResult;
import org.zanata.webtrans.shared.rpc.ReplaceText;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

/**
 * View for project-wide search and replace within textflow targets
 *
 * @author David Mason, damason@redhat.com
 */
public class SearchResultsPresenter extends WidgetPresenter<SearchResultsPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      HasText getSearchResponseLabel();

      /**
       * Set the string that will be highlighted in target content.
       * Set to null or empty string to disable highlight
       *
       * @param highlightString
       */
      void setHighlightString(String highlightString);

      HasValue<String> getFilterTextBox();

      HasValue<String> getReplacementTextBox();

      HasValue<Boolean> getCaseSensitiveChk();

      HasChangeHandlers getSearchFieldSelector();

      String getSelectedSearchField();

      public void setSearching(boolean searching);

      HasClickHandlers getReplaceAllButton();

      HasClickHandlers getSelectAllButton();

      void clearAll();

      void setReplacementMessage(String message, ClickHandler undoButtonHandler);

      void clearReplacementMessage();

      HasData<TransUnitReplaceInfo> addDocument(
            String docName,
            ClickHandler viewDocClickHandler,
            ClickHandler searchDocClickHandler,
            Delegate<TransUnitReplaceInfo> previewDelegate,
            Delegate<TransUnitReplaceInfo> replaceDelegate,
            Delegate<TransUnitReplaceInfo> undoDelegate,
            SelectionModel<TransUnitReplaceInfo> selectionModel,
            ValueChangeHandler<Boolean> selectAllHandler);
   }

   private final CachingDispatchAsync dispatcher;
   private final History history;
   private AsyncCallback<GetProjectTransUnitListsResult> projectSearchCallback;
   private Delegate<TransUnitReplaceInfo> previewButtonDelegate;
   private Delegate<TransUnitReplaceInfo> replaceButtonDelegate;
   private Delegate<TransUnitReplaceInfo> undoButtonDelegate;
   private Comparator<TransUnitReplaceInfo> tuInfoComparator;

   /**
    * Model objects for tables in display. Changes to these are reflected in the
    * view.
    */
   private Map<Long, ListDataProvider<TransUnitReplaceInfo>> documentDataProviders;

   /**
    * Selection model objects for tables in display. Used to determine which
    * transunits are selected
    */
   private Map<Long, MultiSelectionModel<TransUnitReplaceInfo>> documentSelectionModels;

   private Map<TransUnitId, TransUnitReplaceInfo> allReplaceInfos;

   /**
    * most recent history state that was responded to
    */
   private HistoryToken currentHistoryState = null;

   private final WebTransMessages messages;

   @Inject
   public SearchResultsPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, History history, final WebTransMessages webTransMessages)
   {
      super(display, eventBus);
      messages = webTransMessages;
      this.history = history;
      this.dispatcher = dispatcher;
   }

   @Override
   protected void onBind()
   {
      projectSearchCallback = buildProjectSearchCallback();
      previewButtonDelegate = buildPreviewButtonDelegate();
      replaceButtonDelegate = buildReplaceButtonDelegate();
      undoButtonDelegate = buildUndoButtonDelegate();
      documentDataProviders = new HashMap<Long, ListDataProvider<TransUnitReplaceInfo>>();
      documentSelectionModels = new HashMap<Long, MultiSelectionModel<TransUnitReplaceInfo>>();
      allReplaceInfos = new HashMap<TransUnitId, TransUnitReplaceInfo>();
      tuInfoComparator = buildTransUnitReplaceInfoComparator();

      // TODO use explicit 'search' button and add enter key press event for
      // text box
      registerHandler(display.getFilterTextBox().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (!event.getValue().equals(token.getProjectSearchText()))
            {
               token.setProjectSearchText(event.getValue());
               history.newItem(token.toTokenString());
            }
         }
      }));

      registerHandler(display.getReplacementTextBox().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (!event.getValue().equals(token.getProjectSearchReplacement()))
            {
               token.setProjectSearchReplacement(event.getValue());
               history.newItem(token.toTokenString());
            }
         }
      }));

      registerHandler(display.getCaseSensitiveChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (event.getValue() != token.getProjectSearchCaseSensitive())
            {
               token.setProjectSearchCaseSensitive(event.getValue());
               history.newItem(token.toTokenString());
            }
         }
      }));

      registerHandler(display.getSearchFieldSelector().addChangeHandler(new ChangeHandler()
      {

         @Override
         public void onChange(ChangeEvent event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            String selected = display.getSelectedSearchField();
            boolean searchSource = selected.equals("source") || selected.equals("both");
            boolean searchTarget = selected.equals("target") || selected.equals("both");
            token.setProjectSearchInSource(searchSource);
            token.setProjectSearchInTarget(searchTarget);
            history.newItem(token.toTokenString());
         }
      }));

      registerHandler(display.getReplaceAllButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            List<TransUnit> selected = new ArrayList<TransUnit>();
            Set<Long> modifiedDocs = new HashSet<Long>();
            for (Entry<Long, MultiSelectionModel<TransUnitReplaceInfo>> entry : documentSelectionModels.entrySet())
            {
               for (TransUnitReplaceInfo info : entry.getValue().getSelectedSet())
               {
                  selected.add(info.getTransUnit());
                  info.setState(ReplacementState.Replacing);
                  modifiedDocs.add(entry.getKey());
               }
            }

            if (selected.isEmpty())
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Warning, messages.noTextFlowsSelected()));
            }
            else
            {
               fireReplaceTextEvent(selected);
               refreshDocumentDisplays(modifiedDocs);
            }
         }
      }));

      registerHandler(display.getSelectAllButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            for (Entry<Long, ListDataProvider<TransUnitReplaceInfo>> en : documentDataProviders.entrySet())
            {
               MultiSelectionModel<TransUnitReplaceInfo> selectionModel = documentSelectionModels.get(en.getKey());
               if (selectionModel != null)
               {
                  for (TransUnitReplaceInfo tu : en.getValue().getList())
                  {
                     selectionModel.setSelected(tu, true);
                  }
               }
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

            HistoryToken token = HistoryToken.fromTokenString(event.getValue());

            boolean caseSensitivityChanged = token.getProjectSearchCaseSensitive() != currentHistoryState.getProjectSearchCaseSensitive();
            boolean searchTextChanged = !token.getProjectSearchText().equals(currentHistoryState.getProjectSearchText());
            boolean searchFieldsChanged = token.isProjectSearchInSource() != currentHistoryState.isProjectSearchInSource();
            searchFieldsChanged |= token.isProjectSearchInTarget() != currentHistoryState.isProjectSearchInTarget();
            if (caseSensitivityChanged ||  searchTextChanged || searchFieldsChanged)
            {
               display.setHighlightString(token.getProjectSearchText());
               display.getFilterTextBox().setValue(token.getProjectSearchText(), true);
               display.getCaseSensitiveChk().setValue(token.getProjectSearchCaseSensitive(), false);
               // TODO set selection in source/target selector

               documentDataProviders.clear();
               documentSelectionModels.clear();
               allReplaceInfos.clear();
               display.clearAll();

               if (!token.getProjectSearchText().isEmpty())
               {
                  display.setSearching(true);
                  dispatcher.execute(new GetProjectTransUnitLists(token.getProjectSearchText(), token.isProjectSearchInSource(), token.isProjectSearchInTarget(), token.getProjectSearchCaseSensitive()), projectSearchCallback);
               }
            }

            if (!token.getProjectSearchReplacement().equals(currentHistoryState.getProjectSearchReplacement()))
            {
               display.getReplacementTextBox().setValue(token.getProjectSearchReplacement(), true);
            }

            currentHistoryState = token;
         }
      });

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {

         @Override
         public void onTransUnitUpdated(final TransUnitUpdatedEvent event)
         {
            TransUnitUpdateInfo updateInfo = event.getUpdateInfo();
            TransUnitReplaceInfo replaceInfo = allReplaceInfos.get(updateInfo.getTransUnit().getId());
            if (replaceInfo == null)
            {
               Log.debug("no matching TU in document for TU update, id: " + updateInfo.getTransUnit().getId().getId());
               return;
            }
            Log.debug("found matching TU for TU update, id: " + updateInfo.getTransUnit().getId().getId());

            MultiSelectionModel<TransUnitReplaceInfo> selectionModel = documentSelectionModels.get(updateInfo.getDocumentId().getId());
            if (selectionModel == null)
            {
               Log.error("missing selection model for document, id: " + updateInfo.getDocumentId().getId());
            }
            else
            {
               // no need to do this as replaceInfo only has properties changed
               // safe to remove if desired behaviour is keeping selected
               selectionModel.setSelected(replaceInfo, false);
            }

            if (replaceInfo.getState() == ReplacementState.Replaced && replaceInfo.getTransUnit().getVerNum() != updateInfo.getTransUnit().getVerNum())
            {
               // can't undo after additional update
               replaceInfo.setState(ReplacementState.Replaceable);
               replaceInfo.setReplaceInfo(null);
            }
            replaceInfo.setTransUnit(updateInfo.getTransUnit());

            // force table refresh as property changes are not detected
            refreshDocument(replaceInfo.getDocId());
         }
      }));

   }

   private void showDocInEditor(String doc, boolean runSearch)
   {
      HistoryToken token = HistoryToken.fromTokenString(history.getToken());
      token.setDocumentPath(doc);
      token.setView(MainView.Editor);
      if (runSearch)
      {
         token.setSearchText(token.getProjectSearchText());
      }
      else
      {
         token.setSearchText("");
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

   private AsyncCallback<GetProjectTransUnitListsResult> buildProjectSearchCallback()
   {
      return new AsyncCallback<GetProjectTransUnitListsResult>()
      {

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("[SearchResultsPresenter] failed project-wide search request: " + caught.getMessage());
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.searchFailed()));
            display.clearAll();
            display.getSearchResponseLabel().setText(messages.searchFailed());
         }

         @Override
         public void onSuccess(GetProjectTransUnitListsResult result)
         {
            if (result.getDocumentIds().size() == 0)
            {
               display.getSearchResponseLabel().setText("");
            }
            else
            {
               display.getSearchResponseLabel().setText(messages.searchFoundResultsInDocuments(result.getDocumentIds().size()));
            }
            documentDataProviders.clear();
            documentSelectionModels.clear();
            allReplaceInfos.clear();
            display.clearAll();
            for (Long docId : result.getDocumentIds())
            {
               final String doc = result.getDocPath(docId);
               ClickHandler showDocClickHandler = new ClickHandler()
               {
                  @Override
                  public void onClick(ClickEvent event)
                  {
                     showDocInEditor(doc, false);
                  }
               };

               ClickHandler searchDocClickHandler = new ClickHandler()
               {
                  @Override
                  public void onClick(ClickEvent event)
                  {
                     showDocInEditor(doc, true);
                  }
               };

               final MultiSelectionModel<TransUnitReplaceInfo> selectionModel = new MultiSelectionModel<TransUnitReplaceInfo>();
               final ListDataProvider<TransUnitReplaceInfo> dataProvider = new ListDataProvider<TransUnitReplaceInfo>();

               // TODO "select entire document" checkbox if all rows selected
               // (and clear for none selected)
               HasData<TransUnitReplaceInfo> table;
               table = display.addDocument(doc, showDocClickHandler, searchDocClickHandler,
                     previewButtonDelegate, replaceButtonDelegate, undoButtonDelegate,
                     selectionModel, buildSelectAllHandler(selectionModel, dataProvider));
               dataProvider.addDataDisplay(table);

               List<TransUnitReplaceInfo> data = dataProvider.getList();
               for (TransUnit tu : result.getUnits(docId))
               {
                  TransUnitReplaceInfo info = new TransUnitReplaceInfo(docId, tu);
                  data.add(info);
                  allReplaceInfos.put(tu.getId(), info);
               }
               Collections.sort(data, tuInfoComparator);
               documentDataProviders.put(docId, dataProvider);
               documentSelectionModels.put(docId, selectionModel);
            }
            display.setSearching(false);
         }

      };
   }

   private Delegate<TransUnitReplaceInfo> buildReplaceButtonDelegate()
   {
      return new Delegate<TransUnitReplaceInfo>()
      {

         @Override
         public void execute(TransUnitReplaceInfo info)
         {
            info.setState(ReplacementState.Replacing);
            refreshDocument(info.getDocId());
            fireReplaceTextEvent(Collections.singletonList(info.getTransUnit()));
         }

      };
   }

   private Delegate<TransUnitReplaceInfo> buildUndoButtonDelegate()
   {
      return new Delegate<TransUnitReplaceInfo>()
      {

         @Override
         public void execute(final TransUnitReplaceInfo info)
         {
            info.setState(ReplacementState.Undoing);
            refreshDocument(info.getDocId());
            eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.undoInProgress()));
            // TODO extract this into a separate method to re-use for bulk
            // revert
            RevertTransUnitUpdates action = new RevertTransUnitUpdates(info.getReplaceInfo());
            dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>()
            {

               @Override
               public void onFailure(Throwable caught)
               {
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.undoReplacementFailure()));
               }

               @Override
               public void onSuccess(UpdateTransUnitResult result)
               {
                  eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.undoSuccess()));
                  // TODO update model with new values
                  info.setState(ReplacementState.Replaceable);
                  refreshDocument(info.getDocId());
               }
            });
         }
      };
   }

   private Delegate<TransUnitReplaceInfo> buildPreviewButtonDelegate()
   {
      return new Delegate<TransUnitReplaceInfo>()
      {

         @Override
         public void execute(TransUnitReplaceInfo info)
         {
            info.setState(ReplacementState.FetchingPreview);
            refreshDocument(info.getDocId());

            final String searchText = currentHistoryState.getProjectSearchText();
            final String replacement = currentHistoryState.getProjectSearchReplacement();
            boolean caseSensitive = currentHistoryState.getProjectSearchCaseSensitive();
            ReplaceText action = new ReplaceText(Collections.singletonList(info.getTransUnit()), searchText, replacement, caseSensitive);
            PreviewReplaceText previewAction = new PreviewReplaceText(action);
            dispatcher.execute(previewAction, new AsyncCallback<PreviewReplaceTextResult>()
            {

               @Override
               public void onFailure(Throwable e)
               {
                  Log.error("[SearchResultsPresenter] Preview replace text failure " + e, e);
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.previewFailed()));
                  // TODO consider whether possible/desired to change TU state from 'previewing'
               }

               @Override
               public void onSuccess(final PreviewReplaceTextResult result)
               {
                  final Set<Long> updatedDocs = new HashSet<Long>();
                  for (TransUnitUpdatePreview preview : result.getPreviews())
                  {
                     TransUnitReplaceInfo replaceInfo = allReplaceInfos.get(preview.getId());
                     if (replaceInfo == null)
                     {
                        Log.error("no replace info found for previewed text flow");
                     }
                     else
                     {
                        Log.debug("setting preview state for preview id: " + preview.getId());
                        replaceInfo.setPreview(preview);
                        replaceInfo.setState(ReplacementState.PreviewAvailable);
                        updatedDocs.add(replaceInfo.getDocId());
                     }
                  }
                  // force table refresh as property changes are not detected
                  refreshDocumentDisplays(updatedDocs);
                  eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.fetchedPreview()));
               }

            });
         }

      };
   }

   /**
    * Handler to select and de-select all text flows in a document
    * 
    * @param selectionModel
    * @param dataProvider
    * @return the new handler
    */
   private ValueChangeHandler<Boolean> buildSelectAllHandler(final MultiSelectionModel<TransUnitReplaceInfo> selectionModel, final ListDataProvider<TransUnitReplaceInfo> dataProvider)
   {
      return new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            if (event.getValue())
            {
               for (TransUnitReplaceInfo info : dataProvider.getList())
               {
                  selectionModel.setSelected(info, true);
               }
            }
            else
            {
               selectionModel.clear();
            }
         }
      };
   }

   /**
    * @param info
    */
   private void refreshDocument(Long documentId)
   {
      ListDataProvider<TransUnitReplaceInfo> dataProvider = documentDataProviders.get(documentId);
      if (dataProvider != null)
      {
         dataProvider.refresh();
      }
   }

   /**
    * @param toReplace list of TransUnits to replace
    */
   private void fireReplaceTextEvent(List<TransUnit> toReplace)
   {
      // TODO set info to replacing... before calling this event
      final String searchText = currentHistoryState.getProjectSearchText();
      final String replacement = currentHistoryState.getProjectSearchReplacement();
      boolean caseSensitive = currentHistoryState.getProjectSearchCaseSensitive();
      ReplaceText action = new ReplaceText(toReplace, searchText, replacement, caseSensitive);
      dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>()
      {

         @Override
         public void onFailure(Throwable e)
         {
            Log.error("[SearchResultsPresenter] Replace text failure " + e, e);
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.replaceTextFailure()));
            // TODO consider whether possible/desired to change TU state from
            // 'replacing'
         }

         @Override
         public void onSuccess(final UpdateTransUnitResult result)
         {
            int successes = 0;
            final Set<Long> updatedDocs = new HashSet<Long>();
            for (TransUnitUpdateInfo updateInfo : result.getUpdateInfoList())
            {
               if (updateInfo.isSuccess())
               {
                  successes++;
                  TransUnitReplaceInfo replaceInfo = allReplaceInfos.get(updateInfo.getTransUnit().getId());
                  if (replaceInfo != null)
                  {
                     replaceInfo.setReplaceInfo(updateInfo);
                     replaceInfo.setState(ReplacementState.Replaced);
                     // this should be done when the TU update event comes in anyway
                     // may want to remove this
                     replaceInfo.setTransUnit(updateInfo.getTransUnit());
                  }
                  updatedDocs.add(updateInfo.getDocumentId().getId());
               }
               // individual failure behaviour not yet defined
            }

            // force table refresh as property changes are not detected
            refreshDocumentDisplays(updatedDocs);

            eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.replacedTextSuccess()));

            String message = messages.replacedTextInMultipleTextFlows(searchText, replacement, successes);
            display.setReplacementMessage(message, new ClickHandler()
            {

               @Override
               public void onClick(ClickEvent event)
               {
                  display.clearReplacementMessage();

                  RevertTransUnitUpdates action = new RevertTransUnitUpdates();
                  for (TransUnitUpdateInfo info : result.getUpdateInfoList())
                  {
                     action.addUpdateToRevert(info);
                     allReplaceInfos.get(info.getTransUnit().getId()).setState(ReplacementState.Undoing);
                  }
                  refreshDocumentDisplays(updatedDocs);
                  dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>()
                  {

                     @Override
                     public void onFailure(Throwable caught)
                     {
                        eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.undoReplacementFailure()));
                     }

                     @Override
                     public void onSuccess(UpdateTransUnitResult result)
                     {
                        eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.undoSuccess()));
                        for (TransUnitUpdateInfo info : result.getUpdateInfoList())
                        {
                           TransUnitReplaceInfo replaceInfo = allReplaceInfos.get(info.getTransUnit().getId());
                           replaceInfo.setState(ReplacementState.Replaceable);

                        }
                        refreshDocumentDisplays(updatedDocs);
                        // TODO update model with new values
                     }
                  });
               }
            });
         }
      });
   }

   public enum ReplacementState
   {
      NotReplaceable, Replaceable, FetchingPreview, PreviewAvailable, Replacing, Replaced, Undoing
   }

   public class TransUnitReplaceInfo
   {
      private ReplacementState state;
      private Long docId;
      private TransUnit tu;
      private TransUnitUpdatePreview preview;
      private TransUnitUpdateInfo replaceInfo;

      public TransUnitReplaceInfo(Long containingDocId, TransUnit tu)
      {
         this.docId = containingDocId;
         this.tu = tu;
         preview = null;
         replaceInfo = null;
         state = ReplacementState.Replaceable;
      }

      public TransUnit getTransUnit()
      {
         return tu;
      }

      public void setTransUnit(TransUnit tu)
      {
         this.tu = tu;
      }

      public TransUnitUpdatePreview getPreview()
      {
         return preview;
      }

      public void setPreview(TransUnitUpdatePreview preview)
      {
         this.preview = preview;
      }

      public TransUnitUpdateInfo getReplaceInfo()
      {
         return replaceInfo;
      }

      public void setReplaceInfo(TransUnitUpdateInfo replaceInfo)
      {
         this.replaceInfo = replaceInfo;
      }

      public ReplacementState getState()
      {
         return state;
      }

      public void setState(ReplacementState state)
      {
         this.state = state;
      }

      public Long getDocId()
      {
         return docId;
      }

   }

   private Comparator<TransUnitReplaceInfo> buildTransUnitReplaceInfoComparator()
   {
      return new Comparator<SearchResultsPresenter.TransUnitReplaceInfo>()
      {

         @Override
         public int compare(TransUnitReplaceInfo o1, TransUnitReplaceInfo o2)
         {
            if (o1 == o2)
            {
               return 0;
            }
            if (o1 != null)
            {
               return (o2 != null ? Integer.valueOf(o1.getTransUnit().getRowIndex()).compareTo(o2.getTransUnit().getRowIndex()) : 1);
            }
            return -1;
         }
      };
   }

   /**
    * @param docsToRefresh
    */
   private void refreshDocumentDisplays(Set<Long> docsToRefresh)
   {
      for (Long docId : docsToRefresh)
      {
         ListDataProvider<TransUnitReplaceInfo> dataProvider = documentDataProviders.get(docId);
         if (dataProvider != null)
         {
            dataProvider.refresh();
         }
      }
   }
}
