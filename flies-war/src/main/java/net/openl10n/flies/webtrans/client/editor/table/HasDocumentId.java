package net.openl10n.flies.webtrans.client.editor.table;

import net.openl10n.flies.webtrans.shared.model.DocumentId;

public interface HasDocumentId
{
   public void setDocumentId(DocumentId documentId);

   public DocumentId getDocumentId();
}
