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

import java.util.ArrayList

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class VersionDocumentsPage(driver: WebDriver) : VersionBasePage(driver) {

    private val docsList = By.id("documents-document_list")

    // getText often falls into a UI change
    val sourceDocumentNames: List<String>
        get() {
            log.info("Query source documents list")
            return waitForAMoment().withMessage("get source document names")
                    .until<List<String>> {
                        val fileNames = ArrayList<String>()
                        for (element in documentsTabDocumentList) {
                            fileNames.add(element.findElement(
                                    By.className("list__title")).text)
                        }
                        fileNames
                    }
        }

    private val documentsTabDocumentList: List<WebElement>
        get() {
            slightPause()
            return readyElement(docsList)
                    .findElements(By.xpath("./li"))
        }

    fun expectSourceDocsContains(document: String): VersionDocumentsPage {
        log.info("Expect Project documents contains {}", document)
        waitForPageSilence()
        assertThat(sourceDocumentNames).contains(document)
        return VersionDocumentsPage(driver)
    }

    fun sourceDocumentsContains(document: String): Boolean {
        log.info("Query source documents contains {}", document)
        return sourceDocumentNames.contains(document)
    }

    fun clickDownloadPotOnDocument(documentName: String): VersionDocumentsPage {
        val listItem = readyElement(docsList)
                .findElement(By.id("document-$documentName"))
        listItem.findElement(By.className("dropdown__toggle")).click()
        slightPause()
        clickLinkAfterAnimation(
                listItem.findElement(By.linkText("Download this document [offline .pot]")))
        slightPause()
        return VersionDocumentsPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionDocumentsPage::class.java)
    }

}
