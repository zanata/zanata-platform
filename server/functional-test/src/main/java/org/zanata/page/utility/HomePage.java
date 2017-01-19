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
package org.zanata.page.utility;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.page.administration.EditHomeContentPage;

public class HomePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(HomePage.class);
    public static final String SIGNUP_SUCCESS_MESSAGE =
            "You will soon receive an email with a link to activate your account.";
    public static final String EMAILCHANGED_MESSAGE = "Email updated.";
    private By mainBodyContent = By.id("home-content-rendered");
    private By editPageContentButton = By.linkText("Edit Page Content");

    public HomePage(final WebDriver driver) {
        super(driver);
    }

    public EditHomeContentPage goToEditPageContent() {
        log.info("Click Edit Page Content");
        clickElement(editPageContentButton);
        return new EditHomeContentPage(getDriver());
    }

    public String getMainBodyContent() {
        log.info("Query homepage content");
        return readyElement(mainBodyContent).getText();
    }
}
