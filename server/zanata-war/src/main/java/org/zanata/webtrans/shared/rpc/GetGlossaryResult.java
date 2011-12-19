package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;


public class GetGlossaryResult implements Result
{

   private static final long serialVersionUID = 1L;

   private ArrayList<TranslationMemoryGlossaryItem> glossaries;

   @SuppressWarnings("unused")
   private GetGlossaryResult()
   {
   }

   public GetGlossaryResult(ArrayList<TranslationMemoryGlossaryItem> glossaries)
   {
      this.glossaries = glossaries;
   }

   public ArrayList<TranslationMemoryGlossaryItem> getGlossaries()
   {
      return glossaries;
   }

   public void setGlossaries(ArrayList<TranslationMemoryGlossaryItem> glossaries)
   {
      this.glossaries = glossaries;
   }
}
