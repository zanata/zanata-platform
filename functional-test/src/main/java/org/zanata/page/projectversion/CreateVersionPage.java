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

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.Constants;

import java.util.List;
import java.util.Map;

public class CreateVersionPage extends BasePage {

    public final static String VALIDATION_ERROR =
            "must start and end with letter or number, "
            + "and contain only letters, numbers, underscores and hyphens.";

    @FindBy(id = "create-version-form:project-type")
    private WebElement projectTypeSelection;

    @FindBy(id = "create-version-form:statusField:status")
    private WebElement statusSelection;

    @FindBy(id = "create-version-form:button-create")
    private WebElement saveButton;

    private static final Map<String, String> projectTypeOptions =
            Constants.projectTypeOptions();

    public CreateVersionPage(final WebDriver driver) {
        super(driver);
    }

    /**
     * Enter a version ID - only available on creating a new version
     * @param versionId
     * @return new CreateVersionPage
     */
    public CreateVersionPage inputVersionId(final String versionId) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                getVersionIdField().clear();
                new Actions(getDriver()).moveToElement(getVersionIdField()).perform();
                getVersionIdField().sendKeys(versionId);
                return true;
            }
        });
        return new CreateVersionPage(getDriver());
    }

    private WebElement getVersionIdField() {
        return getDriver().findElement(By.id("create-version-form:slugField:slug"));
    }

    public CreateVersionPage selectProjectType(String projectType) {
        List<WebElement> projectTypes =
                projectTypeSelection.findElements(By.tagName("li"));
        for (WebElement projectTypeLi : projectTypes) {
            if (projectTypeLi.findElement(By.xpath(".//div/label")).getText()
                    .equals(projectType)) {
                projectTypeLi.findElement(By.xpath(".//div")).click();
                break;
            }
        }
        return this;
    }

    public VersionLanguagesPage saveVersion() {
        clickAndCheckErrors(saveButton);
        return new VersionLanguagesPage(getDriver());
    }

    public CreateVersionPage waitForNumErrors(final int numberOfErrors) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getErrors().size() == numberOfErrors;
            }
        });
        return new CreateVersionPage(getDriver());
    }

}
