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

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.utility.DashboardPage;
import org.zanata.page.utility.HomePage;
import org.zanata.page.administration.EditHomeCodePage;
import org.zanata.page.administration.EditHomeContentPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditHomePageTest {
    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

    @Test
    @Ignore("Cannot access the editor via WebDriver")
    public void goToEditPageContent() {
        DashboardPage dashboard = new LoginWorkFlow().signIn("admin", "admin");
        EditHomeContentPage editHomeContentPage =
                dashboard.goToHomePage().goToEditPageContent();

        assertThat("Correct page", editHomeContentPage.getTitle(),
                Matchers.equalTo("Zanata: Edit Home Page"));
        editHomeContentPage = editHomeContentPage.enterText("Test");
        HomePage homePage = editHomeContentPage.update();
        editHomeContentPage = homePage.goToEditPageContent();
        editHomeContentPage.cancelUpdate();
    }

    @Test
    public void goToEditPageCode() {
        DashboardPage dashboard = new LoginWorkFlow().signIn("admin", "admin");
        EditHomeCodePage editHomeCodePage =
                dashboard.goToHomePage().goToEditPageCode();

        assertThat("Correct page", editHomeCodePage.getTitle(),
                Matchers.equalTo("Zanata: Edit Page Code"));
        HomePage homePage = editHomeCodePage.enterText("Test").update();
        assertThat("Message displayed", homePage.getNotificationMessage(),
                Matchers.equalTo("Home content was successfully updated."));
        editHomeCodePage = homePage.goToEditPageCode();
        homePage = editHomeCodePage.cancelUpdate();
        assertThat("Homepage text has been updated",
                homePage.getMainBodyContent(), Matchers.equalTo("Test"));
    }
}
