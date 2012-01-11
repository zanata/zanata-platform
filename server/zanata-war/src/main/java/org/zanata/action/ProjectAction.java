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

   private ProjectPagedListDataModel filteredProjectPagedListDataModel = new ProjectPagedListDataModel("obsolete");
   private ProjectPagedListDataModel projectPagedListDataModel = new ProjectPagedListDataModel(null);

   private int scrollerPage = 1;

   @Logger
   Log log;

   @In
   private ProjectDAO projectDAO;

   @In
   Identity identity;
   
   private boolean showObsolete = false;
   private boolean showCurrent = true;
   private boolean showRetired = true;

   private HProject securedEntity = null;

   public boolean getEmpty()
   {
      if (checkViewObsolete() && showObsolete)
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
      if (checkViewObsolete() && showObsolete)
      {
         return projectPagedListDataModel.getPageSize();
      }
      else
      {
         return filteredProjectPagedListDataModel.getPageSize();
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
      if (checkViewObsolete() && showObsolete)
      {
         return projectPagedListDataModel;
      }
      else
      {
         return filteredProjectPagedListDataModel;
      }
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
   public boolean checkViewObsolete()
   {
      return identity != null && identity.hasPermission("HProject", "view-obsolete", null);
   }
   
   public boolean isShowObsolete()
   {
      return showObsolete;
   }
   
   public void setShowObsolete(boolean showObsolete)
   {
      this.showObsolete = showObsolete;
   }

   public boolean isShowCurrent()
   {
      return showCurrent;
   }

   public void setShowCurrent(boolean showCurrent)
   {
      this.showCurrent = showCurrent;
   }

   public boolean isShowRetired()
   {
      return showRetired;
   }

   public void setShowRetired(boolean showRetired)
   {
      this.showRetired = showRetired;
   }

}
