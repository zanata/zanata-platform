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


import static org.jboss.seam.annotations.Install.APPLICATION;

import java.io.Serializable;

import net.openl10n.flies.FliesInit;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;

@Name("fliesExternalLoginBean")
@Scope(ScopeType.SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class FliesExternalLoginBean implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private FliesIdentity identity;

   private FliesJpaIdentityStore identityStore;

   private FliesInit fliesInit;

   @Create
   public void init()
   {
      identity = (FliesIdentity) Component.getInstance(FliesIdentity.class, ScopeType.SESSION);
      identityStore = (FliesJpaIdentityStore) Component.getInstance(FliesJpaIdentityStore.class, ScopeType.APPLICATION);
      fliesInit = (FliesInit) Component.getInstance(FliesInit.class, ScopeType.APPLICATION);
   }


   public boolean isNewUser()
   {
      return identityStore.isNewUser(identity.getCredentials().getUsername());
   }

   public boolean externalLogin()
   {
      return !fliesInit.isInternalAuthentication() && !identity.isApiRequest();
   }

   public void applyAuthentication()
   {
      Object user = identityStore.lookupUser(identity.getCredentials().getUsername());
      for (String role : identityStore.getImpliedRoles(identity.getCredentials().getUsername()))
      {
         identity.addRole(role);
      }
      identityStore.setAuthenticateUser(user);
   }

   public boolean checkDisabledUser()
   {
      String username = identity.getCredentials().getUsername();
      if (!identityStore.isUserEnabled(username))
      {
         FacesMessages.instance().clear();
         FacesMessages.instance().add("User {0} has been disabled.", username);
         identity.unAuthenticate();
         return true;
      }
      return false;
   }


}
