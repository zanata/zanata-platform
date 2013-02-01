package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationActionInfo;



public class GetValidationRulesResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private List<ValidationActionInfo> validations;

   @SuppressWarnings("unused")
   private GetValidationRulesResult()
   {
   }

   public GetValidationRulesResult(List<ValidationActionInfo> validations)
   {
      this.validations = validations;
   }

   public List<ValidationActionInfo> getValidations()
   {
      return validations;
   }
}
