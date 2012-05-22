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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
       * Set the string that will be highlighted in target content. Set to null
       * or empty string to disable highlight
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

      HasClickHandlers getPreviewButton();

      HasClickHandlers getReplaceAllButton();

      HasClickHandlers getSelectAllButton();

      void clearAll();

      /**
       * Shows a message in the replacement region with an associated 'undo'
       * button
       * 
       * @param message
       * @param undoButtonHandler
       * @see #clearReplacementMessage()
       */
      void setReplacementMessage(String message, ClickHandler undoButtonHandler);

      /**
       * @see #setReplacementMessage(String, ClickHandler)
       */
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

      registerHandler(display.getPreviewButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            List<TransUnitReplaceInfo> selected = new ArrayList<TransUnitReplaceInfo>();
            for (MultiSelectionModel<TransUnitReplaceInfo> sel : documentSelectionModels.values())
            {
               selected.addAll(sel.getSelectedSet());
            }
            if (selected.isEmpty())
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Warning, messages.noTextFlowsSelected()));
            }
            else
            {
               firePreviewEvent(selected, true);
            }
         }
      }));

      registerHandler(display.getReplaceAllButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            List<TransUnitReplaceInfo> selected = new ArrayList<TransUnitReplaceInfo>();
            for (MultiSelectionModel<TransUnitReplaceInfo> sel : documentSelectionModels.values())
            {
               selected.addAll(sel.getSelectedSet());
            }
            if (selected.isEmpty())
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Warning, messages.noTextFlowsSelected()));
            }
            else
            {
               fireReplaceTextEvent(selected);
            }
         }
      }));


      // TODO check "select entire document" checkbox if all rows individually
      // selected (and clear for none selected)
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
            processHistoryToken(HistoryToken.fromTokenString(event.getValue()));
         }
      });

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {

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


            if (replaceInfo.getState() == ReplacementState.Replaced && replaceInfo.getTransUnit().getVerNum() != updateInfo.getTransUnit().getVerNum())
            {
               // can't undo after additional update
               replaceInfo.setState(ReplacementState.Replaceable);
               replaceInfo.setReplaceInfo(null);

               MultiSelectionModel<TransUnitReplaceInfo> selectionModel = documentSelectionModels.get(updateInfo.getDocumentId().getId());
               if (selectionModel == null)
               {
                  Log.error("missing selection model for document, id: " + updateInfo.getDocumentId().getId());
               }
               else
               {
                  // clear selection to avoid accidental inclusion in batch operations
                  selectionModel.setSelected(replaceInfo, false);
               }
            }
            replaceInfo.setTransUnit(updateInfo.getTransUnit());
            refreshInfoDisplay(replaceInfo);
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
               // TODO change to "showing results for search "foo bar" (X documents).
               display.getSearchResponseLabel().setText(messages.searchFoundResultsInDocuments(result.getDocumentIds().size()));
            }
            displaySearchResults(result);
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
            fireReplaceTextEvent(Collections.singletonList(info));
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
            fireUndoEvent(Collections.singletonList(info.getReplaceInfo()));
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
            List<TransUnitReplaceInfo> replaceInfos = Collections.singletonList(info);
            final boolean global = false;

            firePreviewEvent(replaceInfos, global);
         }

      };
   }

   /**
    * Fire a {@link PreviewReplaceText} event for the given {@link TransUnit}s
    * using parameters from the current history state. This will also update the
    * state and refresh the table to show 'previewing' indicator.
    * 
    * @param toPreview
    * @param global true if this is a 'replace all' event and should update the
    *           global replace button
    */
   private void firePreviewEvent(List<TransUnitReplaceInfo> toPreview, final boolean global)
   {
      List<TransUnit> transUnits = new ArrayList<TransUnit>();
      for (TransUnitReplaceInfo replaceInfo : toPreview)
      {
         transUnits.add(replaceInfo.getTransUnit());
         replaceInfo.setState(ReplacementState.FetchingPreview);
         refreshInfoDisplay(replaceInfo);
      }

      final String searchText = currentHistoryState.getProjectSearchText();
      final String replacement = currentHistoryState.getProjectSearchReplacement();
      boolean caseSensitive = currentHistoryState.getProjectSearchCaseSensitive();
      ReplaceText action = new ReplaceText(transUnits, searchText, replacement, caseSensitive);
      PreviewReplaceText previewAction = new PreviewReplaceText(action);
      dispatcher.execute(previewAction, new AsyncCallback<PreviewReplaceTextResult>()
      {

         @Override
         public void onFailure(Throwable e)
         {
            Log.error("[SearchResultsPresenter] Preview replace text failure " + e, e);
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.previewFailed()));
            // may want to change TU state from 'previewing' (possibly error state)
         }

         @Override
         public void onSuccess(final PreviewReplaceTextResult result)
         {
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
                  refreshInfoDisplay(replaceInfo);
               }
            }
            if (global)
            {
               // TODO update 'Replace All Selected' button to allow replace.
            }
            eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.fetchedPreview()));
         }

      });
   }

   /**
    * Fire a {@link ReplaceText} event for the given {@link TransUnit}s
    * using parameters from the current history state. This will also update the
    * state and refresh the table to show 'replacing' indicator.
    * 
    * @param toReplace list of TransUnits to replace
    */
   private void fireReplaceTextEvent(List<TransUnitReplaceInfo> toReplace)
   {
      List<TransUnit> transUnits = new ArrayList<TransUnit>();
      for (TransUnitReplaceInfo info : toReplace)
      {
         transUnits.add(info.getTransUnit());
         info.setState(ReplacementState.Replacing);
         refreshInfoDisplay(info);
      }

      final String searchText = currentHistoryState.getProjectSearchText();
      final String replacement = currentHistoryState.getProjectSearchReplacement();
      boolean caseSensitive = currentHistoryState.getProjectSearchCaseSensitive();
      ReplaceText action = new ReplaceText(transUnits, searchText, replacement, caseSensitive);
      dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>()
      {

         @Override
         public void onFailure(Throwable e)
         {
            Log.error("[SearchResultsPresenter] Replace text failure " + e, e);
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.replaceTextFailure()));
            // may want to change state from 'replacing' (possibly add error state)
         }

         @Override
         public void onSuccess(final UpdateTransUnitResult result)
         {
            final List<TransUnitUpdateInfo> updateInfoList = result.getUpdateInfoList();
            int successes = processSuccessfulReplacements(updateInfoList);
            eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.replacedTextSuccess()));

            String message = messages.replacedTextInMultipleTextFlows(searchText, replacement, successes);
            display.setReplacementMessage(message, new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  display.clearReplacementMessage();
                  fireUndoEvent(updateInfoList);
               }
            });
         }
      });
   }

   /**
    * Fire a {@link RevertTransUnitUpdates} event to request undoing of the
    * given updates.
    * 
    * @param updateInfoList updates that are to be reverted
    */
   private void fireUndoEvent(List<TransUnitUpdateInfo> updateInfoList)
   {
      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.undoInProgress()));
      RevertTransUnitUpdates action = new RevertTransUnitUpdates();
      for (TransUnitUpdateInfo updateInfo : updateInfoList)
      {
         action.addUpdateToRevert(updateInfo);
         TransUnitReplaceInfo replaceInfo = allReplaceInfos.get(updateInfo.getTransUnit().getId());
         replaceInfo.setState(ReplacementState.Undoing);
         refreshInfoDisplay(replaceInfo);
      }
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
               refreshInfoDisplay(replaceInfo);
            }
            // update model with new values?
         }
      });
   }

   /**
    * Update data providers and refresh display for successful replacements.
    * 
    * @param updateInfoList info on replacements. If any of these are not
    *           successful, they are ignored.
    * @return the number of updates that are
    *         {@link TransUnitUpdateInfo#isSuccess()}
    */
   private int processSuccessfulReplacements(final List<TransUnitUpdateInfo> updateInfoList)
   {
      int successes = 0;
      for (TransUnitUpdateInfo updateInfo : updateInfoList)
      {
         if (updateInfo.isSuccess())
         {
            successes++;
            TransUnitReplaceInfo replaceInfo = allReplaceInfos.get(updateInfo.getTransUnit().getId());
            if (replaceInfo != null)
            {
               replaceInfo.setReplaceInfo(updateInfo);
               replaceInfo.setState(ReplacementState.Replaced);
               // this should be done when the TU update event comes in
               // anyway may want to remove this
               replaceInfo.setTransUnit(updateInfo.getTransUnit());
               refreshInfoDisplay(replaceInfo);
            }
         }
         // individual failure behaviour not yet defined
      }
      return successes;
   }

   /**
    * Sets the info item to its current index in the containing data
    * provider to force the provider to recognize that it has changed.
    * 
    * @param info
    */
   private void refreshInfoDisplay(TransUnitReplaceInfo info)
   {
      ListDataProvider<TransUnitReplaceInfo> dataProvider = documentDataProviders.get(info.getDocId());
      if (dataProvider != null)
      {
         List<TransUnitReplaceInfo> list = dataProvider.getList();
         try
         {
            list.set(list.indexOf(info), info);
         }
         catch (IndexOutOfBoundsException e)
         {
            Log.error("failed to re-set info object in its dataprovider", e);
         }
      }
   }

   /**
    * Show search results as documents in the display. This will replace any
    * existing results being displayed.
    * 
    * @param result results to display
    */
   private void displaySearchResults(GetProjectTransUnitListsResult result)
   {
      clearAllExistingData();
      for (Long docId : result.getDocumentIds())
      {
         displayDocumentResults(docId, result.getDocPath(docId), result.getUnits(docId));
      }
   }

   /**
    * @param docId
    * @param docPathName
    * @param transUnits
    */
   private void displayDocumentResults(Long docId, final String docPathName, List<TransUnit> transUnits)
   {
      final MultiSelectionModel<TransUnitReplaceInfo> selectionModel = new MultiSelectionModel<TransUnitReplaceInfo>();
      final ListDataProvider<TransUnitReplaceInfo> dataProvider = new ListDataProvider<TransUnitReplaceInfo>();
      documentDataProviders.put(docId, dataProvider);
      documentSelectionModels.put(docId, selectionModel);

      HasData<TransUnitReplaceInfo> table = display.addDocument(docPathName,
            showDocClickHandler(docPathName, false),
            showDocClickHandler(docPathName, true),
            previewButtonDelegate,
            replaceButtonDelegate,
            undoButtonDelegate,
            selectionModel,
            selectAllHandler(selectionModel, dataProvider));
      dataProvider.addDataDisplay(table);

      List<TransUnitReplaceInfo> data = dataProvider.getList();
      for (TransUnit tu : transUnits)
      {
         TransUnitReplaceInfo info = new TransUnitReplaceInfo(docId, tu);
         data.add(info);
         allReplaceInfos.put(tu.getId(), info);
      }
      Collections.sort(data, TransUnitReplaceInfo.getComparator());
   }

   /**
    * Build a click handler to show a document in the editor.
    * 
    * @see #showDocInEditor(String, boolean)
    */
   private ClickHandler showDocClickHandler(final String docPathName, final boolean runSearch)
   {
      ClickHandler showDocClickHandler = new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            showDocInEditor(docPathName, runSearch);
         }
      };
      return showDocClickHandler;
   }

   /**
    * Build a handler to select and de-select all text flows in a document
    * 
    * @param selectionModel
    * @param dataProvider
    * @return the new handler
    */
   private ValueChangeHandler<Boolean> selectAllHandler(final MultiSelectionModel<TransUnitReplaceInfo> selectionModel, final ListDataProvider<TransUnitReplaceInfo> dataProvider)
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

   private void processHistoryToken(HistoryToken token)
   {
      if (currentHistoryState == null)
         currentHistoryState = new HistoryToken(); // default values


      boolean caseSensitivityChanged = token.getProjectSearchCaseSensitive() != currentHistoryState.getProjectSearchCaseSensitive();
      boolean searchTextChanged = !token.getProjectSearchText().equals(currentHistoryState.getProjectSearchText());
      boolean searchFieldsChanged = token.isProjectSearchInSource() != currentHistoryState.isProjectSearchInSource();
      searchFieldsChanged |= token.isProjectSearchInTarget() != currentHistoryState.isProjectSearchInTarget();
      if (caseSensitivityChanged || searchTextChanged || searchFieldsChanged)
      {
         display.setHighlightString(token.getProjectSearchText());
         display.getFilterTextBox().setValue(token.getProjectSearchText(), false);
         display.getCaseSensitiveChk().setValue(token.getProjectSearchCaseSensitive(), false);
         // TODO set selection in source/target selector

         clearAllExistingData();

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

   /**
    * Clear all data providers, selection models, replace infos, and removes all
    * documents from the display
    */
   private void clearAllExistingData()
   {
      documentDataProviders.clear();
      documentSelectionModels.clear();
      allReplaceInfos.clear();
      display.clearAll();
   }

}
