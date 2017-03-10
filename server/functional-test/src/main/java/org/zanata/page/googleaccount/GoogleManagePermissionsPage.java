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
package org.zanata.page.googleaccount;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.zanata.page.AbstractPage;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class GoogleManagePermissionsPage extends AbstractPage {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(GoogleManagePermissionsPage.class);

    public GoogleManagePermissionsPage(WebDriver driver) {
        super(driver);
    }

    public GoogleManagePermissionsPage removePermission(String permissionName) {
        log.info("Click remove permissions for {}", permissionName);
        if (pageContainsPermission(permissionName)) {
            getDriver().findElement(By.name(permissionName))
                    .findElement(By.cssSelector("input[type=\'submit\']"))
                    .click();
        }
        return new GoogleManagePermissionsPage(getDriver());
    }

    public boolean pageContainsPermission(String permissionName) {
        log.info("Query page has permissions for {}", permissionName);
        try {
            return getDriver().findElement(By.name(permissionName))
                    .isDisplayed();
        } catch (NoSuchElementException nsee) {
            // Permission not listed
            return false;
        }
    }
}
