/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.startNewProject;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.page.HomePage;
import org.zanata.page.administration.ManageLanguagePage;
import org.zanata.page.administration.ManageLanguageTeamMemberPage;
import org.zanata.util.TableRow;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ScreenshotExtension.class, TimestampFormatterExtension.class, CustomResourceExtension.class})
@Category(ConcordionTest.class)
public class TranslatorJoinsLanguageTeamTest
{
   private HomePage homePage;
   private ManageLanguagePage manageLanguagePage;
   private ManageLanguageTeamMemberPage manageLanguageTeamMemberPage;

   @Before
   public void beforeMethod()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   public void goToManageLanguagePage()
   {
      manageLanguagePage = homePage.goToAdministration().goToManageLanguagePage();
   }

   public ManageLanguageTeamMemberPage manageLanguage(String locale)
   {
      manageLanguageTeamMemberPage = manageLanguagePage.manageTeamMembersFor(locale);
      return manageLanguageTeamMemberPage;
   }

   public ManageLanguageTeamMemberPage addToLanguage(String person)
   {
      ManageLanguageTeamMemberPage teamMemberPage = manageLanguageTeamMemberPage.clickAddTeamMember();
      List<TableRow> searchResult = teamMemberPage.searchPerson(person);
      return teamMemberPage.addToTeam(searchResult.get(0));
   }

}
