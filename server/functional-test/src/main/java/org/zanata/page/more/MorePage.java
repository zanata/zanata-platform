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
package org.zanata.page.more;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MorePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(MorePage.class);
    private By helpLink = By.id("more_help");
    private By contactAdminLink = By.id("more_contact_admin");

    public MorePage(WebDriver driver) {
        super(driver);
    }

    public ContactAdminFormPage clickContactAdmin() {
        log.info("Click Contact Admin button");
        clickElement(contactAdminLink);
        return new ContactAdminFormPage(getDriver());
    }

    public String getHelpURL() {
        log.info("Query Help URL");
        WebElement help_link = existingElement(helpLink);
        return help_link.getAttribute("href");
    }
}
