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
    private By languagesBy = By.id("profileLanguages");
    private By contributionsBy = By.id("userProfile-matrix");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Get the display name of the user
     * @return name string
     */
    public String getDisplayName() {
        log.info("Query user\'s display name");
        return getText(displayNameBy);
    }

    /**
     * Get the username of the user
     * @return username string
     */
    public String getUsername() {
        log.info("Query user\'s username");
        return getText(userNameBy);
    }

    /**
     * Get the languages of the user
     * @return languages string
     */
    public String getLanguages() {
        log.info("Query user\'s languages list");
        return getText(languagesBy);
    }

    /**
     * Wait for the contributions matrix to be displayed
     * @return successful display of the matrix
     */
    public boolean expectContributionsMatrixVisible() {
        log.info("Wait for contributions matrix to be visible");
        try {
            waitForAMoment().withMessage("displayed contributions matrix")
                    .until(driver -> driver
                    .findElements(contributionsBy).size() > 0);
        } catch (TimeoutException te) {
            log.info("Contributions matrix was not discovered");
            return false;
        }
        return true;
    }
}
