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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import net.openl10n.flies.dao.ProjectDAO;
import net.openl10n.flies.model.HProject;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("localeListAction")
@Scope(ScopeType.PAGE)
public class LocaleListAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   private List<String> customizedLocales = new ArrayList<String>();
   private List<String> availableList = new ArrayList<String>();
   private Map<String, String> globalItems;
   private Map<String, String> availableItems;
   @Out(required = false)
   private Map<String, String> customizedItems;
   @Out(required = false)
   private Boolean overrideLocales;
   private boolean setting;
   @Logger
   Log log;
   @In
   ProjectDAO projectDAO;

   private String slug;

   @In
   LocaleService localeServiceImpl;

   public void toCustomizedLocales()
   {
      if (!availableList.isEmpty())
      {
         for (String op : availableList)
         {
            customizedItems.put(op, op);
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
            customizedItems.remove(op);
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

   public Map<String, String> getAvailableItems()
   {
      return availableItems;
   }
   
   @Factory("customizedItems")
   public void loadCustomizedItems()
   {
      customizedItems = localeServiceImpl.getCustomizedLocalesItems(slug);
      availableItems = new TreeMap<String, String>();
      for (String op : globalItems.keySet())
      {
         if (!customizedItems.containsKey(op))
         {
            availableItems.put(op, op);
         }
      }

   }


   @Create
   public void loadGlobalItems()
   {
      globalItems = localeServiceImpl.getGlobalLocaleItems();
   }

   public String getSlug()
   {
      return slug;
   }

   public void setSlug(String slug)
   {
      this.slug = slug;
   }

   public void setSetting(boolean var)
   {
      setting = var;
      overrideLocales = new Boolean(setting);
   }

   public boolean getSetting()
   {
      if (overrideLocales == null)
      {
         if (slug == null || slug.isEmpty())
         {
            setting = false;
         }
         else
         {
            HProject project = projectDAO.getBySlug(slug);
            setting = project.getOverrideLocales();
         }
         overrideLocales = new Boolean(setting);
      }
      return setting;
   }

}
