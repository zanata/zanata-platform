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


import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.model.HAccount;

@Name("passwordChange")
@Scope(ScopeType.PAGE)
public class PasswordChangeAction implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @Logger
   Log log;

   @In
   private IdentityManager identityManager;

   private String passwordOld;
   private String passwordNew;
   private String passwordConfirm;

   public void setPasswordOld(String passwordOld)
   {
      this.passwordOld = passwordOld;
   }

   public String getPasswordOld()
   {
      return passwordOld;
   }

   @Begin(join = true)
   public void setPasswordNew(String passwordNew)
   {
      this.passwordNew = passwordNew;
   }

   @NotEmpty
   @Length(min = 6, max = 20)
   // @Pattern(regex="(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$",
   // message="Password is not secure enough!")
   public String getPasswordNew()
   {
      return passwordNew;
   }

   @Begin(join = true)
   public void setPasswordConfirm(String passwordConfirm)
   {
      this.passwordConfirm = passwordConfirm;
      validatePasswordsMatch();
   }

   public String getPasswordConfirm()
   {
      return passwordConfirm;
   }

   public boolean validatePasswordsMatch()
   {
      if (passwordNew == null || !passwordNew.equals(passwordConfirm))
      {
         FacesMessages.instance().addToControl("passwordConfirm", "Passwords do not match");
         return false;
      }
      return true;
   }

   @End
   public String change()
   {
      if (!validatePasswordsMatch())
         return null;

      if (!identityManager.authenticate(authenticatedAccount.getUsername(), passwordOld))
      {
         FacesMessages.instance().addToControl("passwordOld", "Old password is incorrect, please check and try again.");
         return null;
      }

      new RunAsOperation()
      {
         public void execute()
         {
            identityManager.changePassword(authenticatedAccount.getUsername(), getPasswordNew());
         }
      }.addRole("admin").run();

      FacesMessages.instance().add("Your password has been successfully changed.");

      return "/profile/view.xhtml";
   }
}
