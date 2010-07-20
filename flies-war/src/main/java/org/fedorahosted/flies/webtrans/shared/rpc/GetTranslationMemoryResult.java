package org.fedorahosted.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.webtrans.shared.model.TranslationMemoryItem;

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
