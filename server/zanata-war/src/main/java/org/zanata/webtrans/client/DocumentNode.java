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
import org.zanata.webtrans.shared.model.DocumentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class DocumentNode extends Composite
{

   private static DocumentNodeUiBinder uiBinder = GWT.create(DocumentNodeUiBinder.class);

   interface DocumentNodeUiBinder extends UiBinder<Widget, DocumentNode>
   {
   }

   @UiField(provided = true)
   TransUnitCountGraph transUnitCountGraph;

   final WebTransMessages messages;
   private final CachingDispatchAsync dispatcher;

   private ListDataProvider<DocumentNode> dataProvider;
   private DocumentInfo dataItem;

   public DocumentNode(WebTransMessages messages, DocumentInfo doc, CachingDispatchAsync dispatcher, EventBus eventBus, ListDataProvider<DocumentNode> dataProvider)
   {
      this.messages = messages;
      this.dispatcher = dispatcher;
      this.transUnitCountGraph = new TransUnitCountGraph(messages);
      this.dataProvider = dataProvider;

      initWidget(uiBinder.createAndBindUi(this));
      setDataItem(doc);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            if (event.getDocumentId().equals(getDataItem().getId()))
            {
               TransUnitCount unitCount = getStatusCount().getUnitCount();
               TransUnitWords wordCount = getStatusCount().getWordCount();
               unitCount.decrement(event.getPreviousStatus());
               unitCount.increment(event.getTransUnit().getStatus());
               wordCount.decrement(event.getPreviousStatus(), event.getWordCount());
               wordCount.increment(event.getTransUnit().getStatus(), event.getWordCount());
               updateGraphStatus();
            }
         }
      });
   }

   public DocumentInfo getDataItem()
   {
      return dataItem;
   }

   public void setDataItem(DocumentInfo dataItem)
   {
      this.dataItem = dataItem;
      updateGraphStatus();
   }

   public TransUnitCountGraph getTransUnitCountGraph()
   {
      return this.transUnitCountGraph;
   }

   private void updateGraphStatus()
   {
      getTransUnitCountGraph().setStats(getStatusCount());
      dataProvider.refresh();
   }

   private TranslationStats getStatusCount()
   {
      return dataItem.getStats();
   }

   public void setStats(TranslationStats stats)
   {
      getStatusCount().set(stats);
      updateGraphStatus();
   }
   
}
