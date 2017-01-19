/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.account;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProfilePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProfilePage.class);
    private By displayNameBy = By.id("profile-displayname");
    private By userNameBy = By.id("profile-username");
    private By languagesBy = By.id("profile-languages");
    private By contributionsBy = By.id("profile-matrix");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public String getDisplayName() {
        log.info("Query user\'s display name");
        return readyElement(displayNameBy).getText();
    }

    public String getUsername() {
        log.info("Query user\'s username");
        return readyElement(userNameBy).getText();
    }

    public String getLanguages() {
        log.info("Query user\'s languages list");
        return readyElement(languagesBy).getText();
    }

    public boolean expectContributionsMatrixVisible() {
        log.info("Wait for contributions matrix to be visible");
        try {
            waitForAMoment().until((Predicate<WebDriver>) webDriver -> webDriver
                    .findElements(contributionsBy).size() > 0);
        } catch (TimeoutException te) {
            log.info("Contributions matrix was not discovered");
            return false;
        }
        return true;
    }
}
