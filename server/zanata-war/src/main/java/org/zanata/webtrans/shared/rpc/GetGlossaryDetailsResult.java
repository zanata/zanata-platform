package org.zanata.webtrans.shared.rpc;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.GlossaryDetails;

public class GetGlossaryDetailsResult implements Result
{

   private static final long serialVersionUID = 1L;
   
   private List<GlossaryDetails> glossaryDetails;

   @SuppressWarnings("unused")
   private GetGlossaryDetailsResult()
   {
      this(null);
   }

   public GetGlossaryDetailsResult(List<GlossaryDetails> glossaryDetails)
   {
      this.glossaryDetails = glossaryDetails;
   }

   public List<GlossaryDetails> getGlossaryDetails()
   {
      return glossaryDetails;
   }

   
}
