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
import java.util.List;

import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.service.LocaleService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;


@Name("languageSearchAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class LanguageSearchAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @In
   private LocaleService localeServiceImpl;
   @DataModel
   List<HLocale> allLanguages;
   @DataModelSelection
   HLocale selectedLanguage;

   public void loadSupportedLanguage()
   {
      allLanguages = localeServiceImpl.getAllLocales();
   }

   public HLocale getSelectedLanguage()
   {
      return selectedLanguage;
   }

   public void disable(HLocale fliesLocalePair)
   {
      localeServiceImpl.disable(fliesLocalePair.getLocaleId());
      Events.instance().raiseEvent("disableLanguage");
   }

   public void enable(HLocale fliesLocalePair)
   {
      localeServiceImpl.enable(fliesLocalePair.getLocaleId());
      Events.instance().raiseEvent("enableLanguage");
   }

}
