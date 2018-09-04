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
package org.zanata.page.webtrans

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.zanata.page.BasePage
import org.zanata.util.WebElementUtil

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@Suppress("unused")
class DocumentsViewPage(driver: WebDriver) : BasePage(driver) {
    @FindBy(id = "gwt-debug-documentListTable")
    private val documentListTable: WebElement? = null

    val documentListTableContent: List<List<String>>
        get() = WebElementUtil.getTwoDimensionList(driver,
                By.id("gwt-debug-documentListTable"))

    fun clickDocumentLink(path: String, name: String): EditorPage {
        val id = "gwt-debug-docLabel-$path$name"
        clickElement(By.id(id))
        return EditorPage(driver)
    }

}
