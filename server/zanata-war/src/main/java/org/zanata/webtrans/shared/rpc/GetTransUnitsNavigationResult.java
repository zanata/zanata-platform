package org.zanata.webtrans.shared.rpc;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.DocumentId;


public class GetTransUnitsNavigationResult implements Result
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private List<Long> units;

   @SuppressWarnings("unused")
   private GetTransUnitsNavigationResult()
   {
   }

   public GetTransUnitsNavigationResult(DocumentId documentId, List<Long> units)
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
