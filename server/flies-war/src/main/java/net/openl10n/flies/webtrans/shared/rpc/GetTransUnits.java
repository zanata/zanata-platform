package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.webtrans.shared.model.DocumentId;

public class GetTransUnits extends AbstractWorkspaceAction<GetTransUnitsResult>
{

   private static final long serialVersionUID = 1L;
   private int offset;
   private int count;
   private DocumentId documentId;

   @SuppressWarnings("unused")
   private GetTransUnits()
   {
   }

   public GetTransUnits(DocumentId id, int offset, int count)
   {
      this.documentId = id;
      this.offset = offset;
      this.count = count;
   }

   public int getOffset()
   {
      return offset;
   }

   public int getCount()
   {
      return count;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

}
