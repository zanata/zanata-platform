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
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;
import org.zanata.webtrans.shared.rpc.ReplaceText;
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
      HasText getTestLabel();

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

      HasClickHandlers getReplaceAllButton();

      void clearAll();

      HasClickHandlers addDocumentLabel(String docName);

      HasData<TransUnitReplaceInfo> addTUTable(Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate, SelectionModel<TransUnitReplaceInfo> selectionModel, ValueChangeHandler<Boolean> selectAllHandler);
   }

   private final CachingDispatchAsync dispatcher;
   private final History history;
   private AsyncCallback<GetProjectTransUnitListsResult> projectSearchCallback;
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

   /**
    * most recent history state that was responded to
    */
   private HistoryToken currentHistoryState = null;

   @Inject
   public SearchResultsPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, History history)
   {
      super(display, eventBus);
      this.history = history;
      this.dispatcher = dispatcher;
   }

   @Override
   protected void onBind()
   {
      projectSearchCallback = buildProjectSearchCallback();
      replaceButtonDelegate = buildReplaceButtonDelegate();
      undoButtonDelegate = buildUndoButtonDelegate();
      documentDataProviders = new HashMap<Long, ListDataProvider<TransUnitReplaceInfo>>();
      documentSelectionModels = new HashMap<Long, MultiSelectionModel<TransUnitReplaceInfo>>();
      tuInfoComparator = buildTransUnitReplaceInfoComparator();


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
            for (MultiSelectionModel<TransUnitReplaceInfo> sm : documentSelectionModels.values())
            {
               for (TransUnitReplaceInfo info : sm.getSelectedSet())
               {
                  selected.add(info.getTransUnit());
               }
            }

            if (selected.isEmpty())
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Warning, "Nothing selected"));
            }
            else
            {
               fireReplaceTextEvent(selected);
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

               documentDataProviders.clear();
               documentSelectionModels.clear();
               display.clearAll();

               if (!token.getProjectSearchText().isEmpty())
               {
                  //TODO show loading indicator
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
            TransUnitReplaceInfo replaceInfo = getReplaceInfoForUpdatedTU(updateInfo);
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

            if (replaceInfo.isUndoable() && replaceInfo.getTransUnit().getVerNum() != updateInfo.getTransUnit().getVerNum())
            {
               // can't undo after new update
               replaceInfo.setUndoable(false);
               replaceInfo.setReplaceable(true);
               replaceInfo.setReplacing(false);
               replaceInfo.setReplaceInfo(null);
            }
            replaceInfo.setTransUnit(updateInfo.getTransUnit());

            // force table refresh as property changes are not detected
            ListDataProvider<TransUnitReplaceInfo> dataProvider = documentDataProviders.get(updateInfo.getDocumentId().getId());
            if (dataProvider != null)
            {
               dataProvider.refresh();
            }
         }
      }));

   }

   private void showDocInEditor(String doc)
   {
      HistoryToken token = HistoryToken.fromTokenString(history.getToken());
      token.setDocumentPath(doc);
      token.setView(MainView.Editor);
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
      return new AsyncCallback<GetProjectTransUnitListsResult>() {

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("[SearchResultsPresenter] failed project-wide search request: " + caught.getMessage());
            eventBus.fireEvent(new NotificationEvent(Severity.Error, "Project-wide search failed"));
            display.clearAll();
            display.getTestLabel().setText("Project TU search failed");
         }

         @Override
         public void onSuccess(GetProjectTransUnitListsResult result)
         {
            display.getTestLabel().setText("Project TU search returned documents: " + result.getDocumentIds().size());

            //TODO extract clearAllData function for these 3 lines
            documentDataProviders.clear();
            documentSelectionModels.clear();
            display.clearAll();
            for (Long docId : result.getDocumentIds())
            {
               final String doc = result.getDocPath(docId);
               HasClickHandlers docLabel = display.addDocumentLabel(doc);
               docLabel.addClickHandler(new ClickHandler() {

                  @Override
                  public void onClick(ClickEvent event)
                  {
                     showDocInEditor(doc);
                  }

               });

               final MultiSelectionModel<TransUnitReplaceInfo> selectionModel = new MultiSelectionModel<TransUnitReplaceInfo>();
               final ListDataProvider<TransUnitReplaceInfo> dataProvider = new ListDataProvider<TransUnitReplaceInfo>();

               // TODO set selected value for header based on selection of rows?
               HasData<TransUnitReplaceInfo> table = display.addTUTable(replaceButtonDelegate, undoButtonDelegate, selectionModel, new ValueChangeHandler<Boolean>()
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
               });
               dataProvider.addDataDisplay(table);

               List<TransUnitReplaceInfo> data = dataProvider.getList();
               for (TransUnit tu : result.getUnits(docId))
               {
                  data.add(new TransUnitReplaceInfo(tu));
               }
               Collections.sort(data, tuInfoComparator);
               documentDataProviders.put(docId, dataProvider);
               documentSelectionModels.put(docId, selectionModel);
            }
         }

      };
   }

   private Delegate<TransUnitReplaceInfo> buildReplaceButtonDelegate()
   {
      return new Delegate<TransUnitReplaceInfo>() {

         @Override
         public void execute(TransUnitReplaceInfo tu)
         {
            fireReplaceTextEvent(Collections.singletonList(tu.getTransUnit()));
         }
      };
   }

   private Delegate<TransUnitReplaceInfo> buildUndoButtonDelegate()
   {
      return new Delegate<TransUnitReplaceInfo>() {

         @Override
         public void execute(TransUnitReplaceInfo info)
         {
            eventBus.fireEvent(new NotificationEvent(Severity.Error, "Undo not implemented"));
            // FIXME fire this when it exists
//            fireUndoUpdateEvent(Collections.singletonList(tu));
         }
      };
   }

   /**
    * @param toReplace list of TransUnits to replace
    */
   private void fireReplaceTextEvent(List<TransUnit> toReplace)
   {
      // TODO set info to replacing... before calling this event
      String searchText = currentHistoryState.getProjectSearchText();
      String replacement = currentHistoryState.getProjectSearchReplacement();
      boolean caseSensitive = currentHistoryState.getProjectSearchCaseSensitive();
      ReplaceText action = new ReplaceText(toReplace, searchText, replacement, caseSensitive);
      dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>()
      {

         @Override
         public void onFailure(Throwable e)
         {
            Log.error("[SearchResultsPresenter] Replace text failure " + e, e);
            // TODO use localised string
            eventBus.fireEvent(new NotificationEvent(Severity.Error, "Replace text failed"));
            // TODO consider whether possible/desired to change TU state from 'replacing'
         }

         @Override
         public void onSuccess(UpdateTransUnitResult result)
         {
            Set<Long> updatedDocs = new HashSet<Long>();
            for (TransUnitUpdateInfo updateInfo : result.getUpdateInfoList())
            {
               if (updateInfo.isSuccess())
               {
                  TransUnitReplaceInfo replaceInfo = getReplaceInfoForUpdatedTU(updateInfo);
                  if (replaceInfo != null)
                  {
                     replaceInfo.setReplaceInfo(updateInfo);
                     replaceInfo.setUndoable(true);
                     replaceInfo.setReplaceable(false);
                     replaceInfo.setReplacing(false);
                     //this should be done when the TU update event comes in anyway
                     //may want to remove this
                     replaceInfo.setTransUnit(updateInfo.getTransUnit());
                  }
                  updatedDocs.add(updateInfo.getDocumentId().getId());
               }
               // individual failure behaviour not yet defined
            }

            // force table refresh as property changes are not detected
            for (Long docId : updatedDocs)
            {
               ListDataProvider<TransUnitReplaceInfo> dataProvider = documentDataProviders.get(docId);
               if (dataProvider != null)
               {
                  dataProvider.refresh();
               }
            }

            // TODO use localised string
            eventBus.fireEvent(new NotificationEvent(Severity.Info, "Successfully replaced text"));
         }
      });
   }

   public class TransUnitReplaceInfo
   {
      private TransUnit tu;
      private TransUnitUpdateInfo replaceInfo = null;
      private boolean replaceable;
      private boolean replacing;
      private boolean undoable;

      public TransUnitReplaceInfo(TransUnit tu)
      {
         this.tu = tu;
         replaceInfo = null;
         replaceable = true;
         replacing = false;
         undoable = false;
      }

      public TransUnit getTransUnit()
      {
         return tu;
      }

      public void setTransUnit(TransUnit tu)
      {
         this.tu = tu;
      }

      public TransUnitUpdateInfo getReplaceInfo()
      {
         return replaceInfo;
      }

      public void setReplaceInfo(TransUnitUpdateInfo replaceInfo)
      {
         this.replaceInfo = replaceInfo;
      }

      public boolean isReplaceable()
      {
         return replaceable;
      }

      public void setReplaceable(boolean canReplace)
      {
         this.replaceable = canReplace;
      }

      public boolean isReplacing()
      {
         return replacing;
      }

      public void setReplacing(boolean isReplacing)
      {
         this.replacing = isReplacing;
      }

      public boolean isUndoable()
      {
         return undoable;
      }

      public void setUndoable(boolean undoable)
      {
         this.undoable = undoable;
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
    * @param updateInfo
    * @param replaceInfo
    * @return the replace info for the updated {@link TransUnit}, or null if the
    *         current search results do not contain the TransUnit
    */
   private TransUnitReplaceInfo getReplaceInfoForUpdatedTU(TransUnitUpdateInfo updateInfo)
   {
      ListDataProvider<TransUnitReplaceInfo> dataProvider = documentDataProviders.get(updateInfo.getDocumentId().getId());
      if (dataProvider == null)
      {
         Log.debug("document '" + updateInfo.getDocumentId().getId() + "' not found for TU update, id: " + updateInfo.getTransUnit().getId().getId());
         return null;
      }

      List<TransUnitReplaceInfo> replaceInfoList = dataProvider.getList();
      //TransUnit does not appear to have .equals(o), so list items are manually compared
      for (TransUnitReplaceInfo info : replaceInfoList)
      {
         if (info.getTransUnit().getId().getId() == updateInfo.getTransUnit().getId().getId())
         {
            return info;
         }
      }
      return null;
   }
}
