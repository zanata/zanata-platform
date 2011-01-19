package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.webtrans.shared.model.DocumentId;


public class GetTransUnitsNavigation extends AbstractWorkspaceAction<GetTransUnitsNavigationResult>
{

   private static final long serialVersionUID = 1L;

   private int offset;
   private int count;
   private DocumentId documentId;
   private boolean reverse;

   @SuppressWarnings("unused")
   private GetTransUnitsNavigation()
   {
   }

   public GetTransUnitsNavigation(DocumentId id, int offset, int count, boolean reverse)
   {
      this.documentId = id;
      this.offset = offset;
      this.count = count;
      this.setReverse(reverse);
   }

   public int getOffset()
   {
      return offset;
   }

   public void setOffset(int offset)
   {
      this.offset = offset;
   }

   public int getCount()
   {
      return count;
   }

   public void setCount(int count)
   {
      this.count = count;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public void setDocumentId(DocumentId documentId)
   {
      this.documentId = documentId;
   }

   public void setReverse(boolean reverse)
   {
      this.reverse = reverse;
   }

   public boolean isReverse()
   {
      return reverse;
   }
}