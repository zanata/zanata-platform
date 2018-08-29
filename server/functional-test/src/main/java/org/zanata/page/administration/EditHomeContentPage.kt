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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.utility.HomePage

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class EditHomeContentPage(driver: WebDriver) : BasePage(driver) {

    private val updateButton = By.id("homeContentForm:update")
    private val cancelButton = By.id("homeContentForm:cancel")
    private val homeContent = By.id("homeContentForm:homeContent")

    /**
     * Enter text into the content editing panel
     * @param text string to enter
     * @return new EditHomeContentPage
     */
    fun enterText(text: String): EditHomeContentPage {
        log.info("Enter homepage code\n{}", text)
        enterText(homeContent, text)
        return EditHomeContentPage(driver)
    }

    /**
     * Press the Update button
     * @return new HomePage
     */
    fun update(): HomePage {
        log.info("Click Update")
        clickElement(updateButton)
        return HomePage(driver)
    }

    /**
     * Press the Cancel button
     * @return new HomePage
     */
    @Suppress("unused")
    fun cancelUpdate(): HomePage {
        log.info("Click Cancel")
        clickElement(cancelButton)
        return HomePage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(EditHomeContentPage::class.java)
    }
}
