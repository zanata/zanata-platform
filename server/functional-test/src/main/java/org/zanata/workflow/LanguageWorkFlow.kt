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
package org.zanata.workflow

import org.zanata.page.languages.LanguagePage
import org.zanata.page.languages.LanguagesPage

class LanguageWorkFlow : AbstractWebWorkFlow() {

    fun addLanguageAndJoin(localeId: String): LanguagePage {
        addLanguage(localeId)
        val languagePage = goToHome().goToLanguages()
                .gotoLanguagePage(localeId).gotoMembersTab()
        if (languagePage.memberUsernames.contains("admin")) {
            log.warn("admin has already joined the language [{}]", localeId)
            return languagePage
        }
        return languagePage.joinLanguageTeam()
    }

    private fun addLanguage(localeId: String): LanguagesPage {
        val languagesPage = goToHome().goToLanguages()
        val locales = languagesPage.languageLocales
        if (locales.contains(localeId)) {
            log.warn("{} has already been added, enabling by default",
                    localeId)
            languagesPage.gotoLanguagePage(localeId).gotoSettingsTab()
                    /*.enableLanguageByDefault(true)*/
                    .saveSettings()
            return goToHome().goToLanguages()
        }
        // continue to add the new language
        return languagesPage.clickAddNewLanguage()
                .enterSearchLanguage(localeId)
                .selectSearchLanguage(localeId)
                /*.enableLanguageByDefault()*/
                .saveLanguage()
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(LanguageWorkFlow::class.java)
    }
}
