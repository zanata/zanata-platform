package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.TranslationMemoryItem;

import net.customware.gwt.dispatch.shared.Result;


public class GetTranslationMemoryResult implements Result
{

   private static final long serialVersionUID = 1L;

   private ArrayList<TranslationMemoryItem> transmemories;

   @SuppressWarnings("unused")
   private GetTranslationMemoryResult()
   {
   }

   public GetTranslationMemoryResult(ArrayList<TranslationMemoryItem> transmemories)
   {
      this.transmemories = transmemories;
   }

   public ArrayList<TranslationMemoryItem> getMemories()
   {
      return transmemories;
   }

   public void setConcepts(ArrayList<TranslationMemoryItem> transmemories)
   {
      this.transmemories = transmemories;
   }
}
