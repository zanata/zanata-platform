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
package org.zanata.page.glossary;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class GlossaryPage extends BasePage {

    private By glossaryMain = By.id("glossary_form");
    private By entryCount = By.className("stats__figure");
    private By listItem = By.className("list__item--actionable");
    private By entryName = By.className("list__title");

    public GlossaryPage(WebDriver driver) {
        super(driver);
    }

    public List<String> getAvailableGlossaryLanguages() {
        log.info("Query available glossary languages");
        List<String> availableLanguages = new ArrayList<>();
        for (WebElement element : getListItems()) {
            availableLanguages.add(element.findElement(entryName)
                    .getText().trim());
        }
        return availableLanguages;

    }

    public int getGlossaryEntryCount(String lang) {
        log.info("Query number of glossary entries for {}", lang);
        List<WebElement> langs = getListItems();
        int row = getAvailableGlossaryLanguages().indexOf(lang);
        if (row >= 0) {
            return Integer.parseInt(langs.get(row)
                    .findElement(entryCount).getText());
        }
        return -1;
    }

    private List<WebElement> getListItems() {
        return waitForElementExists(
                waitForElementExists(glossaryMain),
                        By.className("list--stats"))
                .findElements(listItem);
    }
}
