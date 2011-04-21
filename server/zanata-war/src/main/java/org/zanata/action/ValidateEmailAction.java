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

import javax.security.auth.login.LoginException;


import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.service.impl.EmailChangeActivationService;
import org.zanata.service.impl.EmailChangeActivationService.KeyParameter;

@Name("validateEmail")
public class ValidateEmailAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String activationKey;
   @In
   PersonDAO personDAO;
   @In
   Identity identity;

   public String getActivationKey()
   {
      return activationKey;
   }

   public void setActivationKey(String activationKey)
   {
      this.activationKey = activationKey;
   }
   @Logger
   Log log;

   @Transactional
   public String validate() throws LoginException
   {
      if (activationKey != null && !activationKey.isEmpty())
      {
         KeyParameter keyPair = EmailChangeActivationService.parseKey(activationKey);

         HPerson person = personDAO.findById(new Long(keyPair.getId()), true);
         HAccount account = person.getAccount();
         if (!account.getUsername().equals(identity.getCredentials().getUsername()))
         {
            throw new LoginException();
         }
         person.setEmail(keyPair.getEmail());
         account.setEnabled(true);
         personDAO.makePersistent(person);
         personDAO.flush();
         FacesMessages.instance().add("You have successfully changed your email account.");
         log.info("update email address to {0}  successfully", keyPair.getEmail());
      }
      return "/home.xhtml";
   }

}
