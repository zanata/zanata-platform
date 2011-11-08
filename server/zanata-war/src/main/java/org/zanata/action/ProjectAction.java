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

import javax.faces.model.DataModel;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.security.BaseSecurityChecker;

@Name("projectAction")
@Scope(ScopeType.PAGE)
public class ProjectAction extends BaseSecurityChecker implements Serializable
{
   private static final long serialVersionUID = 1L;
   private ProjectPagedListDataModel projectPagedListDataModel = new ProjectPagedListDataModel(false);
   private ProjectPagedListDataModel filteredProjectPagedListDataModel = new ProjectPagedListDataModel(true);

   private int scrollerPage = 1;

   @Logger
   Log log;

   @In
   private ProjectDAO projectDAO;

   @In
   Identity identity;

   private boolean showObsolete = false;

   private HProject securedEntity = null;

   public boolean getEmpty()
   {
      if (checkProjectPermission("view-obsolete") && showObsolete)
      {
         return projectDAO.getProjectSize() == 0;

      }
      else
      {
         return projectDAO.getFilteredProjectSize() == 0;
      }
   }

   public int getPageSize()
   {
      if (checkProjectPermission("view-obsolete") && showObsolete)
      {
         return filteredProjectPagedListDataModel.getPageSize();
      }
      else
      {
         return projectPagedListDataModel.getPageSize();
      }
   }

   public int getScrollerPage()
   {
      return scrollerPage;
   }

   public void setScrollerPage(int scrollerPage)
   {
      this.scrollerPage = scrollerPage;
   }

   public DataModel getProjectPagedListDataModel()
   {
      if (checkProjectPermission("view-obsolete") && showObsolete)
      {
         return filteredProjectPagedListDataModel;
      }
      else
      {
         return projectPagedListDataModel;
      }
   }

   public void markObsolete(HProject project)
   {
      securedEntity = project;
      if (checkPermission("mark-obsolete"))
      {
         projectDAO.makePersistent(project);
         projectDAO.flush();

         if (project.isObsolete())
         {
            FacesMessages.instance().add("Updated {0} to obsolete", project.getName());
         }
         else
         {
            FacesMessages.instance().add("Updated {0} to active", project.getName());
         }
      }
      securedEntity = null;
   }

   public boolean isShowObsolete()
   {
      return showObsolete;
   }
   
   public void setShowObsolete(boolean showObsolete)
   {
      this.showObsolete = showObsolete;
   }

   @Override
   public Object getSecuredEntity()
   {
      return securedEntity;
   }

   /**
    * Check permission with target object name and operation
    * 
    * @param operation
    * @return
    */
   public boolean checkProjectPermission(String operation)
   {
      return identity != null && identity.hasPermission("HProject", operation, null);
   }

}
