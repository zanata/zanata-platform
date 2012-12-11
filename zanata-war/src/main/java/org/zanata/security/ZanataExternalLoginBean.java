/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.security;


import static org.jboss.seam.annotations.Install.APPLICATION;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.zanata.ApplicationConfiguration;
import org.zanata.action.InactiveAccountAction;

@Name("zanataExternalLoginBean")
@Scope(ScopeType.SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class ZanataExternalLoginBean implements Serializable
{
   private static final long serialVersionUID = 1L;

   private ZanataIdentity identity;

   private ApplicationConfiguration applicationConfiguration;

   private UserRedirectBean userRedirectBean;

   private AuthenticationManager authenticationManager;

   private String redirectUsername = "";

   private InactiveAccountAction inactiveAccountAction;

   @Create
   public void init()
   {
      identity = (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);
      applicationConfiguration = (ApplicationConfiguration) Component.getInstance(ApplicationConfiguration.class, ScopeType.APPLICATION);
      userRedirectBean = (UserRedirectBean) Component.getInstance(UserRedirectBean.class, ScopeType.SESSION);
      authenticationManager = (AuthenticationManager) Component.getInstance(AuthenticationManager.class, ScopeType.SESSION);
   }


   public boolean isAccountActivate()
   {
      String username = identity.getCredentials().getUsername();
      if (!authenticationManager.isAccountActivated(username))
      {
         FacesMessages.instance().clear();
         FacesMessages.instance().add("#{messages['org.jboss.seam.loginFailed']}");
         identity.setPreAuthenticated(false);
         identity.unAuthenticate();

         redirectUsername = username;

         return false;
      }
      redirectUsername = "";
      return true;
   }

   public boolean isAccountEnabled()
   {
      String username = identity.getCredentials().getUsername();
      if (!authenticationManager.isAccountEnabled(username))
      {
         FacesMessages.instance().clear();
         FacesMessages.instance().add("User {0} has been disabled. Please contact server admin.", username);
         identity.setPreAuthenticated(false);
         identity.unAuthenticate();

         return false;
      }
      return true;
   }

   public boolean isRedirectToInactiveAccPage()
   {
      if (!StringUtils.isEmpty(redirectUsername))
      {
         initInactionAccountAction();
         return true;
      }
      return false;
   }

   @Begin
   private void initInactionAccountAction()
   {
      inactiveAccountAction = (InactiveAccountAction) Component.getInstance(InactiveAccountAction.class, ScopeType.CONVERSATION);
      inactiveAccountAction.setUsername(redirectUsername);
   }

   public boolean isNewUser()
   {
      return authenticationManager.isNewUser(identity.getCredentials().getUsername());
   }

   public boolean externalLogin()
   {
      return identity.getAuthenticationType() != AuthenticationType.INTERNAL && !identity.isApiRequest();
   }

   public void applyAuthentication()
   {
      String username = identity.getCredentials().getUsername();

      for (String role : authenticationManager.getImpliedRoles(username))
      {
         identity.addRole(role);
      }
      authenticationManager.setAuthenticateUser(username);
   }


   @Observer(Identity.EVENT_LOGIN_SUCCESSFUL)
   public void loginInSuccessful()
   {
      identity.setPreAuthenticated(true);
      if (externalLogin() && !isNewUser())
      {
         if (isAccountActivate())
         {
            applyAuthentication();
         }
         else if (isAccountEnabled())
         {
            applyAuthentication();
         }
      }
   }

   public void spNegoExecute()
   {
      if (applicationConfiguration.isKerberosAuth())
      {
         SpNegoIdentity spNegoIdentity = (SpNegoIdentity) Component.getInstance(SpNegoIdentity.class, ScopeType.SESSION);
         spNegoIdentity.setCredential();
      }
   }

   public String redirect()
   {
      if (identity.getAuthenticationType() == AuthenticationType.KERBEROS && identity.isLoggedIn() && isNewUser())
      {
         return "edit";
      }

      if (identity.getAuthenticationType() == AuthenticationType.KERBEROS && identity.isLoggedIn() && !isNewUser())
      {
         if (userRedirectBean.isRedirect())
         {
            return "redirect";
         }
         else
         {
            return "home";
         }
      }

      if (identity.getAuthenticationType() == AuthenticationType.KERBEROS && !identity.isLoggedIn())
      {
         if (isRedirectToInactiveAccPage())
         {
            return "inactiveAccount";
         }
         return "home";
      }

      if (identity.getAuthenticationType() != AuthenticationType.KERBEROS)
      {
         return "login";
      }
      return null;
   }

}
