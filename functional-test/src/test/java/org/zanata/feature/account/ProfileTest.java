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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.EditProfilePage;
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

    @Rule
    public ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

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

    @Test
    public void changeUsersName() {
        MyAccountPage myAccountPage = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToMyProfile();
        myAccountPage = myAccountPage
                .clickEditProfileButton()
                .enterName("Tranny")
                .clickSaveChanges();
        assertThat("The user's name has been changed",
                myAccountPage.getFullName(),
                Matchers.equalTo("Tranny"));
    }

    @Test
    public void changeUsersEmailAddress() {
        MyAccountPage myAccountPage = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToMyProfile();
        myAccountPage = myAccountPage
                .clickEditProfileButton()
                .enterEmail("anewemail@test.com")
                .clickSaveChanges();
        assertThat("The email link notification is displayed",
                myAccountPage.getNotificationMessage(),
                Matchers.equalTo("You will soon receive an email with a link "+
                        "to activate your email account change."));
    }

    @Test
    public void cancelChangeUsersProfile() {
        MyAccountPage myAccountPage = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToMyProfile();
        myAccountPage = myAccountPage
                .clickEditProfileButton()
                .enterName("Transistor")
                .enterEmail("transistor@test.com")
                .clickCancel();
        assertThat("The user's name has been not changed",
                myAccountPage.getFullName(),
                Matchers.equalTo("translator"));
        assertThat("No email change indication is shown",
                myAccountPage.getNotificationMessage(),
                Matchers.equalTo(""));
    }

    @Test
    public void emailValidationIsUsedOnProfileEdit() {
        EditProfilePage editProfilePage = new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToMyProfile()
                .clickEditProfileButton()
                .enterName("Transistor")
                .enterEmail("admin@example.com")
                .clickSaveAndExpectErrors();

        assertThat("The email is rejected, being already taken",
                editProfilePage.getErrors(),
                Matchers.contains("This email address is already taken"));

        editProfilePage = editProfilePage
                .enterEmail("test @example.com")
                .clickSaveAndExpectErrors();

        assertThat("The email is rejected, being of invalid format",
                editProfilePage.getErrors(),
                Matchers.contains("not a well-formed email address"));
    }
}
