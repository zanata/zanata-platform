package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.TransMemoryResultItem;


public class GetTranslationMemoryResult implements Result
{

   private static final long serialVersionUID = 1L;

   private GetTranslationMemory request;
   private ArrayList<TransMemoryResultItem> transmemories;


   @SuppressWarnings("unused")
   private GetTranslationMemoryResult()
   {
   }

   public GetTranslationMemoryResult(GetTranslationMemory request, ArrayList<TransMemoryResultItem> transmemories)
   {
      this.request = request;
      this.transmemories = transmemories;
   }

   /**
    * @return the request
    */
   public GetTranslationMemory getRequest()
   {
      return request;
   }

   public ArrayList<TransMemoryResultItem> getMemories()
   {
      return transmemories;
   }
}
