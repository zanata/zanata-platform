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
import net.openl10n.flies.dao.ProjectIterationDAO;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HProject;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.log.Log;

@Name("iterationLocaleManageAction")
@Scope(ScopeType.PAGE)
public class IterationLocaleManageAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @DataModel
   Set<HLocale> customizedLocales;
   @DataModelSelection
   HLocale selectedLocale;

   private String iterationSlug;
   private String projectSlug;

   private HProjectIteration iteration;

   @In
   ProjectIterationDAO projectIterationDAO;

   @In
   ProjectDAO projectDAO;

   @In
   LocaleService localeServiceImpl;
   @Logger
   Log log;

   public void loadCustomizedLocales()
   {
      iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
      customizedLocales = iteration.getCustomizedLocales();
   }

   public HProjectIteration getProjectIteration()
   {
      return this.iteration;
   }

   public void setProjectIteration(HProjectIteration var)
   {
      this.iteration = var;
   }

   public HProject getProject()
   {
      return projectDAO.getBySlug(projectSlug);
   }

   public HLocale getSelectedLocale()
   {
      return this.selectedLocale;
   }

   public void deleteLocale(HLocale locale)
   {
      iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
      customizedLocales = iteration.getCustomizedLocales();
      customizedLocales.remove(locale);
      projectIterationDAO.makePersistent(iteration);
      projectIterationDAO.flush();
   }

   public String addLocale(String locale)
   {
      iteration = projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
      customizedLocales = iteration.getCustomizedLocales();
      LocaleId localeId = new LocaleId(locale);
      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
      if (hLocale != null)
      {
         customizedLocales.add(hLocale);
         projectIterationDAO.makePersistent(iteration);
         projectIterationDAO.flush();
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

   public void setIterationSlug(String slug)
   {
      this.iterationSlug = slug;
   }

   public String getIterationSlug()
   {
      return this.iterationSlug;
   }

   public String cancel()
   {
      return "cancel";
   }

}
