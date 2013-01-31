package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationRule;


public class ActivateWorkspaceResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private UserWorkspaceContext userWorkspaceContext;
   private Identity identity;
   private UserConfigHolder.ConfigurationState storedUserConfiguration;
   private List<ValidationRule> validationRules;

   @SuppressWarnings("unused")
   private ActivateWorkspaceResult()
   {
   }

   public ActivateWorkspaceResult(UserWorkspaceContext userWorkspaceContext,
                                  Identity identity,
 UserConfigHolder.ConfigurationState storedUserConfiguration, List<ValidationRule> validationRules)
   {
      this.userWorkspaceContext = userWorkspaceContext;
      this.identity = identity;
      this.storedUserConfiguration = storedUserConfiguration;
      this.validationRules = validationRules;
   }

   public UserWorkspaceContext getUserWorkspaceContext()
   {
      return userWorkspaceContext;
   }

   public Identity getIdentity()
   {
      return identity;
   }

   public UserConfigHolder.ConfigurationState getStoredUserConfiguration()
   {
      return storedUserConfiguration;
   }

   public List<ValidationRule> getValidationRules()
   {
      return validationRules;
   }
}
