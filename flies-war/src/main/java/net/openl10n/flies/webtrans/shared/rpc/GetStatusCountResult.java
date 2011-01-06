package net.openl10n.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.common.TransUnitCount;
import net.openl10n.flies.common.TransUnitWords;
import net.openl10n.flies.webtrans.shared.model.DocumentId;


public class GetStatusCountResult implements Result
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private TransUnitCount count;
   private TransUnitWords wordCount;

   @SuppressWarnings("unused")
   private GetStatusCountResult()
   {
   }

   public GetStatusCountResult(DocumentId documentId, TransUnitCount count, TransUnitWords wordCount)
   {
      this.documentId = documentId;
      this.count = count;
      this.wordCount = wordCount;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public TransUnitCount getCount()
   {
      return count;
   }

   public TransUnitWords getWordCount()
   {
      return wordCount;
   }

}
