package org.zanata.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DocumentTreeItemView extends Composite
{

   private static DocumentTreeItemViewUiBinder uiBinder = GWT.create(DocumentTreeItemViewUiBinder.class);

   interface DocumentTreeItemViewUiBinder extends UiBinder<Widget, DocumentTreeItemView>
   {
   }

   @UiField
   Label documentName;

   public DocumentTreeItemView(String documentName)
   {
      initWidget(uiBinder.createAndBindUi(this));
      setName(documentName);
   }

   public void setName(String name)
   {
      this.documentName.setText(name);
   }

}
