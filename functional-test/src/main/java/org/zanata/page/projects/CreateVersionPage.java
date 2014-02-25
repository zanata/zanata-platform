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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;
import org.zanata.util.Constants;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

public class CreateVersionPage extends BasePage {

    private static int NAME_COLUMN = 0;
    private static int DESCRIPTION_COLUMN = 1;
    private static int OPTION_COLUMN = 2;

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
                defocus();
                return true;
            }
        });
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

    public ProjectVersionPage clickUpdate() {
        clickAndCheckErrors(
                getDriver().findElement(By.id("iterationForm:update")));
        return new ProjectVersionPage(getDriver());
    }

    public CreateVersionPage showLocalesOverride() {
        getDriver().findElement(By.id("iterationForm:overrideLocales"))
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

    public boolean isValidationLevel(String optionName, String level) {
        String cssPath = ".//*[@value='" + level + "']";
        WebElement option =
                getValidationOption(optionName).getCells().get(OPTION_COLUMN)
                        .findElement(By.xpath(cssPath));
        return option.getAttribute("checked").equals("true");
    }

    private TableRow getValidationOption(final String optionName) {
        TableRow matchedRow = waitForTenSec()
                .until(new Function<WebDriver, TableRow>() {
                    @Override
                    public TableRow apply(WebDriver driver) {
                        List<TableRow> tableRows = WebElementUtil
                                .getTableRows(getDriver(), driver.findElement(By
                                        .id("iterationForm:validationOptionsTable")));
                        Optional<TableRow> matchedRow = Iterables
                                .tryFind(tableRows,
                                        new Predicate<TableRow>() {
                                            @Override
                                            public boolean apply(TableRow input) {
                                                List<String> cellContents = input
                                                        .getCellContents();
                                                String nameCell = cellContents
                                                        .get(NAME_COLUMN)
                                                        .trim();
                                                return nameCell
                                                        .equalsIgnoreCase(optionName);
                                            }
                                        });

                        // we keep looking for the option until timeout
                        return matchedRow.isPresent() ? matchedRow.get() : null;
                    }
                });
           return matchedRow;
    }

    public CreateVersionPage setValidationLevel(String optionName, String level) {
        String cssPath = ".//input[@value='" + level + "']";
        getValidationOption(optionName).getCells().get(OPTION_COLUMN)
                .findElement(By.xpath(cssPath)).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            // Wait for half a second before continuing
        }
        return new CreateVersionPage(getDriver());
    }
}
