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

import java.util.ArrayList;
import java.util.List;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.drools.FactHandle;
import org.drools.StatefulSession;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.Configuration;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.security.permission.RuleBasedPermissionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class ZanataIdentity extends Identity
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ZanataIdentity.class);

   public static final String USER_LOGOUT_EVENT = "user.logout";
   public static final String USER_ENTER_WORKSPACE = "user.enter";
   public static final String JAAS_DEFAULT = "default";

   private static final long serialVersionUID = -5488977241602567930L;

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
      {
         throw new NotLoggedInException();
      }
   }

   @Observer("org.jboss.seam.preDestroyContext.SESSION")
   public void logout()
   {
      if (Events.exists() && getCredentials() != null)
      {
         Events.instance().raiseEvent(USER_LOGOUT_EVENT, getCredentials().getUsername());
      }
      super.logout();
   }

   @Override
   public boolean hasPermission(Object target, String action)
   {
      LOGGER.debug("ENTER hasPermission({}, {})", target, action);
      boolean result = super.hasPermission(target, action);
      LOGGER.debug("EXIT hasPermission(): {}", result);
      return result;
   }

   @Override
   public boolean hasPermission(String name, String action, Object... arg)
   {
      LOGGER.debug("ENTER hasPermission({})", Lists.newArrayList(name, action, arg));
      boolean result = super.hasPermission(name, action, arg);
      LOGGER.debug("EXIT hasPermission(): {}", result);
      return result;
   }
   
   /**
    * Indicates if the user has permissions on a variable number of facts. 
    * This method is a utility provision for Seam's lack of multi-fact insertion into working memory.
    * 
    * @param action The permission action.
    * @param targets Targets for permissions.
    */
   public boolean hasPermission(String action, Object ... targets)
   {
      final List<FactHandle> handles = new ArrayList<FactHandle>();
      StatefulSession securityContext = RuleBasedPermissionResolver.instance().getSecurityContext();
      
      synchronized (securityContext)
      {
         // First target
         Object firstTarget = targets.length > 0 ? targets[0] : null;
         
         // Insert the rest of the targets into working memory
         for( int i = 1; i < targets.length; i++ )
         {
            handles.add( securityContext.insert(targets[i]) );
         }
         
         // Run the permission check
         boolean result = super.hasPermission(firstTarget, action);
         
         // Retract all inserted targets
         for (FactHandle handle : handles)
         {
            securityContext.retract(handle);
         }
         
         return result;
      }
   }
   
   /**
    * Checks permissions on a variable number of facts. 
    * This method is a utility provision for Seam's lack of multi-fact insertion into working memory.
    * 
    * @param action The permission action.
    * @param targets Targets for permissions.
    */
   public void checkPermission(String action, Object ... targets)
   {
      final List<FactHandle> handles = new ArrayList<FactHandle>();
      StatefulSession securityContext = RuleBasedPermissionResolver.instance().getSecurityContext();
      
      synchronized (securityContext)
      {
         // First target
         Object firstTarget = targets.length > 0 ? targets[0] : null;
         
         // Insert the rest of the targets into working memory
         for( int i = 1; i < targets.length; i++ )
         {
            handles.add( securityContext.insert(targets[i]) );
         }
         
         // Run the permission check
         super.checkPermission(firstTarget, action);
         
         // Retract all inserted targets
         for (FactHandle handle : handles)
         {
            securityContext.retract(handle);
         }
      }
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
