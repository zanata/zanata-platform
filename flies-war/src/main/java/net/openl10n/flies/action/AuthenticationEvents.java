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
package net.openl10n.flies.action;

import java.io.Serializable;

import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HPerson;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.security.permission.RuleBasedPermissionResolver;
import org.jboss.seam.security.Identity;
import net.openl10n.flies.security.FliesExternalLoginBean;

@Name("authenticationEvents")
@Scope(ScopeType.STATELESS)
public class AuthenticationEvents implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Logger
   Log log;

   @Out(required = false, scope = ScopeType.SESSION)
   HPerson authenticatedPerson;

   @In
   FliesExternalLoginBean fliesExternalLoginBean;

   @Observer(JpaIdentityStore.EVENT_USER_AUTHENTICATED)
   public void loginSuccessful(HAccount account)
   {
      log.info("Account {0} authenticated", account.getUsername());

      authenticatedPerson = account.getPerson();
      // insert authenticatedPerson for use in security.drl rules
      RuleBasedPermissionResolver.instance().getSecurityContext().insert(authenticatedPerson);
   }

   @Observer(JpaIdentityStore.EVENT_USER_CREATED)
   public void createSuccessful(HAccount account)
   {
      log.info("Account {0} created", account.getUsername());
   }

   @Observer(Identity.EVENT_LOGIN_SUCCESSFUL)
   public void loginInSuccessful()
   {
      log.debug("Account logged in successfully");
      if (fliesExternalLoginBean.externalLogin() && !fliesExternalLoginBean.isNewUser())
      {
         if (!fliesExternalLoginBean.checkDisabledUser())
         {
            fliesExternalLoginBean.applyAuthentication();
         }
      }
   }

}
