package org.zanata.webtrans.shared.rpc;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;


public class GetTransUnitListResult implements Result
{
   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private int totalCount;
   private List<TransUnit> units;
   private int gotoRow;

   @SuppressWarnings("unused")
   private GetTransUnitListResult()
   {
   }

   public GetTransUnitListResult(DocumentId documentId, List<TransUnit> units, int totalCount, int gotoRow)
   {
      this.documentId = documentId;
      this.units = units;
      this.totalCount = totalCount;
      this.gotoRow = gotoRow;
   }

   public List<TransUnit> getUnits()
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
}
