package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;

public class GetTransUnitList extends AbstractWorkspaceAction<GetTransUnitListResult>
{
   private static final long serialVersionUID = 1L;
   private int offset;
   private int count;
   private DocumentId documentId;
   private String phrase;
   private boolean filterTranslated, filterNeedReview, filterUntranslated;
   private TransUnitId targetTransUnitId;

   @SuppressWarnings("unused")
   private GetTransUnitList()
   {
   }

   public GetTransUnitList(DocumentId id, int offset, int count, String phrase, boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated, TransUnitId targetTransUnitId)
   {
      this.documentId = id;
      this.offset = offset;
      this.count = count;
      this.phrase = phrase;
      this.filterTranslated = filterTranslated;
      this.filterNeedReview = filterNeedReview;
      this.filterUntranslated = filterUntranslated;
      this.targetTransUnitId = targetTransUnitId;

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

   public boolean isFilterTranslated()
   {
      return filterTranslated;
   }

   public boolean isFilterNeedReview()
   {
      return filterNeedReview;
   }

   public boolean isFilterUntranslated()
   {
      return filterUntranslated;
   }

   public TransUnitId getTargetTransUnitId()
   {
      return targetTransUnitId;
   }

}
