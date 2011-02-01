package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.webtrans.shared.model.DocumentId;

public class GetTransUnitList extends AbstractWorkspaceAction<GetTransUnitListResult>
{

   private static final long serialVersionUID = 1L;
   private int offset;
   private int count;
   private DocumentId documentId;
   private String phrase;

   @SuppressWarnings("unused")
   private GetTransUnitList()
   {
   }

   public GetTransUnitList(DocumentId id, int offset, int count, String phrase)
   {
      this.documentId = id;
      this.offset = offset;
      this.count = count;
      this.phrase = phrase;
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

   public String getPhrase()
   {
      return this.phrase;
   }

}
