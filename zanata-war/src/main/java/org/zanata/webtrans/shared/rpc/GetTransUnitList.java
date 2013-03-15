package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.base.Objects;

public class GetTransUnitList extends AbstractWorkspaceAction<GetTransUnitListResult>
{
   private static final long serialVersionUID = 1L;
   private int offset;
   private int count;
   private DocumentId documentId;
   private String phrase;
   private boolean filterTranslated, filterNeedReview, filterUntranslated, filterHasError;
   private List<ValidationId> validationIds;
   private TransUnitId targetTransUnitId;
   private boolean needReloadIndex = false;

   @SuppressWarnings("unused")
   private GetTransUnitList()
   {
   }

   private GetTransUnitList(DocumentId id, int offset, int count, String phrase, boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated, boolean filterHasError, TransUnitId targetTransUnitId, List<ValidationId> validationIds)
   {
      this.documentId = id;
      this.offset = offset;
      this.count = count;
      this.phrase = phrase;
      this.filterTranslated = filterTranslated;
      this.filterNeedReview = filterNeedReview;
      this.filterUntranslated = filterUntranslated;
      this.filterHasError = filterHasError;
      this.targetTransUnitId = targetTransUnitId;
      this.validationIds = validationIds;

   }

   public static GetTransUnitList newAction(GetTransUnitActionContext context)
   {
      return new GetTransUnitList(context.getDocumentId(), context.getOffset(), context.getCount(), context.getFindMessage(), context.isFilterTranslated(), context.isFilterNeedReview(), context.isFilterUntranslated(), context.isFilterHasError(), context.getTargetTransUnitId(), context.getValidationIds());
   }

   public boolean isNeedReloadIndex()
   {
      return needReloadIndex;
   }

   public GetTransUnitList setNeedReloadIndex(boolean needReloadIndex)
   {
      this.needReloadIndex = needReloadIndex;
      return this;
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

   public boolean isFilterHasError()
   {
      return filterHasError;
   }

   public TransUnitId getTargetTransUnitId()
   {
      return targetTransUnitId;
   }

   public List<ValidationId> getValidationIds()
   {
      return validationIds;
   }

   public boolean isAcceptAllStatus()
   {
      //all filter options are checked or unchecked
      return filterNeedReview == filterTranslated && filterNeedReview == filterUntranslated;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("offset", offset).
            add("count", count).
            add("documentId", documentId).
            add("phrase", phrase).
            add("filterTranslated", filterTranslated).
            add("filterNeedReview", filterNeedReview).
            add("filterUntranslated", filterUntranslated).
            add("filterHasError", filterHasError).
            add("targetTransUnitId", targetTransUnitId).
            add("needReloadIndex", needReloadIndex).
            toString();
      // @formatter:on
   }
}
