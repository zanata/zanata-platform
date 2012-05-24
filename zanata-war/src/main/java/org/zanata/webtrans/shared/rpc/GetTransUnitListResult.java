package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import com.google.common.base.Objects;


public class GetTransUnitListResult implements DispatchResult
{
   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private int totalCount;
   private ArrayList<TransUnit> units;
   private int gotoRow;

   @SuppressWarnings("unused")
   private GetTransUnitListResult()
   {
   }

   public GetTransUnitListResult(DocumentId documentId, ArrayList<TransUnit> units, int totalCount, int gotoRow)
   {
      this.documentId = documentId;
      this.units = units;
      this.totalCount = totalCount;
      this.gotoRow = gotoRow;
   }

   public ArrayList<TransUnit> getUnits()
   {
      return units;
   }

   public int getTotalCount()
   {
      return totalCount;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public int getGotoRow()
   {
      return gotoRow;
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("documentId", documentId).
            add("totalCount", totalCount).
            add("gotoRow", gotoRow).
            add("units.size", units == null ? 0 : units.size()).
            toString();
   }
}
