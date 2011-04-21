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
package org.zanata.action;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;


import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Pattern;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.zanata.model.HPerson;
import org.zanata.service.RegisterService;

@Name("register")
@Scope(ScopeType.CONVERSATION)
public class RegisterAction implements Serializable
{

   private static final long serialVersionUID = -7883627570614588182L;

   @Logger
   Log log;

   @In
   private EntityManager entityManager;

   @In
   RegisterService registerServiceImpl;

   @In(create = true)
   private Renderer renderer;

   private String username;
   private String password;
   private String passwordConfirm;

   private boolean agreedToTermsOfUse;

   private boolean valid;

   private HPerson person;

   private String activationKey;

   @Begin(join = true)
   public HPerson getPerson()
   {
      if (person == null)
         person = new HPerson();
      return person;
   }

   public void setUsername(String username)
   {
      validateUsername(username);
      this.username = username;
   }

   @NotEmpty
   @Length(min = 3, max = 20)
   @Pattern(regex = "^[a-z\\d_]{3,20}$")
   public String getUsername()
   {
      return username;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   @NotEmpty
   @Length(min = 6, max = 20)
   // @Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$",
   // message="Password is not secure enough!")
   public String getPassword()
   {
      return password;
   }

   public void setPasswordConfirm(String passwordConfirm)
   {
      validatePasswords(getPassword(), passwordConfirm);
      this.passwordConfirm = passwordConfirm;
   }

   public String getPasswordConfirm()
   {
      return passwordConfirm;
   }

   public boolean isAgreedToTermsOfUse()
   {
      return agreedToTermsOfUse;
   }

   public void setAgreedToTermsOfUse(boolean agreedToTermsOfUse)
   {
      this.agreedToTermsOfUse = agreedToTermsOfUse;
   }

   public void validateUsername(String username)
   {
      try
      {
         entityManager.createQuery("from HAccount a where a.username = :username").setParameter("username", username).getSingleResult();
         valid = false;
         FacesMessages.instance().addToControl("username", "This username is not available");
      }
      catch (NoResultException e)
      {
         // pass
      }
   }

   public void validatePasswords(String p1, String p2)
   {

      if (p1 == null || !p1.equals(p2))
      {
         valid = false;
         FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
      }

   }

   public void validateTermsOfUse()
   {
      if (!isAgreedToTermsOfUse())
      {
         valid = false;
         FacesMessages.instance().addToControl("agreedToTerms", "You must accept the Terms of Use");
      }
   }

   @End
   public String register()
   {
      valid = true;
      validateUsername(getUsername());
      validatePasswords(getPassword(), getPasswordConfirm());
      validateTermsOfUse();

      if (!isValid())
      {
         return null;
      }
      final String user = getUsername();
      final String pass = getPassword();
      String key = registerServiceImpl.register(user, pass, getPerson().getName(), getPerson().getEmail());
      setActivationKey(key);
      log.info("get register key:" + key);

      renderer.render("/WEB-INF/facelets/email/activation.xhtml");

      FacesMessages.instance().add("You will soon receive an email with a link to activate your account.");

      return "/home.xhtml";
   }

   public String getActivationKey()
   {
      return activationKey;
   }

   @Begin(join = true)
   public void setActivationKey(String activationKey)
   {
      this.activationKey = activationKey;
   }

   public boolean isValid()
   {
      return valid;
   }

}
