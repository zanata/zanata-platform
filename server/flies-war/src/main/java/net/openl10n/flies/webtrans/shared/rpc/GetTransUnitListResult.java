package net.openl10n.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnit;


public class GetTransUnitListResult implements Result
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private int totalCount;
   private ArrayList<TransUnit> units;

   @SuppressWarnings("unused")
   private GetTransUnitListResult()
   {
   }

   public GetTransUnitListResult(DocumentId documentId, ArrayList<TransUnit> units, int totalCount)
   {
      this.documentId = documentId;
      this.units = units;
      this.totalCount = totalCount;
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

}
