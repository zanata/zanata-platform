package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;


public class ActivateWorkspaceResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private UserWorkspaceContext userWorkspaceContext;
   private Identity identity;
   private UserConfigHolder.ConfigurationState storedUserConfiguration;

   @SuppressWarnings("unused")
   private ActivateWorkspaceResult()
   {
   }

   public ActivateWorkspaceResult(UserWorkspaceContext userWorkspaceContext,
                                  Identity identity,
                                  UserConfigHolder.ConfigurationState storedUserConfiguration)
   {
      this.userWorkspaceContext = userWorkspaceContext;
      this.identity = identity;
      this.storedUserConfiguration = storedUserConfiguration;
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
}
