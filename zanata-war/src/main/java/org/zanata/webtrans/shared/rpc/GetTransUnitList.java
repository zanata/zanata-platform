package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.model.ContentStateGroup;
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
   private ContentStateGroup filterStates;
   private boolean filterHasError;
   private List<ValidationId> validationIds;
   private TransUnitId targetTransUnitId;
   private boolean needReloadIndex = false;

   private GetTransUnitList()
   {
   }

   private GetTransUnitList(GetTransUnitActionContext context)
   {
      documentId = context.getDocument().getId();
      offset = context.getOffset();
      count = context.getCount();
      phrase = context.getFindMessage();
      // @formatter :off
      filterStates = ContentStateGroup.builder()
            .includeNew(context.isFilterUntranslated())
            .includeFuzzy(context.isFilterNeedReview())
            .includeTranslated(context.isFilterTranslated())
            .includeApproved(context.isFilterApproved())
            .includeRejected(context.isFilterRejected())
            .build();
      // @formatter :on
      filterHasError = context.isFilterHasError();
      targetTransUnitId = context.getTargetTransUnitId();
      validationIds = context.getValidationIds();
   }

   public static GetTransUnitList newAction(GetTransUnitActionContext context)
   {
      return new GetTransUnitList(context);
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

   public ContentStateGroup getFilterStates()
   {
      return filterStates;
   }

   public boolean isFilterTranslated()
   {
      return filterStates.hasTranslated();
   }

   public boolean isFilterNeedReview()
   {
      return filterStates.hasFuzzy();
   }

   public boolean isFilterUntranslated()
   {
      return filterStates.hasNew();
   }
   
   public boolean isFilterApproved()
   {
      return filterStates.hasApproved();
   }
   
   public boolean isFilterRejected()
   {
      return filterStates.hasRejected();
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
      return filterStates.hasNoStates() && !filterHasError || filterStates.hasAllStates() && filterHasError;
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
            add("filterStates", filterStates).
            add("filterHasError", filterHasError).
            add("targetTransUnitId", targetTransUnitId).
            add("needReloadIndex", needReloadIndex).
            toString();
      // @formatter:on
   }
}
