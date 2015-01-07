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
package org.zanata.page.administration;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddLanguagePage extends BasePage {

    public static final int NAME_ROW = 3;
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    private By languageInputField = By.xpath(
            "//input[@type='text' and contains(@id, 'localeName')]");
    private By saveButton = By.xpath("//input[@value='Save']");
    private By enabledByDefaultCheckbox = By.xpath(
            "//input[@type='checkbox' and contains(@name, 'enabledByDefault')]");

    public AddLanguagePage(final WebDriver driver) {
        super(driver);
    }

    public AddLanguagePage inputLanguage(String language) {
        log.info("Enter language {}", language);
        waitForWebElement(languageInputField).sendKeys(language);
        defocus();
        waitForPageSilence();
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage enableLanguageByDefault() {
        log.info("Click Enable by default");
        if (!waitForWebElement(enabledByDefaultCheckbox).isSelected()) {
            waitForWebElement(enabledByDefaultCheckbox).click();
        }
        return new AddLanguagePage(getDriver());
    }

    public AddLanguagePage disableLanguageByDefault() {
        log.info("Click Disable by default");
        if (waitForWebElement(enabledByDefaultCheckbox).isSelected()) {
            waitForWebElement(enabledByDefaultCheckbox).click();
        }
        return new AddLanguagePage(getDriver());
    }

    public Map<String, String> getLanguageDetails() {
        log.info("Query language details");
        Map<String, String> map = new HashMap();
        // Wait for the fields to be populated
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                List<WebElement> thisElement = getDriver()
                        .findElements(By.className("prop"));
                return !thisElement.get(NAME_ROW)
                        .findElements(By.tagName("span"))
                        .get(VALUE_COLUMN).getText().isEmpty();
            }
        });
        for (WebElement item : getDriver().findElements(By.className("prop"))) {
            map.put(item.findElements(By.tagName("span"))
                            .get(NAME_COLUMN).getText(),
                    item.findElements(By.tagName("span"))
                            .get(VALUE_COLUMN).getText());
        }
        return map;
    }

    public ManageLanguagePage saveLanguage() {
        log.info("Click Save");
        clickAndCheckErrors(waitForWebElement(saveButton));
        return new ManageLanguagePage(getDriver());
    }
}
