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
package net.openl10n.flies.action;

import java.io.Serializable;

import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.dao.ApplicationConfigurationDAO;
import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HApplicationConfiguration;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.security.FliesJpaIdentityStore;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("profileAction")
@Scope(ScopeType.PAGE)
public class ProfileAction implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String name;
   private String email;
   private String username;
   @In
   ApplicationConfigurationDAO applicationConfigurationDAO;
   @Logger
   Log log;

   @In
   FliesJpaIdentityStore identityStore;

   @In
   Identity identity;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   PersonDAO personDAO;

   @In
   AccountDAO accountDAO;

   @Create
   public void onCreate()
   {
      username = identity.getCredentials().getUsername();
      name = !identityStore.isNewUser() ? authenticatedAccount.getPerson().getName() : identity.getCredentials().getUsername();
      if (identityStore.isNewUser())
      {
         String domain = "@redhat.com";
         HApplicationConfiguration ha = applicationConfigurationDAO.findByKey(HApplicationConfiguration.KEY_DOMAIN);
         if (ha != null && ha.getValue() != null && !ha.getValue().isEmpty())
         {
            domain = ha.getValue();
         }
         email=identity.getCredentials().getUsername() + domain;
      }else{
         email=authenticatedAccount.getPerson().getEmail();
      }
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   @Transactional
   public String edit()
   {
      if (!identityStore.isNewUser())
      {
         HPerson person = personDAO.findById(authenticatedAccount.getPerson().getId(), true);
         person.setName(this.name);
         person.setEmail(this.email);
         personDAO.makePersistent(person);
         personDAO.flush();
         authenticatedAccount.getPerson().setName(this.name);
         authenticatedAccount.getPerson().setEmail(this.email);
         log.debug("updated successfully");
         return "updated";
      }
      else
      {
         if (username != null)
         {
            createNewUser(username, null, email, name);
            log.debug("create a new user:" + name + " " + email);
            if (Events.exists())
               Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL);
         }
      }
      return "home";
   }

   public String cancel(){
      if (identityStore.isNewUser())
      {
         if (identityStore.isNewUser())
         {
            identity.unAuthenticate();
         }
         return "home";
      }
      return "view";
   }

   public HAccount createNewUser(String username, String password, String email, String name)
   {
      HAccount account = new HAccount();
      account.setUsername(username);
      account.setPasswordHash(password);
      account.setEnabled(true);
      HPerson person = new HPerson();
      person.setName(name);
      person.setEmail(email);
      person.setAccount(account);
      account.setPerson(person);
      accountDAO.makePersistent(account);
      accountDAO.flush();
      return account;
   }

}
