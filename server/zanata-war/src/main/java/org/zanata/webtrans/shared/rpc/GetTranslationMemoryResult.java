package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;


public class GetTranslationMemoryResult implements Result
{

   private static final long serialVersionUID = 1L;

   private ArrayList<TranslationMemoryGlossaryItem> transmemories;

   @SuppressWarnings("unused")
   private GetTranslationMemoryResult()
   {
   }

   public GetTranslationMemoryResult(ArrayList<TranslationMemoryGlossaryItem> transmemories)
   {
      this.transmemories = transmemories;
   }

   public ArrayList<TranslationMemoryGlossaryItem> getMemories()
   {
      return transmemories;
   }

   public void setConcepts(ArrayList<TranslationMemoryGlossaryItem> transmemories)
   {
      this.transmemories = transmemories;
   }
}
