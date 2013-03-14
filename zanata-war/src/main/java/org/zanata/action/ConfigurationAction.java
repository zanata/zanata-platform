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

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.service.ConfigurationService;

@Name("configurationAction")
@Scope(ScopeType.EVENT)
public class ConfigurationAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @RequestParameter
   private String iterationSlug;
   @RequestParameter
   private String projectSlug;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @Logger
   private Log log;
   @In
   private ConfigurationService configurationServiceImpl;

   public void getData()
   {
      getData(false);
   }

   public void getOfflinePoConfigData()
   {
      getData(true);
   }

   private void getData(boolean useOfflinePo)
   {
      HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
      response.setContentType("application/xml");
      response.addHeader("Content-disposition", "attachment; filename=\"" 
                                    + this.configurationServiceImpl.getConfigurationFileName() + "\"");
      response.setCharacterEncoding("UTF-8");
      try
      {
         ServletOutputStream os = response.getOutputStream();

         os.write(
            configurationServiceImpl.getConfigurationFileContents(this.projectSlug, this.iterationSlug, useOfflinePo).getBytes());
         os.flush();
         os.close();
         FacesContext.getCurrentInstance().responseComplete();
      }
      catch (Exception e)
      {
         log.error("Failure : " + e.toString() + "\n");
      }
   }

   public boolean isPoProject()
   {
      ProjectType type = projectIterationDAO.getBySlug(projectSlug, iterationSlug).getProjectType();
      return type == ProjectType.Gettext || type == ProjectType.Podir;
   }
}
