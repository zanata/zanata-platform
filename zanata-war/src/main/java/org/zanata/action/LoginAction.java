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

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.openid.OpenIdProviderType;

/**
 * This action takes care of logging a user into the system. It contains logic
 * to handle the different authentication mechanisms offered by the system.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("loginAction")
@Scope(ScopeType.PAGE)
public class LoginAction implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   private ZanataCredentials credentials;

   @In
   private AuthenticationManager authenticationManager;

   @In
   private ApplicationConfiguration applicationConfiguration;

   @In
   private AccountDAO accountDAO;
   
   @In(create = true)
   private InactiveAccountAction inactiveAccountAction;

   private String username;

   private String password;

   private String authProvider;


   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
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
    * Prepares authentication credentials based on the passed parameters.
    */
   private void prepareCredentials()
   {
      AuthenticationType authType = null;
      OpenIdProviderType openIdProviderType = null;

      credentials.setUsername( username );
      credentials.setPassword( password );

      // All others
      if (authProvider == null)
      {
         if (applicationConfiguration.isInternalAuth())
         {
            authType = AuthenticationType.INTERNAL;
         }
         else if (applicationConfiguration.isJaasAuth())
         {
            authType = AuthenticationType.JAAS;
         }
         else if (applicationConfiguration.isKerberosAuth())
         {
            authType = AuthenticationType.KERBEROS;
         }
      }
      // Open Id / internal auth
      else
      {
         try
         {
            // If it is open Id
            openIdProviderType = OpenIdProviderType.valueOf(authProvider);
            authType = AuthenticationType.OPENID;
         }
         catch (Exception e)
         {
            // If it's not open id, it might be another authentication type
            openIdProviderType = null;
            authType = AuthenticationType.valueOf(authProvider);
         }
      }

      credentials.setAuthType( authType );
      credentials.setOpenIdProviderType( openIdProviderType );
   }

   public String login()
   {
      this.prepareCredentials();
      String loginResult = null;

      switch (credentials.getAuthType())
      {
      case OPENID:
         loginResult = authenticationManager.openIdLogin();
         break;
      case INTERNAL:
         loginResult = authenticationManager.internalLogin();
         break;
      case JAAS:
         loginResult = authenticationManager.jaasLogin();
         break;
      // Kerberos auth happens on its own
      }

      return loginResult;
   }
}
