/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.security;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.CredentialsDAO;
import org.zanata.model.HAccount;
import org.zanata.model.security.HCredentials;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.UserAccountService;

/**
 * Centralizes all attempts to authenticate locally or externally.
 *
 * The authenticate methods will perform the authentication but will not login the authenticated
 * user against the session. The login methods will perform these two steps.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("authenticationManager")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class AuthenticationManager
{
   /* Event used to signal a successful login using the authentication manager.
    * It is a compliment to the events in the Identity class.*/
   public static final String EVENT_LOGIN_COMPLETED = "org.zanata.security.event.loginCompleted";

   @In
   private ZanataIdentity identity;

   @In
   private Credentials credentials;

   @In
   private ZanataOpenId zanataOpenId;

   @In
   private UserAccountService userAccountServiceImpl;

   @In
   private CredentialsDAO credentialsDAO;

   @In
   private AccountDAO accountDAO;


   /**
    * Logs in a user using internal authentication.
    *
    * @param username User's name
    * @param password User's password
    * @return A String with the result of the operation.
    */
   public String login(String username, String password)
   {
      return this.login(AuthenticationType.INTERNAL, username, password);
   }

   /**
    * Logs in a user using a specified authentication type.
    *
    * @param authenticationType Authentication type to use.
    * @param username User's name.
    * @param password User's password. May be null for some authentication types.
    * @return A String with the result of the operation.
    */
   public String login( AuthenticationType authenticationType, String username, String password )
   {
      credentials.setUsername(username);
      credentials.setPassword(password);

      String result = identity.login(authenticationType);
      if( result != null && result.equals("loggedIn") )
      {
         this.onLoginCompleted(authenticationType);
      }

      return result;
   }

   /**
    * Logs in an Open Id user
    *
    * @param openIdProviderType Open Id provider to use for authentication
    * @param username User name. The provider will use this username to construct an Open Id.
    * @return A String with the result of the operation.
    */
   public String openIdLogin(OpenIdProviderType openIdProviderType, String username)
   {
      credentials.setUsername(username);
      zanataOpenId.setProvider( openIdProviderType );

      // Federated OpenId providers
      if( zanataOpenId.isFederatedProvider() )
      {
         // NB: Credentials' user name must be set to something or else login will fail. The real user name will be asked
         // by the provider
         credentials.setUsername("zanata");
      }

      String loginResult = identity.login( AuthenticationType.OPENID );

      if( zanataOpenId.isFederatedProvider() )
      {
         // Clear out the credentials again
         credentials.setUsername("");
      }

      return loginResult;
   }

   /**
    * Authenticates an Open Id user. This method <b>will not</b> log in the authenticated user.
    * Because control needs to be handled over to the Open Id provider, a callback may be
    * provided to perform actions after the authentication attempt is finished.
    *
    * @param openIdProviderType Open Id provider to use for authentication
    * @param username User name. The provider will use this username to construct an Open Id.
    * @param callback Contains the logic to execute after the authentication attempt.
    */
   public void openIdAuthenticate(OpenIdProviderType openIdProviderType, String username, OpenIdAuthCallback callback)
   {
      zanataOpenId.login(username, openIdProviderType, callback);
   }


   /**
    * Performs operations after a successful login is completed.
    *
    * @param authType Authentication type that was used to login.
    */
   @Observer(EVENT_LOGIN_COMPLETED)
   public void onLoginCompleted(AuthenticationType authType)
   {
      // Get the authenticated account and credentials
      HAccount authenticatedAccount;
      HCredentials authenticatedCredentials;

      String username = credentials.getUsername();

      if( authType == AuthenticationType.OPENID )
      {
         authenticatedCredentials = credentialsDAO.findByUser( zanataOpenId.getAuthResult().getAuthenticatedId() );
         authenticatedAccount = authenticatedCredentials.getAccount();
      }
      else
      {
         authenticatedCredentials = credentialsDAO.findByUser( username );
         authenticatedAccount = accountDAO.getByUsername( username );
      }

      userAccountServiceImpl.runRoleAssignmentRules(authenticatedAccount, authenticatedCredentials, authType.name());
   }

}
