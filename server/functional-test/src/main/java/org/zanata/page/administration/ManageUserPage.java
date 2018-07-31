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
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ManageUserPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ManageUserPage.class);

    private By userTable = By.id("usermanagerForm");
    private By lockIcon = By.className("i--lock");
    private By listEntry = By.className("list__item--actionable");
    private By actionsDropdown = By.id("rolemanage-more-actions");
    private By createUser = By.linkText("Create new user");

    public ManageUserPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Press the edit button on a user account
     * @param username of account to edit
     * @return new ManageUserAccountPage
     */
    public ManageUserAccountPage editUserAccount(String username) {
        log.info("Click edit on {}", username);
        clickElement(findRowByUserName(username));
        return new ManageUserAccountPage(getDriver());
    }

    /**
     * Get a list of user names
     * @return String list of user names
     */
    public List<String> getUserList() {
        log.info("Query user list");
        // Page may refresh user list
        waitForPageSilence();
        List<String> names = new ArrayList<>();
        for (WebElement element : getRows()) {
            names.add(getListItemUsername(element));
        }
        return names;
    }

    /**
     * Open the menu and select Create New User
     * @return new CreateUserAccountPage
     */
    public CreateUserAccountPage selectCreateNewUser() {
        log.info("Click Create new user");
        clickElement(actionsDropdown);
        clickLinkAfterAnimation(createUser);
        return new CreateUserAccountPage(getDriver());
    }

    /**
     * Query if user is enabled
     * @param username to query
     * @return boolean user is enabled
     */
    public boolean isUserEnabled(String username) {
        log.info("Query is user {} enabled", username);
        return findRowByUserName(username).findElements(lockIcon).isEmpty();
    }

    // Find the row WebElement containing the username
    private WebElement findRowByUserName(final String username) {
        for (WebElement listItem : getRows()) {
            if (getListItemUsername(listItem).equals(username)) {
                // TODO this is ugly but seems to work in firefox
                List<WebElement> linksUnderneath =
                        listItem.findElements(By.tagName("a"));
                for (WebElement link : linksUnderneath) {
                    String onclickCallback = link.getAttribute("href");
                    if (onclickCallback != null
                            && onclickCallback.contains("/userdetail?")) {
                        return link;
                    }
                }
                return listItem;
            }
        }
        throw new RuntimeException("Search for username " + username +
                " failed");
    }

    // Retrieve all of the user rows from the table
    private List<WebElement> getRows() {
        return readyElement(userTable).findElements(listEntry);
    }

    // Retrieve the username from a user row
    private String getListItemUsername(WebElement listItem) {
        String listItemText = listItem.findElement(By.tagName("h3")).getText();
        return listItemText
                .substring(0, listItemText.lastIndexOf(getListItemRoles(listItem)))
                .trim();
    }

    // Retrieve the list of roles, as a string, from a user row
    private String getListItemRoles(WebElement listItem) {
        return listItem.findElement(By.tagName("h3"))
                .findElement(By.className("txt--meta")).getText();
    }

}
