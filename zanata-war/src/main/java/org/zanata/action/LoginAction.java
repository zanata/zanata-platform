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

import lombok.Getter;
import lombok.Setter;

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

   @Getter @Setter
   private String username;

   @Getter @Setter
   private String password;

   @Getter @Setter
   private String openId = "http://";

   public String login()
   {
      credentials.setUsername(username);
      credentials.setPassword(password);
      if (applicationConfiguration.isInternalAuth())
      {
         credentials.setAuthType(AuthenticationType.INTERNAL);
      }
      else if (applicationConfiguration.isJaasAuth())
      {
         credentials.setAuthType(AuthenticationType.JAAS);
      }

      String loginResult;

      switch (credentials.getAuthType())
      {
      case INTERNAL:
         credentials.setAuthType(AuthenticationType.INTERNAL);
         loginResult = authenticationManager.internalLogin();
         break;
      case JAAS:
         credentials.setAuthType(AuthenticationType.JAAS);
         loginResult = authenticationManager.jaasLogin();
         break;
      // Kerberos auth happens on its own
      default:
         throw new RuntimeException("login() only supports internal or jaas authentication");
      }

      return loginResult;
   }

   /**
    * Only for open id.
    * @param authProvider Open Id authentication provider.
    */
   public String openIdLogin(String authProvider)
   {
      OpenIdProviderType providerType = OpenIdProviderType.valueOf(authProvider);

      if( providerType == OpenIdProviderType.Generic )
      {
         credentials.setUsername(openId);
      }

      credentials.setAuthType(AuthenticationType.OPENID);
      credentials.setOpenIdProviderType(providerType);
      return authenticationManager.openIdLogin();
   }
}
