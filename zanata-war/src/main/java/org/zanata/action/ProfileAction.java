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
package org.zanata.action;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.ZanataJpaIdentityStore;
import org.zanata.security.ZanataOpenId;
import org.zanata.service.RegisterService;
import org.zanata.service.impl.EmailChangeService;

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
   private String activationKey;
   private boolean valid;

   @In
   ApplicationConfiguration applicationConfiguration;
   
   @Logger
   Log log;

   @In
   ZanataIdentity identity;

   @In
   ZanataJpaIdentityStore identityStore;

   @In
   private ZanataOpenId zanataOpenId;

   @In(create = true)
   private Renderer renderer;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   PersonDAO personDAO;

   @In
   AccountDAO accountDAO;

   @In
   RegisterService registerServiceImpl;
   
   @In
   EmailChangeService emailChangeService;

   private void validateEmail(String email)
   {
      HPerson person = personDAO.findByEmail(email);
      
      if( person != null && !person.getAccount().equals( authenticatedAccount ) )
      {
         valid = false;
         FacesMessages.instance().addToControl("email", "This email address is already taken");
      }
   }

   private void validateUsername()
   {
      HAccount account = accountDAO.getByUsername(this.username);

      if( account != null && !account.equals( authenticatedAccount ) )
      {
         valid = false;
         FacesMessages.instance().addToControl("username", "This username is already taken");
      }
   }

   @Create
   public void onCreate()
   {
      username = identity.getCredentials().getUsername();
      if (identityStore.isNewUser(username))
      {
         name = identity.getCredentials().getUsername();
         String domain = applicationConfiguration.getDomainName();
         if( domain == null )
         {
            email = "";
         }
         else 
         {
            if( applicationConfiguration.isOpenIdAuth() )
            {
               email = zanataOpenId.getAuthResult().getEmail();
            }
            else
            {
               email = identity.getCredentials().getUsername() + "@" + domain;
            }
         }
      }
      else
      {
         HPerson person = personDAO.findById(authenticatedAccount.getPerson().getId(), false);
         name = person.getName();
         email = person.getEmail();
         authenticatedAccount.getPerson().setName(this.name);
         authenticatedAccount.getPerson().setEmail(this.email);
      }
   }

   @NotEmpty
   @Size(min = 2, max = 80)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Email
   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.validateEmail(email);
      this.email = email;
   }

   @NotEmpty
   @Size(min = 3, max = 20)
   @Pattern(regexp = "^[a-z\\d_]{3,20}$")
   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
      validateUsername();
   }

   public String getActivationKey()
   {
      return activationKey;
   }

   public void setActivationKey(String keyHash)
   {
      this.activationKey = keyHash;
   }

   @Transactional
   public String edit()
   {
      this.valid = true;
      validateEmail(this.email);
      validateUsername();

      if( !this.isValid() )
      {
         return null;
      }

      if (authenticatedAccount != null)
      {
         HPerson person = personDAO.findById(authenticatedAccount.getPerson().getId(), true);
         person.setName(this.name);
         personDAO.makePersistent(person);
         personDAO.flush();
         authenticatedAccount.getPerson().setName(this.name);
         log.debug("updated successfully");
         if (!authenticatedAccount.getPerson().getEmail().equals(this.email))
         {
            activationKey = emailChangeService.generateActivationKey(person, this.email);
            renderer.render("/WEB-INF/facelets/email/email_validation.xhtml");
            FacesMessages.instance().add("You will soon receive an email with a link to activate your email account change.");
         }

         return "updated";
      }
      else
      {

         String key;
         if (identity.getCredentials().getAuthType() == AuthenticationType.KERBEROS || identity.getCredentials().getAuthType() == AuthenticationType.JAAS)
         {
            key = registerServiceImpl.register(this.username, this.username, this.email);
         }
         else
         {
            key = registerServiceImpl.register(this.username, zanataOpenId.getAuthResult().getAuthenticatedId(),
                  AuthenticationType.OPENID, this.name, this.email);
         }
         setActivationKey(key);
         renderer.render("/WEB-INF/facelets/email/email_activation.xhtml");
         FacesMessages.instance().add("You will soon receive an email with a link to activate your account.");

         return "home";
      }
   }

   public String cancel()
   {
      if (identityStore.isNewUser(username))
      {
         return "home";
      }
      return "view";
   }

   public boolean isValid()
   {
      return valid;
   }

   public boolean isNewUser()
   {
      return identityStore.isNewUser(username);
   }

}
