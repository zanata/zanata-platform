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

import javax.security.auth.login.LoginException;

import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.service.impl.Base64UrlSafe;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("validateEmail")
public class ValidateEmailAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String activationKey;
   @In
   PersonDAO personDAO;

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
         log.info("validate key:" + activationKey);
         String var = Base64UrlSafe.decode(activationKey);
         String[] array = var.split(";");
         String id = array[0];
         String email = array[2];

         HPerson person = personDAO.findById(new Long(id), true);
         person.setEmail(email);
         person.getAccount().setEnabled(true);
         personDAO.makePersistent(person);
         personDAO.flush();
         FacesMessages.instance().add("You have successfully changed your email account.");
         log.info("update email address to " + email + " successfully");
      }
      return "/home.xhtml";
   }

}
