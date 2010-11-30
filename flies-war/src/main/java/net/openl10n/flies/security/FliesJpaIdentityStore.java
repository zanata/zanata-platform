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

import static org.jboss.seam.ScopeType.APPLICATION;



import net.openl10n.flies.FliesInit;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManagementException;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.util.AnnotatedBeanProperty;

@Name("org.jboss.seam.security.identityStore")
@Install(precedence = Install.DEPLOYMENT, value = true)
@Scope(APPLICATION)
@BypassInterceptors
public class FliesJpaIdentityStore extends JpaIdentityStore
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   // private static final Log log =
   // Logging.getLog(FliesJpaIdentityStore.class);

   private AnnotatedBeanProperty<UserApiKey> userApiKeyProperty;



   @Create
   public void init()
   {
      super.init();
      initProperties();
   }

   private void initProperties()
   {
      userApiKeyProperty = new AnnotatedBeanProperty<UserApiKey>(getUserClass(), UserApiKey.class);
      if (!userApiKeyProperty.isSet())
      {
         throw new IdentityManagementException("Invalid userClass " + getUserClass().getName() + " - required annotation @UserApiKey not found on any Field or Method.");
      }
   }

   public boolean apiKeyAuthenticate(String username, String apiKey)
   {
      Object user = lookupUser(username);
      if (user == null || !isUserEnabled(username))
      {
         return false;
      }

      if (!userApiKeyProperty.isSet())
      {
         return false;
      }

      String userApiKey = (String) userApiKeyProperty.getValue(user);

      if (userApiKey == null)
      {
         return false;
      }

      boolean success = apiKey.equals(userApiKey);
      setAuthenticateUser(user);
      return success;

   }

   public boolean isInternalAuthentication()
   {
      FliesInit instance = (FliesInit) Component.getInstance(FliesInit.class, ScopeType.APPLICATION);
      return instance.isInternalAuthentication();
   }


   @Override
   public boolean authenticate(String username, String password)
   {
      FliesIdentity identity = FliesIdentity.instance();
      if (identity.isApiRequest())
      {
         return apiKeyAuthenticate(username, password);
      }
      else
      {
         return super.authenticate(username, password);
      }
   }

   public boolean isNewUser()
   {
      Identity identity = Identity.instance();
      String username = identity.getCredentials().getUsername();
      Object user = lookupUser(username);
      return user == null;
   }


   public void jaasUserLoggedIn()
   {
      Identity identity = Identity.instance();
      String username = identity.getCredentials().getUsername();
      Object user = lookupUser(username);
      IdentityManager identityManager = IdentityManager.instance();
      for (String role : identityManager.getImpliedRoles(identity.getCredentials().getUsername()))
      {
         identity.addRole(role);
      }
      setAuthenticateUser(user);
   }


   public void setAuthenticateUser(Object user)
   {
      if (Events.exists())
      {
         if (Contexts.isSessionContextActive())
         {
            Contexts.getSessionContext().set(AUTHENTICATED_USER, user);
         }
         Events.instance().raiseEvent(EVENT_USER_AUTHENTICATED, user);
      }
   }

}
