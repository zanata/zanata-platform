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
package org.zanata.workflow;

import java.util.List;

import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.administration.ManageLanguageTeamMemberPage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LanguageWorkFlow extends AbstractWebWorkFlow
{

   public void addLanguageAndJoin(String localeId)
   {
      ManageLanguagePage manageLanguagePage = addLanguage(localeId);
      ManageLanguageTeamMemberPage teamMemberPage = manageLanguagePage.manageTeamMembersFor(localeId);
      if (!teamMemberPage.getMemberUsernames().contains("admin"))
      {
         teamMemberPage.joinLanguageTeam();
      }
      else
      {
         log.warn("admin has already joined the language [{}]", localeId);
      }
   }

   public ManageLanguagePage addLanguage(String localeId)
   {
      ManageLanguagePage manageLanguagePage = goToHome().goToAdministration().goToManageLanguagePage();
      List<String> locales = manageLanguagePage.getLanguageLocales();
      if (locales.contains(localeId))
      {
         log.warn("{} has already been added, enabling by default", localeId);
         return manageLanguagePage.enableLanguageByDefault(localeId);
      }
      //continue to add the new language
      return manageLanguagePage.addNewLanguage().enableLanguageByDefault().inputLanguage(localeId).saveLanguage();
   }
}
