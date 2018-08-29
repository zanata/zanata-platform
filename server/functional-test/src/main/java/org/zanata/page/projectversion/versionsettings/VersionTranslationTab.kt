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
package org.zanata.page.projectversion.versionsettings

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.projectversion.VersionBasePage
import java.util.HashMap

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class VersionTranslationTab(driver: WebDriver) : VersionBasePage(driver) {

    private val validationNames = validationMapping

    private val validationMapping: Map<String, String>
        get() {
            val map = HashMap<String, String>()
            map["HTML/XML tags"] = "HTML_XML-"
            map["Java variables"] = "JAVA_VARIABLES-"
            map["Leading/trailing newline (\\n)"] = "NEW_LINE-"
            map["Positional printf (XSI extension)"] = "PRINTF_XSI_EXTENSION-"
            map["Printf variables"] = "PRINTF_VARIABLES-"
            map["Tab characters (\\t)"] = "TAB-"
            map["XML entity reference"] = "XML_ENTITY-"
            return map
        }

    fun isValidationLevel(optionName: String, level: String): Boolean {
        log.info("Query is {}  validation at level {}", optionName, level)
        val optionElementID = validationNames[optionName].toString() + level
        return existingElement(By.id(optionElementID)).getAttribute("checked") == "true"
    }

    fun setValidationLevel(optionName: String,
                           level: String): VersionTranslationTab {
        log.info("Click to set {} validation to {}", optionName, level)
        val optionElementID = validationNames[optionName].toString() + level
        val option = readyElement(By.id("settings-translation-validation-form"))
                .findElement(By.id(optionElementID))
        executor.executeScript("arguments[0].click();", option)
        waitForPageSilence()
        return VersionTranslationTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionTranslationTab::class.java)
    }
}
