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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProjectIteration;
import org.zanata.service.LocaleService;

@Name("projectIterationLocaleAction")
@Scope(ScopeType.PAGE)
public class ProjectIterationLocaleAction implements Serializable
{

   private static final long serialVersionUID = 1L;
   private List<String> customizedLocales = new ArrayList<String>();
   private List<String> availableList = new ArrayList<String>();
   private Map<String, String> globalItems;
   private Map<String, String> availableItems;
   @Out(required = false)
   private Map<String, String> iterationCustomizedItems;
   @Out(required = false)
   private Boolean iterationOverrideLocales;
   private boolean setting;
   @Logger
   Log log;
   @In
   ProjectIterationDAO projectIterationDAO;

   private String projectSlug;

   private String iterationSlug;

   @In
   LocaleService localeServiceImpl;

   public void toCustomizedLocales()
   {
      if (!availableList.isEmpty())
      {
         for (String op : availableList)
         {
            iterationCustomizedItems.put(op, op);
            availableItems.remove(op);
         }
      }
   }

   public void removeFromCustomizedLocales()
   {
      if (!customizedLocales.isEmpty())
      {
         for (String op : customizedLocales)
         {
            iterationCustomizedItems.remove(op);
            availableItems.put(op, op);
         }
      }

   }

   public List<String> getCustomizedLocales()
   {
      return customizedLocales;
   }

   public void setCustomizedLocales(List<String> var)
   {
      customizedLocales = var;
   }

   public List<String> getAvailableList()
   {
      return availableList;
   }

   public void setAvailableList(List<String> var)
   {
      availableList = var;
   }

   public Map<String, String> getIterationCustomizedItems()
   {
      return iterationCustomizedItems;
   }

   @Factory("iterationAvailableItems")
   public Map<String, String> loadItems()
   {
      log.debug("load iterationCustomizedItems");
      availableItems = new TreeMap<String, String>();
      iterationCustomizedItems = localeServiceImpl.getIterationCustomizedLocalesItems(projectSlug, iterationSlug);
      globalItems = localeServiceImpl.getIterationGlobalLocaleItems(projectSlug);
      if (iterationCustomizedItems.isEmpty())
      {
         iterationCustomizedItems = globalItems;
      }
      else
      {
         for (String op : globalItems.keySet())
         {
            if (!iterationCustomizedItems.containsKey(op))
            {
               availableItems.put(op, op);
            }
         }
      }
      return availableItems;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getIterationSlug()
   {
      return iterationSlug;
   }

   public void setIterationSlug(String iterationSlug)
   {
      this.iterationSlug = iterationSlug;
   }

   public void setSetting(boolean var)
   {
      setting = var;
      iterationOverrideLocales = new Boolean(setting);
   }

   public boolean getSetting()
   {
      if (iterationOverrideLocales == null)
      {
         if (projectSlug == null || iterationSlug == null)
         {
            setting = false;
         }
         else
         {
            HProjectIteration project = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
            setting = project.getOverrideLocales();
         }
         iterationOverrideLocales = new Boolean(setting);
      }
      return setting;
   }

}
