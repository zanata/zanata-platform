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
import java.util.Collections;
import java.util.List;

import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HProject;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;

@Name("maintainerProjectList")
@Scope(ScopeType.SESSION)
public class MaintainerProjectList implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   HAccount authenticatedAccount;

   @In
   PersonDAO personDAO;

   @Logger
   Log log;

   private List<HProject> maintainerProjects;

   @Create
   public void onCreate()
   {
      fetchMaintainerProjects();
   }

   public List<HProject> getMaintainerProjects()
   {
      return maintainerProjects;
   }

   @Observer(create = false, value = { "projectAdded", JpaIdentityStore.EVENT_USER_AUTHENTICATED })
   synchronized public void fetchMaintainerProjects()
   {
      log.debug("refreshing projects...");
      if (authenticatedAccount == null)
      {
         maintainerProjects = Collections.emptyList();
         return;
      }

      maintainerProjects = personDAO.getMaintainerProjectByUsername(authenticatedAccount.getUsername());
      log.debug("now listing {0} projects", maintainerProjects.size());
   }
}
