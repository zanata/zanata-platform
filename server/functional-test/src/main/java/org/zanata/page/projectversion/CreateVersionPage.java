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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateVersionPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CreateVersionPage.class);
    public static final String VALIDATION_ERROR =
            "must start and end with letter or number, and contain only letters, numbers, periods, underscores and hyphens.";
    public By projectVersionID = By.id("create-version-form:slug:input:slug");
    private By projectTypeSelection = By.id("create-version-form:project-type");
    private By saveButton = By.id("create-version-form:button-create");
    private By copyFromPreviousVersionChk =
            By.id("create-version-form:copy-from-version");
    private By projectTypesList =
            By.id("create-version-form:project-type-list");
    private By previousVersionsList =
            By.id("create-version-form:project-version");

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
        enterText(getVersionIdField(), versionId);
        return new CreateVersionPage(getDriver());
    }

    private boolean isCopyFromVersionAvailable() {
        return getDriver().findElements(copyFromPreviousVersionChk).size() > 0;
    }

    private void clickCopyFromCheckbox() {
        ((JavascriptExecutor) getDriver()).executeScript(
                "arguments[0].click();",
                readyElement(copyFromPreviousVersionChk)
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
        readyElement(previousVersionsList);
        return new CreateVersionPage(getDriver());
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
        readyElement(projectTypesList);
        return new CreateVersionPage(getDriver());
    }

    public boolean copyFromVersionIsChecked() {
        log.info("Query is Copy from Version checkbox checked");
        return readyElement(copyFromPreviousVersionChk)
                .findElement(By.tagName("input")).isSelected();
    }

    private WebElement getVersionIdField() {
        log.info("Query Version ID");
        return readyElement(projectVersionID);
    }

    public CreateVersionPage selectProjectType(final String projectType) {
        log.info("Click project type {}", projectType);
        WebElement projectTypeCheck = waitForAMoment()
                .until((Function<WebDriver, WebElement>) webDriver -> {
                    for (WebElement item : readyElement(projectTypeSelection)
                            .findElements(By.tagName("li"))) {
                        if (item.findElement(By.tagName("label")).getText()
                                .startsWith(projectType)) {
                            return item;
                        }
                    }
                    return null;
                });
        projectTypeCheck.click();
        return new CreateVersionPage(getDriver());
    }

    public VersionLanguagesPage saveVersion() {
        log.info("Click Save");
        clickAndCheckErrors(readyElement(saveButton));
        return new VersionLanguagesPage(getDriver());
    }

    public CreateVersionPage saveExpectingError() {
        log.info("Click Save");
        clickElement(saveButton);
        return new CreateVersionPage(getDriver());
    }

    public CreateVersionPage expectNumErrors(final int numberOfErrors) {
        log.info("Wait for number of error to be {}", numberOfErrors);
        waitForPageSilence();
        assertThat(getErrors()).hasSize(numberOfErrors);
        return new CreateVersionPage(getDriver());
    }
}
