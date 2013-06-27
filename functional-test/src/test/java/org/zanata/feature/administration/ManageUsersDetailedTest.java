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
package org.zanata.feature.administration;

import org.junit.*;
import org.zanata.page.HomePage;
import org.zanata.page.administration.ManageUserPage;
import org.zanata.page.administration.ManageUserAccountPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;
import org.hamcrest.Matchers;
import static org.hamcrest.MatcherAssert.assertThat;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ManageUsersDetailedTest
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule(ResetDatabaseRule.Config.Empty);
   private HomePage homePage;

   @Before
   public void before()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   @Test
   public void changeAUsersUsername()
   {
      String username = "administratornamechange";
      ManageUserPage manageUserPage = homePage.goToAdministration().goToManageUserPage();
      ManageUserAccountPage manageUserAccountPage = manageUserPage.editUserAccount("admin");
      manageUserPage = manageUserAccountPage.clearFields().enterUsername(username).saveUser();
      assertThat("Administrator is displayed", manageUserPage.getUserList(), Matchers.hasItem(username));
   }

}
