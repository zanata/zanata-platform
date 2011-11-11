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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProjectIteration;
import org.zanata.security.BaseSecurityChecker;

@Name("projectIterationAction")
@Scope(ScopeType.PAGE)
public class ProjectIterationAction extends BaseSecurityChecker implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Logger
   Log log;

   @In
   Identity identity;

   @In
   private ProjectIterationDAO projectIterationDAO;

   private HProjectIteration securedEntity = null;

   public void markProjectIterationObsolete(HProjectIteration projectIteration)
   {
      securedEntity = projectIteration;
      if (checkPermission("mark-obsolete"))
      {
         projectIteration.setObsolete(true);

         projectIterationDAO.makePersistent(projectIteration);
         projectIterationDAO.flush();
         FacesMessages.instance().add("Marked iteration {0} as obsolete", projectIteration.getSlug());
      }
   }

   public void markProjectIterationCurrent(HProjectIteration projectIteration)
   {
      securedEntity = projectIteration;
      if (checkPermission("mark-obsolete"))
      {
         projectIteration.setObsolete(false);

         projectIterationDAO.makePersistent(projectIteration);
         projectIterationDAO.flush();
         FacesMessages.instance().add("Marked iteration {0} as current", projectIteration.getSlug());
      }
   }

   public boolean checkViewObsolete()
   {
      return Identity.instance() != null && Identity.instance().hasPermission("HProjectIteration", "view-obsolete", null);
   }

   public boolean checkViewObsoleteOption()
   {
      return Identity.instance() != null && Identity.instance().hasPermission("HProjectIteration", "view-obsolete-option", null);
   }

   @Override
   public Object getSecuredEntity()
   {
      return securedEntity;
   }
}
