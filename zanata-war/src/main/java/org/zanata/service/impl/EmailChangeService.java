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
package org.zanata.service.impl;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.PersonEmailValidationKeyDAO;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonEmailValidationKey;
import org.zanata.util.HashUtil;

@Name("emailChangeService")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class EmailChangeService
{
   @In
   PersonEmailValidationKeyDAO personEmailValidationKeyDAO;

   public static class KeyParameter
   {
      private String id;
      private String email;
      private String creationDate;

      public KeyParameter(String id, String email, String creationDate)
      {
         this.id = id;
         this.email = email;
         this.creationDate = creationDate;
      }

      public String getId()
      {
         return id;
      }

      public String getEmail()
      {
         return email;
      }

      public String getCreationDate()
      {
         return creationDate;
      }
   }

   public String generateActivationKey(HPerson person, String email)
   {
      String var = person.getId() + email + System.currentTimeMillis();
      String hash = HashUtil.generateHash(var);

      HPersonEmailValidationKey entry = personEmailValidationKeyDAO.findById(person.getId(), false);
      if (entry == null)
      {
         entry = new HPersonEmailValidationKey(person, email, hash, new Date());
      }
      else
      {
         entry.setCreationDate(new Date());
         entry.setEmail(email);
         entry.setKeyHash(hash);
      }

      personEmailValidationKeyDAO.makePersistent(entry);
      personEmailValidationKeyDAO.flush();

      return hash;
   }

   public HPersonEmailValidationKey getActivationKey(String keyHash)
   {
      return personEmailValidationKeyDAO.findByKeyHash(keyHash);
   }

   public boolean isExpired(Date creationDate, int activeDays) throws ParseException
   {
      Date expiryDate = DateUtils.addDays(creationDate, activeDays);
      return expiryDate.before(new Date());
   }


   public void removeEntry(HPersonEmailValidationKey entry)
   {
      personEmailValidationKeyDAO.makeTransient(entry);
      personEmailValidationKeyDAO.flush();
   }
}
