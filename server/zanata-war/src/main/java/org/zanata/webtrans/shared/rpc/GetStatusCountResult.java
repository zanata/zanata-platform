package org.zanata.webtrans.shared.rpc;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.shared.model.DocumentId;

import net.customware.gwt.dispatch.shared.Result;

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
