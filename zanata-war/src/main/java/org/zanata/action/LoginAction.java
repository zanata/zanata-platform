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
package org.zanata.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Credentials;
import org.zanata.ApplicationConfiguration;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataExternalLoginBean;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.ZanataOpenId;
import org.zanata.security.openid.OpenIdProviderType;

/**
 * This action takes care of logging a user into the system. It contains logic to
 * handle the different authentication mechanisms offered by the system.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("loginAction")
@Scope(ScopeType.PAGE)
public class LoginAction
{
   // Open Id Provider types
   private static final int OTHER_OPENID = -1;
   private static final int FEDORA = 1;
   private static final int MYOPENID = 2;
   private static final int YAHOO = 3;

   @In
   private ZanataIdentity identity;

   @In
   private ZanataOpenId zanataOpenId;

   @In
   private Credentials credentials;

   @In
   private ApplicationConfiguration applicationConfiguration;

   @In
   private ZanataExternalLoginBean zanataExternalLoginBean;

   private String username;

   private String authProvider;

   private AuthenticationType authType;

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getAuthProvider()
   {
      return authProvider;
   }

   public void setAuthProvider(String authProvider)
   {
      this.authProvider = authProvider;
   }

   /**
    * Prepares authentication based on the passed parameters.
    */
   private void configureAuthentication()
   {
      try
      {
         // If it is open Id
         OpenIdProviderType providerType = OpenIdProviderType.valueOf(authProvider);
         this.authType = AuthenticationType.OPENID;
         zanataOpenId.setProvider(providerType);
      }
      catch (IllegalArgumentException e)
      {
         // If it's not open id, it might be another authentication type
         this.authType = AuthenticationType.valueOf(authProvider);
      }
   }

   public boolean showLoginChoices()
   {
      int availableChoices = 0;

      if( applicationConfiguration.isOpenIdAuth() )
      {
         availableChoices += 5;
      }
      if( applicationConfiguration.isKerberosAuth() )
      {
         availableChoices++;
      }
      if( applicationConfiguration.isInternalAuth() )
      {
         availableChoices++;
      }
      if( applicationConfiguration.isJaasAuth() )
      {
         availableChoices++;
      }

      return availableChoices > 1;
   }

   public String login()
   {
      this.configureAuthentication();
      String loginResult = null;

      switch (authType)
      {
         case OPENID:
            loginResult = this.loginWithOpenId();
            break;
         case INTERNAL:
            loginResult = this.loginWithInternal();
            break;
         case JAAS:
            loginResult = this.loginWithJaas();
            break;
         // Kerberos auth happens on its own
      }

      return loginResult;
   }

   private String loginWithOpenId()
   {
      credentials.setUsername( username );
      // Federated OpenId providers
      if( zanataOpenId.isFederatedProvider() )
      {
         // NB: Credentials' user name must be set to something or else login will fail. The real user name will be asked
         // by the provider
         credentials.setUsername("zanata");
         String loginResult = identity.login(authType);

         // Clear out the credentials again
         credentials.setUsername("");

         return loginResult;
      }

      return this.identity.login(authType);
   }

   private String loginWithInternal()
   {
      credentials.setUsername( username );
      return this.identity.login(authType);
   }

   private String loginWithJaas()
   {
      credentials.setUsername( username );
      return this.identity.login(authType);
   }

}
