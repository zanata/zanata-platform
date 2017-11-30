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
package org.zanata.page.projects;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.util.WebElementUtil;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectPeoplePage extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectPeoplePage.class);
    private By peopleList = By.id("people_form");
    private By addSomeoneForm = By.id("project-people_add");
    private By addSomeoneInput = By.id("modalManagePermissionsAutocomplete");

    public ProjectPeoplePage(WebDriver driver) {
        super(driver);
    }

    public List<String> getPeople() {
        log.info("Query people list");
        List<String> names = new ArrayList<>();
        for (WebElement row : readyElement(peopleList)
                .findElements(By.tagName("li"))) {
            String username = row.findElement(By.tagName("a")).getText().trim();
            String roles = "";
            for (WebElement role : row
                    .findElements(By.className("txt--understated"))) {
                roles = roles.concat(role.getText().trim() + ";");
            }
            names.add(username + "|" + roles);
        }
        return names;
    }

    public ProjectPeoplePage clickAddSomeone() {
        log.info("Click Add Someone button");
        clickElement(existingElement(addSomeoneForm).findElement(By.tagName("button")));
        return new ProjectPeoplePage(getDriver());
    }

    public ProjectPeoplePage enterAddSomeoneUsername(final String username) {
        log.info("Enter user's username to search for");
        enterText(existingElement(addSomeoneInput), username, true, false, false);
        return new ProjectPeoplePage(getDriver());
    }

    public ProjectPeoplePage selectUserFromAddList(String username) {
        log.info("Click project version {}", username);
        waitForAMoment().withMessage("click on username in list")
                .until(driver -> {
            List<WebElement> items =
                    WebElementUtil.getSearchAutocompleteResults(driver,
                            "peopleTab-permissions", "modalManagePermissionsAutocomplete");
            for (WebElement item : items) {
                if (item.getText().equals(username)) {
                    item.click();
                    return true;
                }
            }
            return false;
        });
        return new ProjectPeoplePage(getDriver());
    }

    public ProjectPeoplePage clickTranslatorCheckboxFor(final String language) {
        log.info("Click checkbox for translator: {}", language);
        waitForAMoment().withMessage("click language checkbox")
                .until(driver -> {
            boolean found = false;
            List<WebElement> items = existingElement(By.id("peopleTab-permissions"))
                    .findElement(By.className("list--slat")).findElements(By.tagName("li"));
            for (WebElement item : items) {
                if (item.findElement(By.tagName("label")).getText().trim().equals(language)) {
                    found = true;
                    clickElement(existingElement(item, By.className("form__checkbox")));
                    break;
                }
            }
            return found;
        });
        slightPause();
        return new ProjectPeoplePage(getDriver());
    }

    public ProjectPeoplePage clickAddPerson() {
        log.info("Click Add Person button");
        clickElement(existingElement(
                By.id("peopleTab-permissions:modalManagePermissions-submit-buttons"))
                .findElement(By.tagName("input")));
        slightPause();
        return new ProjectPeoplePage(getDriver());
    }
}
