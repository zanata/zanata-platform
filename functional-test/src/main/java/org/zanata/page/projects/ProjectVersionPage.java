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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectVersionPage extends BasePage {
    @FindBy(id = "settings_tab")
    private WebElement settingsTab;

    @FindBy(id = "settings-languages_tab")
    private WebElement settingsLanguagesTab;

    @FindBy(id = "settings-translation_tab")
    private WebElement settingsTranslationTab;

    @FindBy(id = "documents_tab")
    private WebElement documentsTab;

    @FindBy(id = "languages_tab")
    private WebElement languageTab;

    public ProjectVersionPage(final WebDriver driver) {
        super(driver);
    }

    public String getVersionId() {
        return getLastBreadCrumbText();
    }

    private List<WebElement> getLanguageTabLocaleList() {
        WebElement languageList =
                waitForTenSec().until(new Function<WebDriver, WebElement>() {
                    @Override
                    public WebElement apply(WebDriver input) {
                        return getDriver().findElement(
                                By.id("languages-language_list"));
                    }
                });
        return languageList.findElements(By.tagName("li"));
    }

    public EditorPage translate(final String locale, final String docName) {
        gotoLanguageTab();
        return refreshPageUntil(this, new Function<WebDriver, EditorPage>() {
            @Override
            public EditorPage apply(WebDriver driver) {
                List<WebElement> localeList = getLanguageTabLocaleList();
                for (WebElement localeRow : localeList) {
                    WebElement link = localeRow.findElement(By.xpath(".//a"));
                    if (link.findElement(By.className("list__item"))
                            .findElement(By.className("list__item__info"))
                            .findElement(By.className("list__item__meta"))
                            .getText().equals(locale)) {
                        clickLinkAfterAnimation(link);

                        List<WebElement> documentList =
                                getVersionTabDocumentList();
                        for (int i = 0; i < documentList.size(); i++) {
                            WebElement document = documentList.get(i);
                            if (document
                                    .findElement(
                                            By.xpath(".//a/div/div/h3[@class='list__title']"))
                                    .getText().equals(docName)) {

                                clickLinkAfterAnimation(document.findElement(By
                                        .id(i + ":" + "link-translate-options")));

                                clickLinkAfterAnimation(document
                                        .findElement(
                                                By.id(i
                                                        + ":"
                                                        + "link-translate-online"))
                                        .findElement(By.tagName("a")));

                                return new EditorPage(getDriver());
                            }
                        }
                    }
                }
                throw new IllegalArgumentException("can not translate locale: "
                        + locale);
            }
        });
    }

    public ProjectVersionPage gotoDocumentTab() {
        documentsTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return documentsTab.isDisplayed();
            }
        });
        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage gotoLanguageTab() {
        languageTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return languageTab.isDisplayed();
            }
        });
        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage pressUploadFileButton() {
        clickLinkAfterAnimation(By.id("button--upload-new-document"));
        return new ProjectVersionPage(getDriver());
    }

    public boolean canSubmitDocument() {
        return getDriver().findElement(
                By.id("uploadDocForm:generalDocSubmitUploadButton"))
                .isEnabled();
    }

    public ProjectVersionPage cancelUpload() {
        getDriver().findElement(
                By.id("uploadDocForm:generalDocCancelUploadButton")).click();
        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage enterFilePath(String filePath) {
        getDriver().findElement(By.id("uploadDocForm:generalDocFileUpload"))
                .sendKeys(filePath);
        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage submitUpload() {
        getDriver().findElement(
                By.id("uploadDocForm:generalDocSubmitUploadButton")).click();
        return new ProjectVersionPage(getDriver());
    }

    public boolean sourceDocumentsContains(String document) {
        gotoDocumentTab();
        List<WebElement> documentList = getLanguageTabDocumentList();
        for (WebElement tableRow : documentList) {
            if (tableRow.getText().contains(document)) {
                return true;
            }
        }
        return false;
    }

    private List<WebElement> getLanguageTabDocumentList() {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("documents-document_list"))
                        .isDisplayed();
            }
        });

        return getDriver().findElement(By.id("documents-document_list"))
                .findElements(By.tagName("li"));
    }

    private List<WebElement> getVersionTabDocumentList() {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(
                                By.xpath("//form[@id='languages-document_list']/ul[@class='list--stats']"))
                        .isDisplayed();
            }
        });
        return getDriver()
                .findElements(
                        By.xpath("//form[@id='languages-document_list']/ul[@class='list--stats']/li"));
    }

    public ProjectVersionPage gotoSettingsTab() {
        settingsTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsTab.isDisplayed();
            }
        });
        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage gotoSettingsLanguagesTab() {
        settingsLanguagesTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsLanguagesTab.isDisplayed();
            }
        });
        return new ProjectVersionPage(getDriver());
    }

    public CreateVersionPage gotoSettingsTranslationTab() {
        settingsTranslationTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsTranslationTab.isDisplayed();
            }
        });
        return new CreateVersionPage(getDriver());
    }

    private List<WebElement> getEnabledLocaleListElement() {
        return getDriver().findElement(By.id("settings-languages-form"))
                .findElements(By.xpath(".//ul/li[@class='reveal--list-item']"));
    }

    public ProjectVersionPage removeLocale(final String localeId) {
        boolean removedLocale = false;
        for (WebElement localeLi : getEnabledLocaleListElement()) {
            String displayedLocaleId =
                    localeLi.findElement(By.xpath(".//span")).getText()
                            .replace("[", "").replace("]", "");
            if (displayedLocaleId.equals(localeId)) {
                localeLi.findElement(By.tagName("a")).click();
                removedLocale = true;
                break;
            }
        }
        Preconditions.checkState(removedLocale, "can not remove locale: %s",
                localeId);

        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return !getEnabledLocaleList().contains(localeId);
            }
        });

        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage enterSearchLocale(String localeQuery) {
        WebElementUtil.searchAutocomplete(getDriver(), "languageAutocomplete",
                localeQuery);
        return new ProjectVersionPage(getDriver());
    }

    public ProjectVersionPage addLocale(final String localeId) {
        waitForTenSec().until(new Function<WebDriver, List<WebElement>>() {
            @Override
            public List<WebElement> apply(WebDriver driver) {
                return WebElementUtil.getSearchAutocompleteResults(driver,
                        "settings-languages-form", "languageAutocomplete");
            }
        });

        List<WebElement> searchResults =
                WebElementUtil.getSearchAutocompleteResults(getDriver(),
                        "settings-languages-form", "languageAutocomplete");

        boolean clickedLocale = false;
        for (WebElement searchResult : searchResults) {
            if (searchResult.getText().contains(localeId)) {
                searchResult.click();
                clickedLocale = true;
                break;
            }
        }
        Preconditions.checkState(clickedLocale, "can not find locale - %s",
                localeId);

        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return getEnabledLocaleList().contains(localeId);
            }
        });

        return new ProjectVersionPage(getDriver());
    }

    public List<String> getEnabledLocaleList() {
        List<String> rows =
                Lists.transform(getEnabledLocaleListElement(),
                        new Function<WebElement, String>() {
                            @Override
                            public String apply(WebElement li) {
                                return li.getText();
                            }
                        });
        return rows;
    }

    public String getStatisticsForLocale(final String localeId) {
        gotoLanguageTab();

        return refreshPageUntil(this, new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver webDriver) {
                String figure = null;

                List<WebElement> localeList = getLanguageTabLocaleList();
                for (WebElement locale : localeList) {
                    if (locale
                            .findElement(
                                    By.xpath(".//a/div/div/span[@class='list__item__meta']"))
                            .getText().equals(localeId)) {
                        figure =
                                locale.findElement(
                                        By.xpath(".//a/div/div[2]/span/span[@class='stats__figure']"))
                                        .getText();
                        break;
                    }
                }
                Preconditions.checkState(figure != null,
                        "can not find statistics for locale: %s", localeId);
                return figure;
            }
        });
    }
}
