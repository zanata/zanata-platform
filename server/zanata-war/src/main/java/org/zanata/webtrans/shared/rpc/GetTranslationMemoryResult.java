package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;


public class GetTranslationMemoryResult implements Result
{

   private static final long serialVersionUID = 1L;

   private GetTranslationMemory request;
   private ArrayList<TranslationMemoryGlossaryItem> transmemories;


   @SuppressWarnings("unused")
   private GetTranslationMemoryResult()
   {
   }

   public GetTranslationMemoryResult(GetTranslationMemory request, ArrayList<TranslationMemoryGlossaryItem> transmemories)
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

   public ArrayList<TranslationMemoryGlossaryItem> getMemories()
   {
      return transmemories;
   }
}
