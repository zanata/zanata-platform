/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.more

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class MorePage(driver: WebDriver) : BasePage(driver) {
    private val helpLink = By.id("more_help")
    private val contactAdminLink = By.id("more_contact_admin")

    val helpURL: String
        get() {
            log.info("Query Help URL")
            val helpLink = existingElement(helpLink)
            return helpLink.getAttribute("href")
        }

    fun clickContactAdmin(): ContactAdminFormPage {
        log.info("Click Contact Admin button")
        clickElement(contactAdminLink)
        return ContactAdminFormPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(MorePage::class.java)
    }
}
