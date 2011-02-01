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
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.ibm.icu.util.ULocale;

@Name("languageManagerAction")
@Scope(ScopeType.EVENT)
@Restrict("#{s:hasRole('admin')}")
public class LanguageManagerAction implements Serializable
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
      fectchLocaleFromJava();
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

   public String save()
   {
      LocaleId locale = new LocaleId(language);
      localeServiceImpl.save(locale);
      return "success";
   }

   public void fectchLocaleFromJava()
   {
      List<LocaleId> locale = localeServiceImpl.getAllJavaLanguages();
      List<SelectItem> localeList = new ArrayList<SelectItem>();
      for (LocaleId var : locale)
      {
         SelectItem op = new SelectItem(var.getId(), var.getId());
         localeList.add(op);
      }
      localeStringList = localeList;
   }

   public List<SelectItem> getLocaleStringList()
   {
      return localeStringList;
   }

}
