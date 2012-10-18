package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import com.google.common.base.Objects;


public class GetTransUnitListResult implements DispatchResult
{
   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private List<TransUnit> units;
   private int gotoRow;

   @SuppressWarnings("unused")
   private GetTransUnitListResult()
   {
   }

   public GetTransUnitListResult(DocumentId documentId, List<TransUnit> units, int gotoRow)
   {
      this.documentId = documentId;
      this.units = units;
      this.gotoRow = gotoRow;
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

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("documentId", documentId).
            add("gotoRow", gotoRow).
            add("units.size", units == null ? 0 : units.size()).
            toString();
   }
}
