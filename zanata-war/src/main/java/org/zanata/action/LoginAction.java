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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.zanata.ApplicationConfiguration;
import org.zanata.security.FedoraOpenId;
import org.zanata.security.openid.OpenIdProvider;
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
   private Identity identity;

   @In
   private FedoraOpenId fedoraOpenId;

   @In
   private Credentials credentials;

   @In
   private ApplicationConfiguration applicationConfiguration;

   private String username;

   private OpenIdProviderType providerType;


   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getProviderType()
   {
      return providerType != null ? providerType.toString() : "";
   }

   public void setProviderType(String providerType)
   {
      try
      {
         this.providerType = OpenIdProviderType.valueOf(providerType);
      }
      catch (IllegalArgumentException e)
      {
         this.providerType = OpenIdProviderType.Generic;
      }
   }

   /**
    * NB: Since Google only offers a federated open id login, there is no need to
    * ask for a user name. Simply perform the authentication right away.
    */
   private String useGoogleLogin()
   {
      // NB: Credentials' user name must be set to something or else login will fail. The real user name will be asked
      // by the provider
      credentials.setUsername("google");
      String loginResult = identity.login();

      // Clear out the credentials again
      credentials.setUsername("");

      return loginResult;
   }

   public String login()
   {
      if( applicationConfiguration.isFedoraOpenIdAuth() )
      {
         credentials.setUsername( username );
         fedoraOpenId.setProvider( this.providerType );

         // Google is a special case (federated)
         if( this.providerType == OpenIdProviderType.Google )
         {
            return this.useGoogleLogin();
         }
      }

      return identity.login();
   }

}
