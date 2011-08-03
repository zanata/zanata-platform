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

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Configuration;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class ZanataIdentity extends Identity
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   public static final String USER_LOGOUT_EVENT = "user.logout";
   public static final String USER_ENTER_WORKSPACE = "user.enter";
   public static final String JAAS_DEFAULT = "default";

   private static final LogProvider log = Logging.getLogProvider(ZanataIdentity.class);

   private String apiKey;

   private boolean preAuthenticated;

   public String getApiKey()
   {
      return apiKey;
   }

   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
      getCredentials().setPassword(apiKey);
   }

   public boolean isApiRequest()
   {
      return apiKey != null;
   }
   
   public static ZanataIdentity instance()
   {
      if (!Contexts.isSessionContextActive())
      {
         throw new IllegalStateException("No active session context");
      }

      ZanataIdentity instance = (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);

      if (instance == null)
      {
         throw new IllegalStateException("No Identity could be created");
      }

      return instance;
   }

   public void checkLoggedIn()
   {
      if (!isLoggedIn())
         throw new NotLoggedInException();
   }

   public void logout()
   {
      if (Events.exists() && getPrincipal() != null)
         Events.instance().raiseEvent(USER_LOGOUT_EVENT, getPrincipal().getName());
      super.logout();
   }

   @Override
   public boolean hasPermission(Object target, String action)
   {
      if (log.isDebugEnabled())
         log.debug("ENTER hasPermission(" + target + "," + action + ")");
      boolean result = super.hasPermission(target, action);
      if (log.isDebugEnabled())
         log.debug("EXIT hasPermission(): " + result);
      return result;
   }

   @Override
   public boolean hasPermission(String name, String action, Object... arg)
   {
      if (log.isDebugEnabled())
         log.debug("ENTER hasPermission(" + name + "," + action + "," + arg + ")");
      boolean result = super.hasPermission(name, action, arg);
      if (log.isDebugEnabled())
         log.debug("EXIT hasPermission(): " + result);
      return result;
   }

   @Override
   public LoginContext getLoginContext() throws LoginException
   {
      if (isApiRequest())
      {
         return new LoginContext(JAAS_DEFAULT, getSubject(), getCredentials().createCallbackHandler(), Configuration.instance());
      }
      if (getJaasConfigName() != null && !getJaasConfigName().equals(JAAS_DEFAULT))
      {
         return new LoginContext(getJaasConfigName(), getSubject(), getCredentials().createCallbackHandler());
      }

      return new LoginContext(JAAS_DEFAULT, getSubject(), getCredentials().createCallbackHandler(), Configuration.instance());
   }

   public boolean isPreAuthenticated()
   {
      return preAuthenticated;
   }

   public void setPreAuthenticated(boolean var)
   {
      this.preAuthenticated = var;
   }

   public String login()
   {
      String result = super.login();
      if (result != null && result.equals("loggedIn"))
      {
         this.preAuthenticated = true;
      }
      return result;
   }
}
