package net.openl10n.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.common.TranslationStats;
import net.openl10n.flies.webtrans.shared.model.DocumentId;

public class GetStatusCountResult implements Result
{
   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private TranslationStats stats;

   @SuppressWarnings("unused")
   private GetStatusCountResult()
   {
   }

   public GetStatusCountResult(DocumentId documentId, TranslationStats stats)
   {
      this.documentId = documentId;
      this.stats = stats;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public TranslationStats getCount()
   {
      return stats;
   }

}
