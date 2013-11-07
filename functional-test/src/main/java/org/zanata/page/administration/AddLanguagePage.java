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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddLanguagePage extends BasePage {

    public static final int NAME_ROW = 3;
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    @FindBy(xpath = "//input[@type='text' and contains(@id, 'localeName')]")
    private WebElement languageInput;

    @FindBy(xpath = "//input[@value='Save']")
    private WebElement saveButton;

    @FindBy(xpath =
            "//input[@type='checkbox' and contains(@name, 'enabledByDefault')]")
    private WebElement enabledByDefaultInput;

    public AddLanguagePage(final WebDriver driver) {
        super(driver);
    }

    public AddLanguagePage inputLanguage(String language) {
        languageInput.sendKeys(language);
        defocus();
        return this;
    }

    public AddLanguagePage enableLanguageByDefault() {
        if (!enabledByDefaultInput.isSelected()) {
            enabledByDefaultInput.click();
        }
        return this;
    }

    public AddLanguagePage disableLanguageByDefault() {
        if (enabledByDefaultInput.isSelected()) {
            enabledByDefaultInput.click();
        }
        return this;
    }

    public Map<String, String> getLanguageDetails() {
        Map<String, String> map = new HashMap();
        // Wait for the fields to be populated
        waitForTenSec().until(new Predicate<WebDriver>() {
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
        clickAndCheckErrors(saveButton);
        return new ManageLanguagePage(getDriver());
    }
}
