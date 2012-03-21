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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;

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

public class SearchResultsPresenter extends WidgetPresenter<SearchResultsPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      HasText getTestLabel();

      HasValue<String> getFilterTextBox();

      void clearAll();

      HasClickHandlers addDocumentLabel(String docName);

      HasData<TransUnit> addTUTable();
   }

   private final CachingDispatchAsync dispatcher;
   private final History history;

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

      history.addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            if (currentHistoryState == null)
               currentHistoryState = new HistoryToken(); // default values

            HistoryToken token = HistoryToken.fromTokenString(event.getValue());

            if (!token.getProjectSearchText().equals(currentHistoryState.getProjectSearchText()))
            {
               dispatcher.execute(new GetProjectTransUnitLists(token.getProjectSearchText()), new AsyncCallback<GetProjectTransUnitListsResult>() {

                  @Override
                  public void onFailure(Throwable caught)
                  {
                     display.clearAll();
                     display.getTestLabel().setText("Project TU search failed");
                  }

                  @Override
                  public void onSuccess(GetProjectTransUnitListsResult result)
                  {
                     display.getTestLabel().setText("Project TU search returned documents: " + result.getDocumentPaths().size());

                     display.clearAll();
                     for (final String doc : result.getDocumentPaths())
                     {
                        HasClickHandlers docLabel = display.addDocumentLabel(doc);
                        docLabel.addClickHandler(new ClickHandler()
                        {

                           @Override
                           public void onClick(ClickEvent event)
                           {
                              showDocInEditor(doc);
                           }

                        });
                        HasData<TransUnit> table = display.addTUTable();
                        ListDataProvider<TransUnit> dataProvider = new ListDataProvider<TransUnit>();
                        dataProvider.addDataDisplay(table);
                        //link dataProvider BEFORE adding units, so they display automatically
                        dataProvider.getList().addAll(result.getUnits(doc));
                     }
                  }

               });

               //TODO set filter text box to value from history token.
               //watch out for greying-out issue with this type of text box
            }

            currentHistoryState = token;
         }
      });

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
      // Auto-generated method stub
   }

}
