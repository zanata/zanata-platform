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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.page.ManageLanguagePage;

public class LanguageWorkFlow extends AbstractWebWorkFlow
{
   private static final Logger LOGGER = LoggerFactory.getLogger(LanguageWorkFlow.class);

   public ManageLanguagePage addLanguageAndJoin(String localeId)
   {
      ManageLanguagePage manageLanguagePage = addLanguage(localeId);
      manageLanguagePage = manageLanguagePage.manageTeamMembersFor(localeId);
      if (!manageLanguagePage.getMemberUsernames().contains("admin"))
      {
         return manageLanguagePage.joinLanguageTeam();
      }
      else
      {
         LOGGER.warn("admin has already joined the language [{}]", localeId);
         return manageLanguagePage;
      }
   }

   public ManageLanguagePage addLanguage(String localeId)
   {
      return goToHome().goToAdministration().goToManageLanguagePage().addNewLanguage()
            .enableLanguageByDefault().selectLanguage(localeId).saveLanguage();
   }
}
