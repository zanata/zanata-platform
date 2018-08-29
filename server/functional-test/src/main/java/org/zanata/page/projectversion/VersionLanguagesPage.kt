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
package org.zanata.page.projectversion

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.BasePage
import org.zanata.page.webtrans.EditorPage

import com.google.common.base.Preconditions

/**
 * This class represents the languages page for a project version.
 *
 * @author Damian Jansen
 */
class VersionLanguagesPage(driver: WebDriver) : VersionBasePage(driver) {

    private val languageList = By.id("languages-language_list")
    private val languageItemTitle = By.className("list__item__meta")
    private val languageItemStats = By.className("stats__figure")
    private val languageDocumentList = By.id("languages-document_list")
    private val documentListItem = By.className("list__item--actionable")
    private val documentListItemTitle = By.className("list__title")

    private val languageTabLocaleList: List<WebElement>
        get() = readyElement(languageList).findElements(By.tagName("li"))

    private val languageTabDocumentList: List<WebElement>
        get() {
            log.info("Query documents list")
            return readyElement(existingElement(languageDocumentList),
                    By.className("list--stats")).findElements(documentListItem)
        }

    val versionID: String
        get() {
            log.info("Query version ID")
            return driver
                    .findElement(By.id("version-info"))
                    .findElement(By.tagName("h1")).text
        }

    fun clickLocale(locale: String): VersionLanguagesPage {
        log.info("Click locale {}", locale)
        waitForAMoment().withMessage("click locale in list").until {
            BasePage(driver).waitForPageSilence()
            for (localeRow in languageTabLocaleList) {
                // Top <a>
                val link = localeRow.findElement(By.xpath(".//a"))
                if (link.findElement(languageItemTitle).text == locale) {
                    // Clicking too fast can often confuse
                    // the button:
                    slightPause()
                    clickLinkAfterAnimation(link)
                    return@until true
                }
            }
            false
        }
        return VersionLanguagesPage(driver)
    }

    fun clickDocument(docName: String): EditorPage {
        log.info("Click document {}", docName)
        val document = waitForAMoment()
                .withMessage("find document $docName")
                .until<WebElement> {
                    for (document1 in languageTabDocumentList) {
                        if (existingElement(document1, documentListItemTitle)
                                        .text == docName) {
                            return@until document1
                        }
                    }
                    null
                }
        clickLinkAfterAnimation(document)
        return EditorPage(driver)
    }

    fun translate(locale: String, docName: String): EditorPage {
        log.info("Click on {} : {} to begin translation", locale, docName)
        return gotoLanguageTab().clickLocale(locale).clickDocument(docName)
    }

    fun clickDownloadTranslatedPo(documentName: String): VersionLanguagesPage {
        val linkText = "Download translated [offline .po]"
        return clickDownloadWithLinkText(documentName, linkText)
    }

    fun clickDownloadTranslatedFile(documentName: String,
                                    format: String): VersionLanguagesPage {
        val linkText = "Download translated [.$format]"
        return clickDownloadWithLinkText(documentName, linkText)
    }

    private fun clickDownloadWithLinkText(
            documentName: String,
            linkText: String): VersionLanguagesPage {
        val document = waitForAMoment()
                .withMessage("click download on $documentName")
                .until<WebElement> {
                    for (doc in languageTabDocumentList) {
                        if (existingElement(doc, documentListItemTitle)
                                        .text == documentName) {
                            return@until doc
                        }
                    }
                    null
                }
        clickElement(existingElement(document, By.className("dropdown__toggle")))
        slightPause()
        clickLinkAfterAnimation(existingElement(document, By.linkText(linkText)))
        slightPause()
        return VersionLanguagesPage(driver)
    }

    fun getStatisticsForLocale(localeId: String): String {
        log.info("Query stats for {}", localeId)
        gotoLanguageTab()
        return refreshPageUntil(this, "Find the stats for locale $localeId") {
            var figure: String? = null
            val localeList = languageTabLocaleList
            for (locale in localeList) {
                if (locale.findElement(languageItemTitle).text == localeId) {
                    figure = locale.findElement(languageItemStats).text
                    break
                }
            }
            Preconditions.checkState(figure != null,
                    "can not find statistics for locale: %s", localeId)
            figure.orEmpty()
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionLanguagesPage::class.java)
    }
}
