package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;


public class GetGlossaryResult implements Result
{

   private static final long serialVersionUID = 1L;

   private ArrayList<TranslationMemoryGlossaryItem> glossaries;
   private GetGlossary request;

   @SuppressWarnings("unused")
   private GetGlossaryResult()
   {
   }

   public GetGlossaryResult(GetGlossary request, ArrayList<TranslationMemoryGlossaryItem> glossaries)
   {
      this.glossaries = glossaries;
      this.request = request;
   }

   public ArrayList<TranslationMemoryGlossaryItem> getGlossaries()
   {
      return glossaries;
   }

   public void setGlossaries(ArrayList<TranslationMemoryGlossaryItem> glossaries)
   {
      this.glossaries = glossaries;
   }

   public GetGlossary getRequest()
   {
      return request;
   }
}
