package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;



public class GetValidationRulesResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private Map<ValidationId, ValidationInfo> validations;

   @SuppressWarnings("unused")
   private GetValidationRulesResult()
   {
   }

   public GetValidationRulesResult(Map<ValidationId, ValidationInfo> validations)
   {
      this.validations = validations;
   }

   public Map<ValidationId, ValidationInfo> getValidations()
   {
      return validations;
   }
}
