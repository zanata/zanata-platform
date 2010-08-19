package net.openl10n.flies.webtrans.client;

import java.util.ArrayList;
import java.util.HashMap;

import net.openl10n.flies.webtrans.client.editor.HasTransUnitCount;
import net.openl10n.flies.webtrans.client.filter.ContentFilter;
import net.openl10n.flies.webtrans.client.ui.ClearableTextBox;
import net.openl10n.flies.webtrans.client.ui.UiMessages;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.DocumentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DocumentListView extends Composite implements DocumentListPresenter.Display, HasSelectionHandlers<DocumentInfo>
{

   private static DocumentListViewUiBinder uiBinder = GWT.create(DocumentListViewUiBinder.class);

   interface DocumentListViewUiBinder extends UiBinder<LayoutPanel, DocumentListView>
   {
   }

   @UiField(provided = true)
   final Resources resources;

   @UiField
   FlowPanel documentList;

   @UiField
   ScrollPanel documentScrollPanel;

   @UiField(provided = true)
   TransUnitCountBar transUnitCountBar;

   @UiField(provided = true)
   ClearableTextBox filterTextBox;

   private ContentFilter<DocumentInfo> filter;

   private DocumentNode currentSelection;

   private HashMap<DocumentId, DocumentNode> nodes;

   final WebTransMessages messages;

   @Inject
   public DocumentListView(Resources resources, WebTransMessages messages, UiMessages uiMessages)
   {
      this.resources = resources;
      this.messages = messages;
      filterTextBox = new ClearableTextBox(resources, uiMessages);
      nodes = new HashMap<DocumentId, DocumentNode>();
      transUnitCountBar = new TransUnitCountBar(messages);
      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void startProcessing()
   {
   }

   @Override
   public void stopProcessing()
   {
   }

   public void clear()
   {
      documentList.clear();
      nodes.clear();
   }

   public void add(FolderNode folderNode)
   {
      documentList.add(folderNode);
   }

   public void add(DocumentNode documentNode)
   {
      documentList.add(documentNode);
   }

   public int getChildCount()
   {
      return documentList.getWidgetCount();
   }

   public Node getChild(int index)
   {
      return (Node) documentList.getWidget(index);
   }

   @Override
   public void setList(ArrayList<DocumentInfo> sortedList)
   {
      clear();
      for (int i = 0; i < sortedList.size(); i++)
      {
         DocumentInfo doc = sortedList.get(i);
         DocumentNode node;
         if (doc.getPath() == null || doc.getPath().isEmpty())
         {
            node = new DocumentNode(resources, messages, doc, documentNodeClickHandler);
            add(node);
         }
         else
         {
            FolderNode folder = new FolderNode(resources, doc);
            node = new DocumentNode(resources, messages, doc, documentNodeClickHandler);
            folder.addChild(node);
            add(folder);
         }
         nodes.put(doc.getId(), node);
         if (filter != null)
         {
            node.setVisible(filter.accept(doc));
         }
      }
   }

   /**
    * Common click-handler for all 'translate' links
    */
   private final ClickHandler documentNodeClickHandler = new ClickHandler()
   {
      @Override
      public void onClick(ClickEvent event)
      {
         DocumentNode node = (DocumentNode) event.getSource();
         SelectionEvent.fire(DocumentListView.this, node.getDataItem());
      }
   };

   @Override
   public void clearSelection()
   {
      if (currentSelection == null)
      {
         return;
      }
      currentSelection.setSelected(false);
      currentSelection = null;
   }

   @Override
   public void setSelection(final DocumentInfo document)
   {
      if (currentSelection != null && currentSelection.getDataItem() == document)
      {
         return;
      }
      clearSelection();
      DocumentNode node = nodes.get(document.getId());
      if (node != null)
      {
         node.setSelected(true);
         currentSelection = node;
      }
   }

   @Override
   public void ensureSelectionVisible()
   {
      if (currentSelection != null)
         documentScrollPanel.ensureVisible(currentSelection);
   }

   @Override
   public void setFilter(ContentFilter<DocumentInfo> filter)
   {
      this.filter = filter;
      for (DocumentNode docNode : nodes.values())
      {
         docNode.setVisible(filter.accept(docNode.getDataItem()));
      }
   }

   @Override
   public void removeFilter()
   {
      for (DocumentNode docNode : nodes.values())
      {
         docNode.setVisible(true);
      }
   }

   @Override
   public HasValue<String> getFilterTextBox()
   {
      return filterTextBox.getTextBox();
   }

   @Override
   public HasTransUnitCount getTransUnitCountBar()
   {
      return transUnitCountBar;
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

}
