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
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;


@Name("languageSearchAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class LanguageSearchAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @In
   private LocaleService localeServiceImpl;
   @In
   private LocaleDAO localeDAO;
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
   
   public String manageMembers(HLocale zanataLocalePair)
   {
      return "/language/view/" + zanataLocalePair.getLocaleId().getId();
   }

   public void selectedLocaleChanged()
   {
      selectedLanguage = localeDAO.makePersistent(selectedLanguage);
      localeDAO.flush();
   }

   public void selectedLocaleActivatedOrDeactivated()
   {
      selectedLanguage = localeDAO.makePersistent(selectedLanguage);
      localeDAO.flush();

      if( selectedLanguage.isActive() )
      {
         Events.instance().raiseEvent("enableLanguage");
      }
      else
      {
         Events.instance().raiseEvent("disableLanguage");
      }
   }

}
