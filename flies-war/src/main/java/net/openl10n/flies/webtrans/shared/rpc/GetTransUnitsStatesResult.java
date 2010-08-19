package net.openl10n.flies.webtrans.shared.rpc;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.webtrans.shared.model.DocumentId;


public class GetTransUnitsStatesResult implements Result
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private List<Long> units;

   @SuppressWarnings("unused")
   private GetTransUnitsStatesResult()
   {
   }

   public GetTransUnitsStatesResult(DocumentId documentId, List<Long> units)
   {
      this.documentId = documentId;
      this.units = units;
   }

   public List<Long> getUnits()
   {
      return units;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }
}
