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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.faces.model.SelectItem;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;

import com.ibm.icu.util.ULocale;

@Name("languageManagerAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class LanguageManagerAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @In
   private LocaleService localeServiceImpl;
   private String language;
   private ULocale uLocale;
   private List<SelectItem> localeStringList;
   private boolean enabledByDefault;

   // cache this so it is called only once
   private List<LocaleId> allLocales;

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

   public boolean isEnabledByDefault()
   {
      return enabledByDefault;
   }

   public void setEnabledByDefault(boolean enabledByDefault)
   {
      this.enabledByDefault = enabledByDefault;
   }

   public void updateLanguage()
   {
      this.uLocale = new ULocale(this.language);
   }

   public String save()
   {
      LocaleId locale = new LocaleId(language);
      localeServiceImpl.save(locale, enabledByDefault);
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

   public List<HLocale> suggestLocales( Object queryObj )
   {
      final String query = (String)queryObj;

      if( allLocales == null )
      {
         allLocales = localeServiceImpl.getAllJavaLanguages();
      }

      Collection<LocaleId> filtered =
            Collections2.filter(allLocales, new Predicate<LocaleId>()
            {
               @Override
               public boolean apply(@Nullable LocaleId input)
               {
                  return input.getId().startsWith(query);
               }
            });

      /*return new ArrayList<String>(Collections2.transform(filtered, new Function<LocaleId, String>()
      {
         @Override
         public String apply(@Nullable LocaleId from)
         {
            return from.getId();
         }
      }));*/
      return new ArrayList<HLocale>(Collections2.transform(filtered, new Function<LocaleId, HLocale>()
      {
         @Override
         public HLocale apply(@Nullable LocaleId from)
         {
            return new HLocale(from);
         }
      }));
   }

}
