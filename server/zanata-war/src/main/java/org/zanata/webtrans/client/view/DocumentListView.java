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
package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.HashMap;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.filter.ContentFilter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.ClearableTextBox;
import org.zanata.webtrans.client.ui.DocumentListTable;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class DocumentListView extends Composite implements DocumentListPresenter.Display, HasSelectionHandlers<DocumentInfo>
{

   private static DocumentListViewUiBinder uiBinder = GWT.create(DocumentListViewUiBinder.class);

   interface DocumentListViewUiBinder extends UiBinder<LayoutPanel, DocumentListView>
   {
   }

   @UiField
   ScrollPanel documentScrollPanel;

   @UiField(provided = true)
   ClearableTextBox filterTextBox;

   @UiField(provided = true)
   final CellTable<DocumentNode> documentListTable;

   final Resources resources;

   private ListDataProvider<DocumentNode> dataProvider;

   private ContentFilter<DocumentInfo> filter;

   private DocumentNode currentSelection;

   private HashMap<DocumentId, DocumentNode> nodes;

   final WebTransMessages messages;

   final CachingDispatchAsync dispatcher;

   final EventBus eventBus;

   @Inject
   public DocumentListView(Resources resources, WebTransMessages messages, UiMessages uiMessages, final CachingDispatchAsync dispatcher, EventBus eventBus)
   {
      this.resources = resources;
      this.messages = messages;
      this.dispatcher = dispatcher;
      this.eventBus = eventBus;

      filterTextBox = new ClearableTextBox(resources, uiMessages);
      nodes = new HashMap<DocumentId, DocumentNode>();
      dataProvider = new ListDataProvider<DocumentNode>();
      documentListTable = DocumentListTable.initDocumentListTable(this, resources, messages, dataProvider);
      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setList(ArrayList<DocumentInfo> sortedList)
   {
      dataProvider.getList().clear();
      nodes = new HashMap<DocumentId, DocumentNode>(sortedList.size());
      int counter = 0;
      long start = System.currentTimeMillis();
      for (DocumentInfo doc : sortedList)
      {
         Log.info("Loading document: " + ++counter + " ");
         DocumentNode node = new DocumentNode(messages, doc, eventBus, dataProvider);
         if (filter != null)
         {
            node.setVisible(filter.accept(doc));
         }
         if (node.isVisible())
         {
            dataProvider.getList().add(node);
         }
         nodes.put(doc.getId(), node);
      }
      Log.info("Time to create DocumentNodes: " + String.valueOf(System.currentTimeMillis() - start) + "ms");
      documentListTable.setPageSize(dataProvider.getList().size());
      dataProvider.addDataDisplay(documentListTable);
   }

   @Override
   public TransUnitUpdatedEventHandler getDocumentNode(DocumentId docId)
   {
      return nodes.get(docId);
   }

   @Override
   public void clearSelection()
   {
      if (currentSelection == null)
      {
         return;
      }
      currentSelection = null;
   }

   @Override
   public void setSelection(final DocumentId documentId)
   {
      if (currentSelection != null && currentSelection.getDocInfo().getId() == documentId)
      {
         return;
      }
      clearSelection();
      DocumentNode node = nodes.get(documentId);
      if (node != null)
      {
         currentSelection = node;
         // required to have document selected in doclist when loading from
         // bookmarked history token
         documentListTable.getSelectionModel().setSelected(node, true);
      }
   }

   @Override
   public void setFilter(ContentFilter<DocumentInfo> filter)
   {
      this.filter = filter;
      dataProvider.getList().clear();
      for (DocumentNode docNode : nodes.values())
      {
         docNode.setVisible(filter.accept(docNode.getDocInfo()));
         if (docNode.isVisible())
         {
            dataProvider.getList().add(docNode);
         }
      }
      dataProvider.refresh();
   }

   @Override
   public void removeFilter()
   {
      dataProvider.getList().clear();
      for (DocumentNode docNode : nodes.values())
      {
         docNode.setVisible(true);
         dataProvider.getList().add(docNode);
      }
      dataProvider.refresh();
   }

   @Override
   public HasValue<String> getFilterTextBox()
   {
      return filterTextBox.getTextBox();
   }

   @Override
   public HasSelectionHandlers<DocumentInfo> getDocumentList()
   {
      return this;
   }

   @Override
   public HandlerRegistration addSelectionHandler(SelectionHandler<DocumentInfo> handler)
   {
      return addHandler(handler, SelectionEvent.getType());
   }

   @Override
   public DocumentInfo getDocumentInfo(DocumentId docId)
   {
      DocumentNode node = nodes.get(docId);
      return (node == null ? null : node.getDocInfo());
   }
}