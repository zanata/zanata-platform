package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitValidationResult;

import com.google.common.base.Objects;


public class GetTransUnitListResult implements DispatchResult
{
   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private List<TransUnit> units;
   private int gotoRow;
   private int targetOffset;
   private int targetPage;
   private GetTransUnitsNavigationResult navigationIndex;
   
   private List<TransUnitValidationResult> validationResult;

   @SuppressWarnings("unused")
   private GetTransUnitListResult()
   {
   }

   public GetTransUnitListResult(DocumentId documentId, List<TransUnit> units, int gotoRow, int targetOffset, int targetPage)
   {
      this.documentId = documentId;
      this.units = units;
      this.gotoRow = gotoRow;
      this.targetOffset = targetOffset;
      this.targetPage = targetPage;
   }

   public List<TransUnit> getUnits()
   {
      return units;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public int getGotoRow()
   {
      return gotoRow;
   }

   public int getTargetOffset()
   {
      return targetOffset;
   }

   public int getTargetPage()
   {
      return targetPage;
   }

   public GetTransUnitsNavigationResult getNavigationIndex()
   {
      return navigationIndex;
   }

   public void setNavigationIndex(GetTransUnitsNavigationResult navigationIndex)
   {
      this.navigationIndex = navigationIndex;
   }
   
   public void setValidationResults(List<TransUnitValidationResult> validationResult)
   {
      this.validationResult =validationResult;
   }

   public List<TransUnitValidationResult> getValidationResult()
   {
      return validationResult;
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("documentId", documentId).
            add("units.size", units == null ? 0 : units.size()).
            add("gotoRow", gotoRow).
            add("targetOffset", targetOffset).
            add("targetPage", targetPage).
            add("navigationIndex", navigationIndex).
            toString();
   }

  
}
