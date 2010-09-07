package net.openl10n.flies.webtrans.client;

import net.openl10n.flies.webtrans.shared.model.DocumentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FolderNode extends Node<DocumentInfo>
{

   private static FolderNodeUiBinder uiBinder = GWT.create(FolderNodeUiBinder.class);

   interface FolderNodeUiBinder extends UiBinder<Widget, FolderNode>
   {
   }

   @UiField
   Label folderLabel;

   @UiField
   FlowPanel childrenContainer;

   @UiField(provided = true)
   final Resources resources;

   public FolderNode(Resources resources)
   {
      this.resources = resources;
      initWidget(uiBinder.createAndBindUi(this));
   }

   public FolderNode(Resources resources, DocumentInfo doc)
   {
      this(resources);
      setDataItem(doc);
   }

   public void addChild(DocumentNode node)
   {
      childrenContainer.add(node);
   }

   public int getChildCount()
   {
      return childrenContainer.getWidgetCount();
   }

   public void clear()
   {
      childrenContainer.clear();
   }

   public DocumentNode getChild(int index)
   {
      return (DocumentNode) childrenContainer.getWidget(index);
   }

   @Override
   boolean isDocument()
   {
      return false;
   }

   @Override
   void refresh()
   {
      folderLabel.setText(getDataItem().getPath());
   }

}
