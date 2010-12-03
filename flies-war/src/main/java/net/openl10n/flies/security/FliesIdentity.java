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
package net.openl10n.flies.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import java.lang.reflect.Field;

import javax.faces.context.FacesContext;
import javax.security.auth.Subject;
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
import org.jboss.security.SecurityAssociation;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class FliesIdentity extends Identity
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   public static final String USER_LOGOUT_EVENT = "user.logout";
   public static final String USER_ENTER_WORKSPACE = "user.enter";
   public static final String JAAS_DEFAULT = "default";

   private static final String SUBJECT = "subject";
   private static final String PRINCIPAL = "principal";
   private static final String LOGGED_IN = "loggedIn";

   private static final LogProvider log = Logging.getLogProvider(FliesIdentity.class);

   private String apiKey;

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

   public static FliesIdentity instance()
   {
      if (!Contexts.isSessionContextActive())
      {
         throw new IllegalStateException("No active session context");
      }

      FliesIdentity instance = (FliesIdentity) Component.getInstance(FliesIdentity.class, ScopeType.SESSION);

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
      if (Events.exists())
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
      if (getJaasConfigName() != null && !getJaasConfigName().equals(JAAS_DEFAULT))
      {
         return new LoginContext(getJaasConfigName(), getSubject(), getCredentials().createCallbackHandler());
      }

      return new LoginContext(JAAS_DEFAULT, getSubject(), getCredentials().createCallbackHandler(), Configuration.instance());
   }

   public String spnegoLogin()
   {
      String spuser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
      if (spuser == null || spuser.isEmpty())
         return null;
      succeed(spuser);
      return LOGGED_IN;
   }

   private void succeed(String username)
   {
      try
      {
         getCredentials().setUsername(username);
         log.debug("username: " + getCredentials().getUsername());
         getCredentials().setPassword("");

         Field field = Identity.class.getDeclaredField(PRINCIPAL);
         field.setAccessible(true);
         field.set(this, SecurityAssociation.getCallerPrincipal());

         field = Identity.class.getDeclaredField(SUBJECT);
         field.setAccessible(true);
         field.set(this, SecurityAssociation.getSubject());

         if (Events.exists())
            Events.instance().raiseEvent(EVENT_LOGIN_SUCCESSFUL);
      }
      catch (Exception e)
      {
         log.info(e.getMessage());
      }
   }

   public synchronized void authenticate() throws LoginException
   {
      // If we're already authenticated, then don't authenticate again
      if (isApiRequest())
      {
         if (!isLoggedIn() && !getCredentials().isInvalid())
         {
            Field field;
            try
            {
               field = Identity.class.getDeclaredField(SUBJECT);
               field.setAccessible(true);
               field.set(this, new Subject());
               authenticate(new LoginContext(JAAS_DEFAULT, getSubject(), getCredentials().createCallbackHandler(), Configuration.instance()));
            }
            catch (Exception e)
            {
               throw new LoginException();
            }
         }
         return;
      }
      super.authenticate();
   }

}
