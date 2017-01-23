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
package org.zanata.page.projectversion;

import java.util.List;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.webtrans.EditorPage;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * This class represents the languages page for a project version.
 *
 * @author Damian Jansen
 */
public class VersionLanguagesPage extends VersionBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VersionLanguagesPage.class);

    public VersionLanguagesPage(final WebDriver driver) {
        super(driver);
    }

    private By languageList = By.id("languages-language_list");
    private By languageItemTitle = By.className("list__item__meta");
    private By languageItemStats = By.className("stats__figure");
    private By languageDocumentList = By.id("languages-document_list");
    private By documentListItem = By.className("list__item--actionable");
    private By documentListItemTitle = By.className("list__title");

    private List<WebElement> getLanguageTabLocaleList() {
        return readyElement(languageList).findElements(By.tagName("li"));
    }

    private List<WebElement> getLanguageTabDocumentList() {
        log.info("Query documents list");
        return readyElement(existingElement(languageDocumentList),
                By.className("list--stats")).findElements(documentListItem);
    }

    public VersionLanguagesPage clickLocale(final String locale) {
        log.info("Click locale {}", locale);
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            new BasePage(getDriver()).waitForPageSilence();
            for (WebElement localeRow : getLanguageTabLocaleList()) {
                // Top <a>
                WebElement link = localeRow.findElement(By.xpath(".//a"));
                if (link.findElement(languageItemTitle).getText()
                        .equals(locale)) {
                    // Clicking too fast can often confuse
                    // the button:
                    slightPause();
                    clickLinkAfterAnimation(link);
                    return true;
                }
            }
            return false;
        });
        return new VersionLanguagesPage(getDriver());
    }

    public EditorPage clickDocument(final String docName) {
        log.info("Click document {}", docName);
        WebElement document = waitForAMoment()
                .until((Function<WebDriver, WebElement>) webDriver -> {
                    for (WebElement document1 : getLanguageTabDocumentList()) {
                        if (existingElement(document1, documentListItemTitle)
                                .getText().equals(docName)) {
                            return document1.findElement(documentListItemTitle);
                        }
                    }
                    return null;
                });
        clickLinkAfterAnimation(document);
        return new EditorPage(getDriver());
    }

    public EditorPage translate(final String locale, final String docName) {
        log.info("Click on {} : {} to begin translation", locale, docName);
        return gotoLanguageTab().clickLocale(locale).clickDocument(docName);
    }

    public String getStatisticsForLocale(final String localeId) {
        log.info("Query stats for {}", localeId);
        gotoLanguageTab();
        return refreshPageUntil(this,
                (Function<WebDriver, String>) webDriver -> {
                    String figure = null;
                    List<WebElement> localeList = getLanguageTabLocaleList();
                    for (WebElement locale : localeList) {
                        if (locale.findElement(languageItemTitle).getText()
                                .equals(localeId)) {
                            figure = locale.findElement(languageItemStats)
                                    .getText();
                            break;
                        }
                    }
                    Preconditions.checkState(figure != null,
                            "can not find statistics for locale: %s", localeId);
                    return figure;
                }, "Find the stats for locale " + localeId);
    }
}
