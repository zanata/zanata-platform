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

package org.zanata.page.projects;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.TableRow;

import static org.zanata.util.WebElementUtil.*;

import java.util.List;

/**
 * @author Damian Jansen <a
 *      href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectMaintainersPage extends BasePage {

    private static int USERNAME_COLUMN = 0;
    private static int NAME_COLUMN = 1;
    private static int EMAIL_COLUMN = 2;
    private static int ACTION_COLUMN = 3;

    public ProjectMaintainersPage(WebDriver driver) {
        super(driver);
    }

    public ProjectMaintainersPage clickAddMaintainer() {
        getDriver().findElement(By.linkText("Add Project Maintainer")).click();
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectMaintainersPage enterName(String name) {
        waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                return getDriver()
                        .findElement(By.id("addMaintainerForm:" +
                                "userField:usernameAutocompleteInput"));
            }
        }).sendKeys(name);
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectMaintainersPage clickAddAutoComplete(String username) {
        getAutoCompleteOption(username);
        // Click somewhere to actually get this to work
        getDriver().findElement(By.id("addMaintainerForm:results")).click();
        return new ProjectMaintainersPage(getDriver());
    }

    public String getAddMaintainerUserName() {
        expectAddMaintainerData();
        return getDriver().findElement(By
                .id("addMaintainerForm:output1:nameLabel"))
                .getText();
    }

    public String getAddMaintainerEmail() {
        expectAddMaintainerData();
        return getDriver().findElement(By
                .id("addMaintainerForm:output2:emailLabel"))
                .getText();
    }

    public List<String> maintainersList() {
        return getColumnContents(getDriver(),
                By.id("projectMaintainersForm:threads"), USERNAME_COLUMN);
    }

    public String getUserFullName(String username) {
        return findNameColumnFor(username).getText();
    }

    public String getUserEmailAddress(String username) {
        return findEmailColumnFor(username).getText();
    }

    public ProjectMaintainersPage removeMaintainer(String username) {
        findRemoveButtonFor(username).click();
        getDriver().switchTo().alert().accept();
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectMaintainersPage cancelRemoveMaintainer(String username) {
        findRemoveButtonFor(username).click();
        getDriver().switchTo().alert().dismiss();
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectMaintainersPage clickAdd() {
        getDriver().findElement(By.id("addMaintainerForm:addButton")).click();
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectMaintainersPage clickClose() {
        getDriver().findElement(By.id("addMaintainerForm:closeButton")).click();
        return new ProjectMaintainersPage(getDriver());
    }

    private void expectAddMaintainerData() {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                WebElement name = getDriver().findElement(By
                        .id("addMaintainerForm:output1:nameLabel"));
                WebElement email = getDriver().findElement(By
                        .id("addMaintainerForm:output2:emailLabel"));
                return !(name.getText().isEmpty() && email.getText().isEmpty());
            }
        });
    }

    private WebElement getAutoCompleteOption(final String username) {
        return waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                List<WebElement> elements = getDriver()
                        .findElement(By.id("addMaintainerForm:userField:"+
                                "usernameAutocompleteItems"))
                        .findElements(By.className("rf-au-itm"));
                for (WebElement element : elements) {
                    if (element.getText().equals(username)) {
                        return element;
                    }
                }
                return null;
            }
        });
    }

    private TableRow getRowFor(String username) {
        List<TableRow> rows = getTableRows(getDriver(),
                By.id("projectMaintainersForm:threads"));
        for (TableRow tableRow : rows) {
            if (tableRow.getCells()
                    .get(USERNAME_COLUMN).getText().equals(username)) {
                return tableRow;
            }
        }
        return null;
    }

    private WebElement findNameColumnFor(String username) {
        TableRow row = getRowFor(username);
        if (row == null) {
            throw new RuntimeException("Expected row for "+username+" missing");
        }
        return row.getCells().get(NAME_COLUMN);
    }

    private WebElement findEmailColumnFor(String username) {
        TableRow row = getRowFor(username);
        if (row == null) {
            throw new RuntimeException("Expected row for "+username+" missing");
        }
        return row.getCells().get(EMAIL_COLUMN);
    }

    private WebElement findRemoveButtonFor(String username) {
        TableRow row = getRowFor(username);
        if (row == null) {
            throw new RuntimeException("Expected row for "+username+" missing");
        }
        return row.getCells().get(ACTION_COLUMN);
    }
}
