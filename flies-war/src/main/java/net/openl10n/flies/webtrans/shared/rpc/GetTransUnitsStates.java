package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.common.NavigationType;
import net.openl10n.flies.webtrans.shared.model.DocumentId;


public class GetTransUnitsStates extends AbstractWorkspaceAction<GetTransUnitsStatesResult>
{

   private static final long serialVersionUID = 1L;

   private int offset;
   private int count;
   private DocumentId documentId;
   private NavigationType state;
   private boolean reverse;

   @SuppressWarnings("unused")
   private GetTransUnitsStates()
   {
   }

   public GetTransUnitsStates(DocumentId id, int offset, int count, boolean reverse, NavigationType state)
   {
      this.documentId = id;
      this.offset = offset;
      this.count = count;
      this.state = state;
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

   public void setState(NavigationType state)
   {
      this.state = state;
   }

   public NavigationType getState()
   {
      return state;
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