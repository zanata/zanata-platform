package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;


public class ActivateWorkspaceResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private UserWorkspaceContext userWorkspaceContext;
   private Identity identity;
   private UserConfigHolder.ConfigurationState storedUserConfiguration;
   private Map<ValidationId, State> validationStates;

   @SuppressWarnings("unused")
   private ActivateWorkspaceResult()
   {
   }

   public ActivateWorkspaceResult(UserWorkspaceContext userWorkspaceContext,
                                  Identity identity,
 UserConfigHolder.ConfigurationState storedUserConfiguration, Map<ValidationId, State> validationStates)
   {
      this.userWorkspaceContext = userWorkspaceContext;
      this.identity = identity;
      this.storedUserConfiguration = storedUserConfiguration;
      this.validationStates = validationStates;
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

   public Map<ValidationId, State> getValidationStates()
   {
      return validationStates;
   }
}
