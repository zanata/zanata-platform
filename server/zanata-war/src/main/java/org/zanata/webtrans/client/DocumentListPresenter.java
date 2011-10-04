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
package org.zanata.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.editor.HasTranslationStats;
import org.zanata.webtrans.client.editor.filter.ContentFilter;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListPresenter.Display> implements HasDocumentSelectionHandlers
{

   public interface Display extends WidgetDisplay
   {
      void setList(ArrayList<DocumentInfo> sortedList);

      void clearSelection();

      void setSelection(DocumentInfo document);

      void ensureSelectionVisible();

      void setFilter(ContentFilter<DocumentInfo> filter);

      void removeFilter();

      HasValue<String> getFilterTextBox();

      HasTranslationStats getTransUnitCountBar();

      HasSelectionHandlers<DocumentInfo> getDocumentList();
   }

   private final DispatchAsync dispatcher;
   private final WorkspaceContext workspaceContext;
   private DocumentInfo currentDocument;
   private final TranslationStats projectStats = new TranslationStats();

   private final WebTransMessages messages;

   @Inject
   public DocumentListPresenter(Display display, EventBus eventBus, WorkspaceContext workspaceContext, CachingDispatchAsync dispatcher, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.workspaceContext = workspaceContext;
      this.dispatcher = dispatcher;
      this.messages = messages;
      Log.info("DocumentListPresenter()");
      loadDocumentList();
   }

   @Override
   protected void onBind()
   {

      registerHandler(display.getDocumentList().addSelectionHandler(new SelectionHandler<DocumentInfo>()
      {
         @Override
         public void onSelection(SelectionEvent<DocumentInfo> event)
         {
            currentDocument = event.getSelectedItem();
            display.setSelection(currentDocument);
            fireEvent(new DocumentSelectionEvent(currentDocument));
         }
      }));

      registerHandler(display.getFilterTextBox().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            if (event.getValue().isEmpty())
            {
               display.removeFilter();
            }
            else
            {
               basicContentFilter.setPattern(event.getValue());
               display.setFilter(basicContentFilter);
            }
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            TransUnitCount projectCount = projectStats.getUnitCount();
            projectCount.decrement(event.getPreviousStatus());
            projectCount.increment(event.getTransUnit().getStatus());
            TransUnitWords projectWords = projectStats.getWordCount();
            projectWords.decrement(event.getPreviousStatus(), event.getWordCount());
            projectWords.increment(event.getTransUnit().getStatus(), event.getWordCount());
            getDisplay().getTransUnitCountBar().setStats(projectStats);
         }
      }));

   }

   final class BasicContentFilter implements ContentFilter<DocumentInfo>
   {
      private String pattern = "";

      @Override
      public boolean accept(DocumentInfo value)
      {
         return value.getName().contains(pattern);
      }

      public void setPattern(String pattern)
      {
         this.pattern = pattern;
      }
   }

   private final BasicContentFilter basicContentFilter = new BasicContentFilter();

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
            final ArrayList<DocumentInfo> documents = result.getDocuments();
            Log.info("Received doc list for " + result.getProjectIterationId() + ": " + documents.size() + " elements");
            display.setList(documents); // TODO server should sort
            for (DocumentInfo doc : documents)
            {
               projectStats.add(doc.getStats());
            }
            display.getTransUnitCountBar().setStats(projectStats);
         }
      });
   }

}
