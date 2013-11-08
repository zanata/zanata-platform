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
package org.zanata.page.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;
import org.zanata.util.Constants;

public class CreateVersionPage extends BasePage {

    @FindBy(id = "iterationForm:projectTypeField:projectType")
    private WebElement projectTypeSelection;

    @FindBy(id = "iterationForm:statusField:status")
    private WebElement statusSelection;

    @FindBy(id = "iterationForm:save")
    private WebElement saveButton;

    private static final Map<String, String> projectTypeOptions =
            Constants.projectTypeOptions();

    public CreateVersionPage(final WebDriver driver) {
        super(driver);
    }

    public CreateVersionPage inputVersionId(String versionId) {
        getVersionIdField().clear();
        new Actions(getDriver()).moveToElement(getVersionIdField()).perform();
        getVersionIdField().sendKeys(versionId);
        defocus();
        return new CreateVersionPage(getDriver());
    }

    private WebElement getVersionIdField() {
        return getDriver().findElement(By.id("iterationForm:slugField:slug"));
    }

    public CreateVersionPage selectProjectType(String projectType) {
        new Select(projectTypeSelection).selectByVisibleText(projectTypeOptions
                .get(projectType));
        return this;
    }

    public CreateVersionPage selectStatus(String status) {
        new Select(statusSelection).selectByVisibleText(status);
        return this;
    }

    public ProjectVersionPage saveVersion() {
        clickAndCheckErrors(saveButton);
        return new ProjectVersionPage(getDriver());
    }

    public CreateVersionPage showLocalesOverride() {
        getDriver().findElement(By.xpath("//*[@title='overrideLocales']"))
                .click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("iterationForm:languagelist1"))
                        .isDisplayed();
            }
        });
        return new CreateVersionPage(getDriver());
    }

    public CreateVersionPage selectEnabledLanguage(String language) {
        getDriver()
                .findElement(By.id("iterationForm:languagelist1"))
                .findElement(By.xpath(".//option[@value='"+language+"']"))
                .click();
        return new CreateVersionPage(getDriver());
    }

    public CreateVersionPage selectDisabledLanguage(String language) {
        getDriver()
                .findElement(By.id("iterationForm:languagelist2"))
                .findElement(By.xpath(".//option[@value='"+language+"']"))
                .click();
        return new CreateVersionPage(getDriver());
    }

    public CreateVersionPage clickAddLanguage() {
        getDriver().findElement(By.xpath("//*[@value='Add >']")).click();
        return new CreateVersionPage(getDriver());
    }

    public CreateVersionPage clickRemoveLanguage() {
        getDriver().findElement(By.xpath("//*[@value='< Remove']")).click();
        return new CreateVersionPage(getDriver());
    }

    public List<String> getEnabledLanguages() {
        List<String> languages = new ArrayList<String>();
        for (WebElement element : getDriver()
                .findElement(By.id("iterationForm:languagelist1"))
                .findElements(By.tagName("option"))) {
            languages.add(element.getText());
        }
        return languages;
    }

    public List<String> getDisabledLanguages() {
        List<String> languages = new ArrayList<String>();
        for (WebElement element : getDriver()
                .findElement(By.id("iterationForm:languagelist2"))
                .findElements(By.tagName("option"))) {
            languages.add(element.getText());
        }
        return languages;
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

    public CreateVersionPage waitForListCount(final int enabled,
                                              final int disabled) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                    .findElement(By.id("iterationForm:languagelist1"))
                    .findElements(By.tagName("option"))
                        .size() == enabled &&
                    getDriver()
                        .findElement(By.id("iterationForm:languagelist2"))
                        .findElements(By.tagName("option"))
                            .size() == disabled;
            }
        });
        return new CreateVersionPage(getDriver());
    }

}
