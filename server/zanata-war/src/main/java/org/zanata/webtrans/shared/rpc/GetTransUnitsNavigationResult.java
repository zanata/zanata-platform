package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.DocumentId;


public class GetTransUnitsNavigationResult implements Result
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private ArrayList<Long> units;

   @SuppressWarnings("unused")
   private GetTransUnitsNavigationResult()
   {
   }

   public GetTransUnitsNavigationResult(DocumentId documentId, ArrayList<Long> units)
   {
      this.documentId = documentId;
      this.units = units;
   }

   public ArrayList<Long> getUnits()
   {
      return units;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }
}
