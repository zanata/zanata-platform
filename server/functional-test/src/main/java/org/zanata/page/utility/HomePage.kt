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
package org.zanata.page.utility

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.administration.EditHomeContentPage

class HomePage(driver: WebDriver) : BasePage(driver) {
    private val mainBodyContent = By.id("home-content-rendered")
    private val editPageContentButton = By.linkText("Edit Page Content")

    fun goToEditPageContent(): EditHomeContentPage {
        log.info("Click Edit Page Content")
        clickElement(editPageContentButton)
        return EditHomeContentPage(driver)
    }

    fun getMainBodyContent(): String {
        log.info("Query homepage content")
        return readyElement(mainBodyContent).text
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(HomePage::class.java)
        const val SIGNUP_SUCCESS_MESSAGE = "You will soon receive an email with a link to activate your account."
        const val EMAILCHANGED_MESSAGE = "Email updated."
    }
}
