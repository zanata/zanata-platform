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
package org.zanata.feature.account;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.MyAccountPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProfileTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

    private String adminsApiKey = "b6d7044e9ee3b2447c28fb7c50d86d98";

    @Test
    public void verifyProfileData() {
        MyAccountPage myAccountPage =
                new LoginWorkFlow().signIn("admin", "admin").goToMyProfile();

        assertThat("The user's name is displayed in bold",
                myAccountPage.getFullName(), Matchers.equalTo("Administrator"));

        assertThat("The user's username is displayed in smaller bold",
                myAccountPage.getUsername(), Matchers.equalTo("admin"));

        assertThat("The correct api key is present", myAccountPage.getApiKey(),
                Matchers.equalTo(adminsApiKey));

        assertThat(
                "The configuration url is correct",
                myAccountPage.getConfigurationDetails(),
                Matchers.containsString("localhost.url=http://localhost:9898/zanata/"));

        assertThat("The configuration username is correct",
                myAccountPage.getConfigurationDetails(),
                Matchers.containsString("localhost.username=admin"));

        assertThat("The configuration api key is correct",
                myAccountPage.getConfigurationDetails(),
                Matchers.containsString("localhost.key=".concat(adminsApiKey)));
    }

    @Test
    public void changeUsersApiKey() {
        MyAccountPage myAccountPage =
                new LoginWorkFlow().signIn("translator", "translator")
                        .goToMyProfile();
        String currentApiKey = myAccountPage.getApiKey();
        myAccountPage = myAccountPage.pressApiKeyGenerateButton();

        assertThat("The user's api key is different",
                myAccountPage.getApiKey(),
                Matchers.not(Matchers.equalTo(currentApiKey)));

        assertThat("The user's api key is not empty",
                myAccountPage.getApiKey(),
                Matchers.not(Matchers.isEmptyString()));

        assertThat("The configuration api key matches the label",
                myAccountPage.getConfigurationDetails(),
                Matchers.containsString("localhost.key=".concat(myAccountPage
                        .getApiKey())));
    }
}
