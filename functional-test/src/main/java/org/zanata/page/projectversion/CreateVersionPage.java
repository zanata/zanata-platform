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
package org.zanata.page.projectversion;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.zanata.page.BasePage;

@Slf4j
public class CreateVersionPage extends BasePage {

    public final static String VALIDATION_ERROR =
            "must start and end with letter or number, and contain only " +
                    "letters, numbers, periods, underscores and hyphens.";

    private By projectVersionID = By.id("create-version-form:slugField:slug");
    private By projectTypeSelection = By.id("create-version-form:project-type");
    private By saveButton = By.id("create-version-form:button-create");
    private By copyFromPreviousVersionChk = By.id("create-version-form:copy-from-version");

    private By projectTypesList = By.id("create-version-form:project-type-list");
    private By previousVersionsList = By.id("create-version-form:project-version");

    public CreateVersionPage(final WebDriver driver) {
        super(driver);
    }

    /**
     * Enter a version ID - only available on creating a new version
     *
     * @param versionId
     * @return new CreateVersionPage
     */
    public CreateVersionPage inputVersionId(final String versionId) {
        log.info("Enter version ID {}", versionId);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                getVersionIdField().clear();
                new Actions(getDriver()).moveToElement(getVersionIdField())
                        .perform();
                getVersionIdField().sendKeys(versionId);
                return true;
            }
        });
        return new CreateVersionPage(getDriver());
    }

    private boolean isCopyFromVersionAvailable() {
        return getDriver()
                .findElements(copyFromPreviousVersionChk)
                .size() > 0;
    }

    private void clickCopyFromCheckbox() {
        ((JavascriptExecutor) getDriver())
                .executeScript("arguments[0].click();",
                waitForWebElement(copyFromPreviousVersionChk)
                        .findElement(By.tagName("span")));

    }

    public CreateVersionPage enableCopyFromVersion() {
        log.info("Set Copy From Previous checkbox");
        if (!isCopyFromVersionAvailable()) {
            log.warn("Copy Version not available!");
            return this;
        }
        if (copyFromVersionIsChecked()) {
            log.warn("Checkbox already enabled!");
        } else {
            clickCopyFromCheckbox();
        }
        waitForWebElement(previousVersionsList);
        return this;
    }

    public CreateVersionPage disableCopyFromVersion() {
        log.info("Unset Copy From Previous checkbox");
        if (!isCopyFromVersionAvailable()) {
            log.warn("Copy Version not available!");
            return this;
        }
        if (!copyFromVersionIsChecked()) {
            log.warn("Checkbox already disabled!");
        } else {
            clickCopyFromCheckbox();
        }
        waitForWebElement(projectTypesList);
        return this;
    }

    public boolean copyFromVersionIsChecked() {
        log.info("Query is Copy from Version checkbox checked");
        return waitForWebElement(copyFromPreviousVersionChk)
                .findElement(By.tagName("input")).isSelected();
    }

    private WebElement getVersionIdField() {
        log.info("Query Version ID");
        return waitForWebElement(projectVersionID);
    }

    public CreateVersionPage selectProjectType(final String projectType) {
        log.info("Click project type {}", projectType);
        WebElement projectTypeCheck = waitForAMoment()
                .until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                for (WebElement item : waitForWebElement(projectTypeSelection)
                        .findElements(By.tagName("li"))) {
                    if (item.findElement(By.tagName("label")).getText()
                            .startsWith(projectType)) {
                        return item;
                    }
                }
                return null;
            }
        });
        projectTypeCheck.click();
        return new CreateVersionPage(getDriver());
    }

    public VersionLanguagesPage saveVersion() {
        log.info("Click Save");
        clickAndCheckErrors(waitForWebElement(saveButton));
        return new VersionLanguagesPage(getDriver());
    }

    public CreateVersionPage saveExpectingError() {
        log.info("Click Save");
        waitForWebElement(saveButton).click();
        return new CreateVersionPage(getDriver());
    }

    public CreateVersionPage waitForNumErrors(final int numberOfErrors) {
        log.info("Wait for number of error to be {}", numberOfErrors);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getErrors().size() == numberOfErrors;
            }
        });
        return new CreateVersionPage(getDriver());
    }

}
