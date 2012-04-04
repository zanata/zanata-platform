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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
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

      void clearAll();

      HasClickHandlers addDocumentLabel(String docName);

      HasData<TransUnit> addTUTable(Delegate<TransUnit> replaceDelegate);
   }

   private final CachingDispatchAsync dispatcher;
   private final History history;
   private AsyncCallback<GetProjectTransUnitListsResult> projectSearchCallback;
   private Delegate<TransUnit> replaceButtonDelegate;

   /**
    * Model objects for tables in display. Changes to these are reflected in the
    * view.
    */
   private Map<Long, ListDataProvider<TransUnit>> documentDataProviders;

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
      documentDataProviders = new HashMap<Long, ListDataProvider<TransUnit>>();

      registerHandler(display.getFilterTextBox().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            HistoryToken token = HistoryToken.fromTokenString(history.getToken());
            if (event.getValue() != token.getProjectSearchText())
            {
               token.setProjectSearchText(event.getValue());
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
            if (caseSensitivityChanged ||  searchTextChanged)
            {
               display.setHighlightString(token.getProjectSearchText());
               display.getFilterTextBox().setValue(token.getProjectSearchText(), true);
               display.getCaseSensitiveChk().setValue(token.getProjectSearchCaseSensitive(), false);

               documentDataProviders.clear();
               display.clearAll();

               if (!token.getProjectSearchText().isEmpty())
               {
                  //TODO show loading indicator
                  dispatcher.execute(new GetProjectTransUnitLists(token.getProjectSearchText(), token.getProjectSearchCaseSensitive()), projectSearchCallback);
               }
            }

            currentHistoryState = token;
         }
      });

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {

         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            ListDataProvider<TransUnit> dataProvider = documentDataProviders.get(event.getDocumentId().getId());
            if (dataProvider == null)
            {
               return;
            }

            List<TransUnit> transUnits = dataProvider.getList();
            //TransUnit does not appear to have .equals(o), so list items are manually compared
            for (int i = 0; i < transUnits.size(); i++)
            {
               if (transUnits.get(i).getId().getId() == event.getTransUnit().getId().getId())
               {
                  transUnits.set(i, event.getTransUnit());
                  return;
               }
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

            documentDataProviders.clear();
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

               HasData<TransUnit> table = display.addTUTable(replaceButtonDelegate);
               ListDataProvider<TransUnit> dataProvider = new ListDataProvider<TransUnit>();
               dataProvider.addDataDisplay(table);
               List<TransUnit> data = dataProvider.getList();
               data.addAll(result.getUnits(docId));
               Collections.sort(data, TransUnit.getRowIndexComparator());
               documentDataProviders.put(docId, dataProvider);
            }
         }

      };
   }

   private Delegate<TransUnit> buildReplaceButtonDelegate()
   {
      return new Delegate<TransUnit>() {

         @Override
         public void execute(TransUnit tu)
         {
            String target = tu.getTarget();
            target = target.replace(currentHistoryState.getProjectSearchText(), display.getReplacementTextBox().getValue());
            final UpdateTransUnit updateTransUnit = new UpdateTransUnit(tu.getId(), target, tu.getStatus());
            dispatcher.execute(updateTransUnit, new AsyncCallback<UpdateTransUnitResult>()
            {
               @Override
               public void onFailure(Throwable e)
               {
                  Log.error("[SearchResultsPresenter] Replace text failure " + e, e);
                  // TODO use localised string
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, "Replace text failed"));
               }

               @Override
               public void onSuccess(UpdateTransUnitResult result)
               {
                  eventBus.fireEvent(new NotificationEvent(Severity.Info, "Successfully replaced text"));
                  // not sure if any undoable TU action is
                  // required here
                  //TODO show replacement in relevant table
               }
            });
         }
      };
   }

}
