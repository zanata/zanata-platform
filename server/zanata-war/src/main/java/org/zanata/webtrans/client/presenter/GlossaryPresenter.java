/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class GlossaryPresenter extends WidgetPresenter<GlossaryPresenter.Display>
{
   private final WorkspaceContext workspaceContext;
   private final CachingDispatchAsync dispatcher;
   private final GlossaryDetailsPresenter glossaryDetailsPresenter;
   private GetGlossary submittedRequest = null;
   private GetGlossary lastRequest = null;
   private ListDataProvider<TranslationMemoryGlossaryItem> dataProvider;

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getSearchButton();

      HasText getGlossaryTextBox();

      HasValue<SearchType> getSearchType();

      void startProcessing();

      boolean isFocused();

      Column<TranslationMemoryGlossaryItem, String> getCopyColumn();

      Column<TranslationMemoryGlossaryItem, ImageResource> getDetailsColumn();

      void setDataProvider(ListDataProvider<TranslationMemoryGlossaryItem> dataProvider);

      void setPageSize(int size);
   }

   @Inject
   public GlossaryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, GlossaryDetailsPresenter glossaryDetailsPresenter, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.workspaceContext = workspaceContext;
      this.glossaryDetailsPresenter = glossaryDetailsPresenter;
      dataProvider = new ListDataProvider<TranslationMemoryGlossaryItem>();
      display.setDataProvider(dataProvider);
   }

   @Override
   protected void onBind()
   {
      display.getSearchButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            String query = display.getGlossaryTextBox().getText();
            createGlossaryRequest(query, display.getSearchType().getValue());
         }
      });

      registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), new TransUnitSelectionHandler()
      {
         @Override
         public void onTransUnitSelected(TransUnitSelectionEvent event)
         {
            createGlossaryRequestForTransUnit(event.getSelection());
         }
      }));

      display.getCopyColumn().setFieldUpdater(new FieldUpdater<TranslationMemoryGlossaryItem, String>()
      {
         @Override
         public void update(int index, TranslationMemoryGlossaryItem object, String value)
         {
            eventBus.fireEvent(new InsertStringInEditorEvent(object.getSource(), object.getTarget()));
         }
      });

      display.getDetailsColumn().setFieldUpdater(new FieldUpdater<TranslationMemoryGlossaryItem, ImageResource>()
      {
         @Override
         public void update(int index, TranslationMemoryGlossaryItem object, ImageResource value)
         {
            glossaryDetailsPresenter.show(object);
         }
      });
   }

   private void createGlossaryRequest(final String query, GetGlossary.SearchType searchType)
   {
      display.startProcessing();
      final GetGlossary action = new GetGlossary(query, workspaceContext.getWorkspaceId().getLocaleId(), searchType);
      scheduleGlossaryRequest(action);
   }

   public void createGlossaryRequestForTransUnit(TransUnit transUnit)
   {
      String query = transUnit.getSource();
      SearchType searchType = GetGlossary.SearchType.FUZZY;
      display.getGlossaryTextBox().setText("");
      createGlossaryRequest(query, searchType);
   }

   private void scheduleGlossaryRequest(GetGlossary action)
   {
      lastRequest = action;
      if (submittedRequest == null)
      {
         submitGlossaryRequest(action);
      }
      else
      {
         Log.debug("blocking glossary request until outstanding request returns");
      }
   }

   private void submitGlossaryRequest(GetGlossary action)
   {
      Log.debug("submitting glossary request");
      dispatcher.execute(action, new AsyncCallback<GetGlossaryResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error(caught.getMessage(), caught);
            submittedRequest = null;
         }

         @Override
         public void onSuccess(GetGlossaryResult result)
         {
            if (result.getRequest().equals(lastRequest))
            {
               Log.debug("received glossary result for query");
               displayGlossaryResult(result);
               lastRequest = null;
            }
            else
            {
               Log.debug("ignoring old glossary result for query");
            }
            submittedRequest = null;
            if (lastRequest != null)
            {
               // submit the waiting request
               submitGlossaryRequest(lastRequest);
            }
         }
      });
      submittedRequest = action;
   }

   private void displayGlossaryResult(GetGlossaryResult result)
   {
      String query = submittedRequest.getQuery();
      display.getGlossaryTextBox().setText(query);
      display.getSearchType().setValue(submittedRequest.getSearchType());

      dataProvider.getList().clear();
      for (final TranslationMemoryGlossaryItem glossary : result.getGlossaries())
      {
         dataProvider.getList().add(glossary);
      }
      display.setPageSize(dataProvider.getList().size());
      dataProvider.refresh();
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }
}
