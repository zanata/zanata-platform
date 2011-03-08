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
import java.util.Set;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.ProjectDAO;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HProject;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.log.Log;

@Name("projectLocaleManageAction")
@Scope(ScopeType.PAGE)
public class ProjectLocaleManageAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @DataModel
   Set<HLocale> customizedLocales;
   @DataModelSelection
   HLocale selectedLocale;

   private String projectSlug;
   private HProject project;

   @In
   ProjectDAO projectDAO;
   @In
   LocaleService localeServiceImpl;
   @Logger
   Log log;


   public void loadCustomizedLocales()
   {
      project = projectDAO.getBySlug(this.projectSlug);
      customizedLocales = project.getCustomizedLocales();
   }

   public HProject getProject()
   {
      if (project == null)
      {
         project = projectDAO.getBySlug(this.projectSlug);
      }
      return this.project;
   }

   public void setProject(HProject var)
   {
      this.project = var;
   }

   public HLocale getSelectedLocale()
   {
      return this.selectedLocale;
   }

   public void deleteLocale(HLocale locale)
   {
      customizedLocales.remove(locale);
      projectDAO.makePersistent(project);
      projectDAO.flush();
   }

   public String addLocale(String locale)
   {
      project = projectDAO.getBySlug(this.projectSlug);
      customizedLocales = project.getCustomizedLocales();
      LocaleId localeId =new LocaleId(locale);
      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
      if(hLocale != null){
         customizedLocales.add(hLocale);
         projectDAO.makePersistent(project);
         projectDAO.flush();
      }
      return "success";
   }

   public void setProjectSlug(String slug)
   {
      this.projectSlug = slug;
   }

   public String getProjectSlug()
   {
      return this.projectSlug;
   }

   public String cancel()
   {
      return "cancel";
   }


}
