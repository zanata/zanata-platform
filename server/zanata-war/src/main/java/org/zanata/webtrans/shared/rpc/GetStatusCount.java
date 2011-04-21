package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.DocumentId;

public class GetStatusCount extends AbstractWorkspaceAction<GetStatusCountResult>
{
   private static final long serialVersionUID = 1L;
   private DocumentId documentId;

   @SuppressWarnings("unused")
   private GetStatusCount()
   {
   }

   public GetStatusCount(DocumentId id)
   {
      this.documentId = id;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

}
