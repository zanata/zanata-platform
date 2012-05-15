package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.DocumentId;


public class GetTransUnitsNavigationResult implements Result
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private ArrayList<Long> idIndexList;
   private Map<Long, ContentState> transIdStateList;


   @SuppressWarnings("unused")
   private GetTransUnitsNavigationResult()
   {
   }

   public GetTransUnitsNavigationResult(DocumentId documentId, ArrayList<Long> idIndexList, Map<Long, ContentState> transIdStateList)
   {
      this.documentId = documentId;
      this.idIndexList = idIndexList;
      this.transIdStateList = transIdStateList;
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public ArrayList<Long> getIdIndexList()
   {
      return idIndexList;
   }

   public Map<Long, ContentState> getTransIdStateList()
   {
      return transIdStateList;
   }

}
