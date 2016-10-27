/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.language;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.subethamail.wiser.WiserMessage;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.languages.LanguagePage;
import org.zanata.util.HasEmailRule;
import org.zanata.workflow.LoginWorkFlow;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.HasEmailRule.getEmailContent;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class JoinLanguageTeamTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule hasEmailRule = new HasEmailRule();

    @Feature(summary = "The administrator can add a member to a language team",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 181703)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void translatorJoinsLanguageTeam() throws Exception {
        LanguagePage languagePage = new LoginWorkFlow()
                .signIn("admin", "admin").goToLanguages()
                .gotoLanguagePage("pl")
                .gotoMembersTab()
                .clickAddTeamMember()
                .searchPersonAndAddToTeam("translator",
                        LanguagePage.TeamPermission.Translator,
                        LanguagePage.TeamPermission.Reviewer);

        assertThat(languagePage.getMemberUsernames())
                .contains("translator")
                .as("Translator is a listed member of the pl team");
        assertThat(hasEmailRule.emailsArrivedWithinTimeout(1, 30,
                TimeUnit.SECONDS))
                .as("The email arrived within thirty seconds")
                .isTrue();
        WiserMessage emailMessage = hasEmailRule.getMessages().get(0);
        assertThat(getEmailContent(emailMessage))
                .contains("Administrator has changed your permissions");
    }
}
