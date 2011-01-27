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

import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HPerson;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("personHome")
@Scope(ScopeType.CONVERSATION)
public class PersonHome extends EntityHome<HPerson>
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   AccountDAO accountDAO;

   @Logger
   Log log;

   @Override
   public Object getId()
   {
      Object id = super.getId();
      if (id == null && authenticatedAccount != null && authenticatedAccount.getPerson() != null)
      {
         return authenticatedAccount.getPerson().getId();
      }
      return id;
   }

   public void regenerateApiKey()
   {
      accountDAO.createApiKey(getInstance().getAccount());
      getEntityManager().merge(getInstance().getAccount());
      log.info("Reset API key for {0}", getInstance().getAccount().getUsername());
   }

}
