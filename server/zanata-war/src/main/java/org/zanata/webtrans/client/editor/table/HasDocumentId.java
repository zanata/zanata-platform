package org.zanata.webtrans.client.editor.table;

import org.zanata.webtrans.shared.model.DocumentId;

public interface HasDocumentId
{
   public void setDocumentId(DocumentId documentId);

   public DocumentId getDocumentId();
}
