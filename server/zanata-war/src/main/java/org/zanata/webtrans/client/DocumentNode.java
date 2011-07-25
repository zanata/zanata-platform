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

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class DocumentNode extends Node<DocumentInfo>
{

   private static DocumentNodeUiBinder uiBinder = GWT.create(DocumentNodeUiBinder.class);

   interface DocumentNodeUiBinder extends UiBinder<Widget, DocumentNode>
   {
   }

   @UiField(provided = true)
   TransUnitCountGraph transUnitCountGraph;

   final WebTransMessages messages;
   private final TranslationStats statusCount = new TranslationStats();
   private final CachingDispatchAsync dispatcher;

   private ListDataProvider<DocumentNode> dataProvider;

   public DocumentNode(WebTransMessages messages, CachingDispatchAsync dispatcher, ListDataProvider<DocumentNode> dataProvider)
   {
      this.messages = messages;
      this.dispatcher = dispatcher;
      this.transUnitCountGraph = new TransUnitCountGraph(messages);
      this.dataProvider = dataProvider;

      initWidget(uiBinder.createAndBindUi(this));
   }

   public DocumentNode(WebTransMessages messages, DocumentInfo doc, CachingDispatchAsync dispatcher, ListDataProvider<DocumentNode> dataProvider)
   {
      this(messages, dispatcher, dataProvider);
      setDataItem(doc);
   }

   public DocumentNode(WebTransMessages messages, DocumentInfo doc, CachingDispatchAsync dispatcher, EventBus eventBus, ListDataProvider<DocumentNode> dataProvider)
   {
      this(messages, doc, dispatcher, dataProvider);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            if (event.getDocumentId().equals(getDataItem().getId()))
            {
               TransUnitCount unitCount = statusCount.getUnitCount();
               TransUnitWords wordCount = statusCount.getWordCount();
               unitCount.decrement(event.getPreviousStatus());
               unitCount.increment(event.getTransUnit().getStatus());
               wordCount.decrement(event.getPreviousStatus(), event.getWordCount());
               wordCount.increment(event.getTransUnit().getStatus(), event.getWordCount());
               updateGraphStatus();
            }
         }
      });
   }

   public void refresh()
   {
      requestStatusCount(getDataItem().getId());
   }

   @Override
   boolean isDocument()
   {
      return true;
   }

   public TransUnitCountGraph getTransUnitCountGraph()
   {
      return this.transUnitCountGraph;
   }

   private void updateGraphStatus()
   {
      getTransUnitCountGraph().setStats(statusCount);
      dataProvider.refresh();
   }

   private void requestStatusCount(final DocumentId newDocumentId)
   {
      dispatcher.execute(new GetStatusCount(newDocumentId), new AsyncCallback<GetStatusCountResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("error fetching GetStatusCount: " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetStatusCountResult result)
         {
            statusCount.set(result.getCount());
            updateGraphStatus();
         }
      });
   }
   
}
