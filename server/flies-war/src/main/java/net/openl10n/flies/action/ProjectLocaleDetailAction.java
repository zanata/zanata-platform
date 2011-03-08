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

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.ibm.icu.util.ULocale;

import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.service.LocaleService;

@Name("projectLocaleDetailAction")
@Scope(ScopeType.PAGE)
public class ProjectLocaleDetailAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @In
   private LocaleService localeServiceImpl;
   private String language;
   private ULocale uLocale;
   private List<SelectItem> localeStringList;

   @Create
   public void onCreate()
   {
      fetchLocaleFromSupportedLanguage();
   }

   public String getLanguage()
   {
      return language;
   }

   public ULocale getuLocale()
   {
      return uLocale;
   }

   public void setuLocale(ULocale uLocale)
   {
      this.uLocale = uLocale;
   }

   public void setLanguage(String language)
   {
      this.language = language;
   }

   public void updateLanguage()
   {
      this.uLocale = new ULocale(this.language);
   }

   public void fetchLocaleFromSupportedLanguage()
   {
      List<HLocale> locale = localeServiceImpl.getSupportedLocales();
      List<SelectItem> localeList = new ArrayList<SelectItem>();
      for (HLocale var : locale)
      {
         SelectItem op = new SelectItem(var.getLocaleId().getId(), var.getLocaleId().getId());
         localeList.add(op);
      }
      localeStringList = localeList;
   }

   public List<SelectItem> getLocaleStringList()
   {
      return localeStringList;
   }

}
