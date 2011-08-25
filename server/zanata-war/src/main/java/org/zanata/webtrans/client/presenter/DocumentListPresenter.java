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
import java.util.HashMap;
import java.util.Map;

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
import org.zanata.webtrans.client.events.ProjectStatsRetrievedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;
import org.zanata.webtrans.shared.rpc.GetProjectStatusCount;
import org.zanata.webtrans.shared.rpc.GetProjectStatusCountResult;

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
   private final Map<DocumentId, DocumentStatus> statuscache = new HashMap<DocumentId, DocumentStatus>();
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

      // display.setProjectStatusBar(prStatusPresenter.getDisplay().asWidget());

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            DocumentStatus doc = statuscache.get(event.getDocumentId());
            if (doc == null)
               return; // GetProjectStatusCount hasn't returned yet!
               // ContentState status = event.getPreviousStatus();
               // doc.setStatus(status, doc.getStatus(status)-1);
               // status = event.getNewStatus();
               // doc.setStatus(status, doc.getStatus(status)+1);
               // TreeNode<DocName> node =
               // display.getTree().getNodeByKey(doc.getDocumentid());
               // node.setName(node.getObject().getName() + " ("+
               // calPercentage(doc.getUntranslated(), doc.getFuzzy(),
               // doc.getTranslated()) +"%)");
         }

      }));

      // registerHandler(getDisplay().getTree().addSelectionHandler(new
      // SelectionHandler<TreeItem>() {
      // @Override
      // public void onSelection(SelectionEvent<TreeItem> event) {
      // DocName selectedDocName = (DocName)
      // event.getSelectedItem().getUserObject();
      // if (selectedDocName != null) // folders have null names
      // setValue(selectedDocName.getId(), true);
      // }
      // }));
      //		
      // registerHandler(display.getReloadButton().addClickHandler(new
      // ClickHandler() {
      // @Override
      // public void onClick(ClickEvent event) {
      // refreshDisplay();
      // }
      // }));

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

      // TODO get rid of this
      // It is fetching stats for all documents in the workspace,
      // but then it adds them all up
      // and discards the individual document stats.
      // this can be discarded when project stats bar is removed from here.
      dispatcher.execute(new GetProjectStatusCount(), new AsyncCallback<GetProjectStatusCountResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
         }

         @Override
         public void onSuccess(GetProjectStatusCountResult result)
         {
            ArrayList<DocumentStatus> liststatus = result.getStatus();
            for (DocumentStatus doc : liststatus)
            {
               projectStats.add(doc.getCount());
            }
            display.getTransUnitCountBar().setStats(projectStats);
         }
      });

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

   public void setDocumentList(ArrayList<DocumentInfo> documents)
   {
      ArrayList<DocumentInfo> sortedList = new ArrayList<DocumentInfo>(documents);

      Collections.sort(sortedList, new Comparator<DocumentInfo>()
      {
         @Override
         public int compare(DocumentInfo o1, DocumentInfo o2)
         {
            String path1 = o1.getPath();
            if (path1 == null)
               path1 = "";
            String path2 = o2.getPath();
            if (path2 == null)
               path2 = "";
            int pathCompare = path1.compareTo(path2);
            if (pathCompare == 0)
               return o1.getName().compareTo(o2.getName());
            return pathCompare;
         }
      });
      display.setList(sortedList);
   }

   private void loadDocumentList()
   {
      loadDocsStatus();
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
            setDocumentList(documents);
         }
      });
   }

   private void loadDocsStatus()
   {
      dispatcher.execute(new GetProjectStatusCount(), new AsyncCallback<GetProjectStatusCountResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.info("load Doc Status failure " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetProjectStatusCountResult result)
         {
            ArrayList<DocumentStatus> listStatus = result.getStatus();
            Log.info("Received project status for " + listStatus.size() + " elements");
            statuscache.clear();
            for (DocumentStatus doc : listStatus)
            {
               statuscache.put(doc.getDocumentid(), doc);
               // TreeNode<DocName> node =
               // display.getTree().getNodeByKey(doc.getDocumentid());
               // node.setName(node.getObject().getName() + " ("+
               // calPercentage(doc.getUntranslated(), doc.getFuzzy(),
               // doc.getTranslated()) +"%)");
            }

            // re-use these stats for the project stats
            eventBus.fireEvent(new ProjectStatsRetrievedEvent(listStatus));
         }
      });
   }

}
