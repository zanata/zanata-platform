package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationInfo;



public class GetValidationRulesResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private List<ValidationInfo> validations;

   @SuppressWarnings("unused")
   private GetValidationRulesResult()
   {
   }

   public GetValidationRulesResult(List<ValidationInfo> validations)
   {
      this.validations = validations;
   }

   public List<ValidationInfo> getValidations()
   {
      return validations;
   }
}
