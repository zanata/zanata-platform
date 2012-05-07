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
package org.zanata.service.impl;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityNotFoundException;
import org.jboss.seam.log.Log;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.service.UserAccountService;
import org.zanata.util.HashUtil;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("userAccountServiceImpl")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class UserAccountServiceImpl implements UserAccountService
{
   @Logger
   private Log log;

   @In
   private Session session;

   @Override
   public void clearPasswordResetRequests(HAccount account)
   {
      HAccountResetPasswordKey key = (HAccountResetPasswordKey) session.createCriteria(HAccountResetPasswordKey.class).add(Restrictions.naturalId().set("account", account)).uniqueResult();
      if (key != null)
      {
         session.delete(key);
         session.flush();
      }
   }

   @Override
   public HAccountResetPasswordKey requestPasswordReset(HAccount account)
   {
      if (account == null || !account.isEnabled() || account.getPerson() == null)
      {
         return null;
      }

      clearPasswordResetRequests(account);

      HAccountResetPasswordKey key = new HAccountResetPasswordKey();
      key.setAccount(account);
      key.setKeyHash(HashUtil.generateHash(account.getUsername() + account.getPasswordHash() + account.getPerson().getEmail() + account.getPerson().getName() + System.currentTimeMillis()));
      session.persist(key);

      log.info("Sent password reset key to {0} ({1})", account.getPerson().getName(), account.getUsername());
      return key;
   }
}
