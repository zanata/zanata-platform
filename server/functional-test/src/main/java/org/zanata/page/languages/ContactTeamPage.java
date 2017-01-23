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
package org.zanata.page.languages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ContactTeamPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ContactTeamPage.class);
    private By messageField = By.id(
            "contactCoordinatorForm:messageField:input:contact-coordinator-message");
    private By sendButton = By.id("contact-coordinator-send-button");

    public ContactTeamPage(WebDriver driver) {
        super(driver);
    }

    public ContactTeamPage enterMessage(String message) {
        log.info("Enter message {}", message);
        enterText(readyElement(messageField), message);
        return new ContactTeamPage(getDriver());
    }

    public LanguagesPage clickSend() {
        log.info("Click the Send Message button");
        clickElement(sendButton);
        return new LanguagesPage(getDriver());
    }
}
