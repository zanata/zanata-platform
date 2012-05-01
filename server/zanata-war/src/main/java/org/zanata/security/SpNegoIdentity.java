/*
R * Copyright 2010, Red Hat, Inc. and individual contributors
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

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.security.SecurityAssociation;

@Name("org.jboss.seam.security.spNegoIdentity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
public class SpNegoIdentity implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private static final String SUBJECT = "subject";
   private static final String PRINCIPAL = "principal";
   private static final LogProvider log = Logging.getLogProvider(SpNegoIdentity.class);

   public void setCredential()
   {
      try
      {
         ZanataIdentity identity = (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);
         if (identity.isLoggedIn())
         {
            if (Events.exists())
               Events.instance().raiseEvent(Identity.EVENT_ALREADY_LOGGED_IN);
            return;
         }

         String username = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
         log.debug("remote username: " + username);

         identity.getCredentials().setUsername(username);
         identity.getCredentials().setPassword("");
         identity.getCredentials().setInitialized(true);
         
         Field field = Identity.class.getDeclaredField(PRINCIPAL);
         field.setAccessible(true);
         field.set(identity, SecurityAssociation.getCallerPrincipal());

         field = Identity.class.getDeclaredField(SUBJECT);
         field.setAccessible(true);
         field.set(identity, SecurityAssociation.getSubject());

         if (Events.exists())
            Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL);

      }
      catch (Exception e)
      {
         log.warn(e, e);
      }
   }
}
