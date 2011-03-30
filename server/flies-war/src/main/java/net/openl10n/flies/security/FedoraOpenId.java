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
package net.openl10n.flies.security;

import static org.jboss.seam.ScopeType.SESSION;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.openid.OpenId;

@Name("org.jboss.seam.security.fedoraOpenId")
@Scope(SESSION)
public class FedoraOpenId
{
   private OpenId openId;
   private static String FEDORA_HOST = ".id.fedoraproject.org/";
   private static final LogProvider log = Logging.getLogProvider(FedoraOpenId.class);
   private FliesIdentity identity;
   private FliesJpaIdentityStore identityStore;

   @Create
   public void init()
   {
      identity = (FliesIdentity) Component.getInstance(FliesIdentity.class, ScopeType.SESSION);
      openId = (OpenId) Component.getInstance(OpenId.class, ScopeType.SESSION);
      identityStore = (FliesJpaIdentityStore) Component.getInstance(FliesJpaIdentityStore.class, ScopeType.APPLICATION);
   }
   
   public String loginImmediately() throws LoginException
   {
      if (openId.loginImmediately())
      {
         identity.setPreAuthenticated(true);
         if (!identity.isLoggedIn())
         {
            throw new LoginException();
         }

         if (Events.exists())
            Events.instance().raiseEvent(FliesIdentity.EVENT_LOGIN_SUCCESSFUL);
         
         if (identityStore.isNewUser(identity.getCredentials().getUsername()))
         {
            return "new";
         }

         return "success";
      }
      else
      {
         return "failure";
      }

   }

   public void login(String username) throws LoginException
   {
      try
      {
         String var = "http://" + username + FEDORA_HOST;
         openId.setId(var);
         log.info("openid:" + openId.getId());
         openId.login();
      }
      catch (IOException e)
      {
         LoginException le = new LoginException(e.getMessage());
         throw le;
      }
   }


}
