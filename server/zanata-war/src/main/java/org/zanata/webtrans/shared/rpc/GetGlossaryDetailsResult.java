package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.GlossaryDetails;

public class GetGlossaryDetailsResult implements Result
{

   private static final long serialVersionUID = 1L;
   
   private ArrayList<GlossaryDetails> glossaryDetails;

   @SuppressWarnings("unused")
   private GetGlossaryDetailsResult()
   {
      this(null);
   }

   public GetGlossaryDetailsResult(ArrayList<GlossaryDetails> glossaryDetails)
   {
      this.glossaryDetails = glossaryDetails;
   }

   public ArrayList<GlossaryDetails> getGlossaryDetails()
   {
      return glossaryDetails;
   }

   
}
