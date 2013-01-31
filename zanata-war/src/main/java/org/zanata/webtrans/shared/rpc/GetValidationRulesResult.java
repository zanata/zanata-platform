package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationRule;



public class GetValidationRulesResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private List<ValidationRule> validationRules;

   @SuppressWarnings("unused")
   private GetValidationRulesResult()
   {
   }

   public GetValidationRulesResult(List<ValidationRule> validationRules)
   {
      this.validationRules = validationRules;
   }

   public List<ValidationRule> getValidationRules()
   {
      return validationRules;
   }
}
