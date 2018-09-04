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
import org.openqa.selenium.support.PageFactory
import org.zanata.page.BasePage

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ContactAdminFormPage(driver: WebDriver) : BasePage(driver) {
    private val messageField = By.id("contactAdminForm:messageField:input:contact-admin-message")
    private val sendButton = By.id("contactAdminForm:contact-admin-send-button")
    private val sendAnonButton = By.id("contactAdminForm:contact-admin-send-button-anon")

    fun inputMessage(message: String): ContactAdminFormPage {
        log.info("Enter message {}", message)
        enterText(readyElement(messageField), message)
        return ContactAdminFormPage(driver)
    }

    /**
     * Send the message to the administrator with an active login
     * Requires a page type to return to
     *
     * @param pageClass
     * type of page to return to
     * @return new page type P
     */
    fun <P> send(pageClass: Class<P>): P {
        log.info("Click Send")
        clickElement(sendButton)
        return PageFactory.initElements(driver, pageClass)
    }

    /**
     * Send the message to the administrator, without log in
     * Requires a page type to return to
     *
     * @param pageClass
     * type of page to return to
     * @return new page type P
     */
    fun <P> sendAnonymous(pageClass: Class<P>): P {
        log.info("Click Send")
        clickElement(sendAnonButton)
        return PageFactory.initElements(driver, pageClass)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ContactAdminFormPage::class.java)
    }
}
