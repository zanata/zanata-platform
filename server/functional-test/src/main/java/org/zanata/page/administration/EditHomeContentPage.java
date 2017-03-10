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
package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EditHomeContentPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(EditHomeContentPage.class);
    private By updateButton = By.id("homeContentForm:update");
    private By cancelButton = By.id("homeContentForm:cancel");

    public EditHomeContentPage(final WebDriver driver) {
        super(driver);
    }

    public EditHomeContentPage enterText(String text) {
        log.info("Enter homepage code\n{}", text);
        readyElement(By.id("homeContentForm:homeContent")).sendKeys(text);
        return new EditHomeContentPage(getDriver());
    }

    public HomePage update() {
        log.info("Click Update");
        clickElement(updateButton);
        return new HomePage(getDriver());
    }

    public HomePage cancelUpdate() {
        log.info("Click Cancel");
        clickElement(cancelButton);
        return new HomePage(getDriver());
    }
}
