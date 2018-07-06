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
package org.zanata.feature.language

import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.languages.LanguagePage
import org.zanata.workflow.LoginWorkFlow

import java.util.concurrent.TimeUnit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.zanata.util.HasEmailExtension
import org.zanata.util.HasEmailExtension.getEmailContent

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
@ExtendWith(HasEmailExtension::class)
class JoinLanguageTeamTest : ZanataTestCase() {

    @Trace(summary = "The administrator can add a member to a language team")
    @Test
    fun translatorJoinsLanguageTeam() {
        val languagePage = LoginWorkFlow()
                .signIn("admin", "admin")
                .goToLanguages()
                .gotoLanguagePage("pl")
                .gotoMembersTab()
                .clickAddTeamMember()
                .searchPersonAndAddToTeam("translator",
                        LanguagePage.TeamPermission.Translator,
                        LanguagePage.TeamPermission.Reviewer)

        assertThat(languagePage.memberUsernames)
                .describedAs("Translator is a listed member of the pl team")
                .contains("translator")
        assertThat(hasEmailExtension.emailsArrivedWithinTimeout(1, 30,
                TimeUnit.SECONDS))
                .describedAs("The email arrived within thirty seconds")
                .isTrue()
        val emailMessage = hasEmailExtension.getMessages().get(0)
        assertThat(getEmailContent(emailMessage))
                .contains("Administrator has changed your permissions")
    }
}
