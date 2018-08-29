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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.CorePage
import org.zanata.util.Checkbox
import org.zanata.util.WebElementUtil

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ManageSearchPage(driver: WebDriver) : BasePage(driver) {
    private val classesTable = By.id("form:actions")
    private val abortButton = By.id("form:cancel")
    private val selectAllButton = By.id("form:selectAllCheck")
    private val performButton = By.id("reindex")
    private val cancelButton = By.linkText("Abort")
    private val noOpsLabel = By.id("noOperationsRunning")
    private val abortedLabel = By.id("aborted")
    private val completedLabel = By.id("completed")

    /**
     * Select all of the available actions for a data type
     * @param clazz data type to select
     * @return new ManageSearchPage
     */
    @Suppress("unused")
    fun selectAllActionsFor(clazz: String): ManageSearchPage {
        val tableRows = WebElementUtil.getTableRows(driver,
                readyElement(classesTable))
        for (tableRow in tableRows) {
            if (tableRow.cellContents.contains(clazz)) {
                val allActionsChkBox = tableRow.cells[SELECT_ALL_COLUMN]
                        .findElement(CorePage.inputElement)
                Checkbox.of(allActionsChkBox).check()
            }
        }
        return ManageSearchPage(driver)
    }

    /**
     * Press the Select All button
     * @return new ManageSearchPage
     */
    fun clickSelectAll(): ManageSearchPage {
        log.info("Click Select All")
        clickElement(selectAllButton)
        // It seems that if the Select All and Perform buttons are clicked too
        // quickly in succession, the operation will fail
        waitForPageSilence()
        return ManageSearchPage(driver)
    }

    /**
     * Query if all actions in the table are selected
     * @return boolean all actions are selected
     */
    fun allActionsSelected(): Boolean {
        log.info("Query all actions selected")
        val tableRows = WebElementUtil.getTableRows(driver, readyElement(
                existingElement(classesTable), CorePage.tableElement))
        for (tableRow in tableRows) {
            // column 2, 3, 4 are checkboxes for purge, reindex and optimize
            for (i in 1..3) {
                try {
                    val checkBox = tableRow.cells[i]
                            .findElement(CorePage.inputElement)
                    if (!Checkbox.of(checkBox).checked()) {
                        return false
                    }
                } catch (e: IndexOutOfBoundsException) {
                    throw RuntimeException("Test oops: " +
                            tableRow.toString() + ":" + i)
                }

            }
        }
        return true
    }

    /**
     * Press the Perform All Actions button and wait for 'Abort' to be displayed
     * @return new ManageSearchPage
     */
    fun performSelectedActions(): ManageSearchPage {
        log.info("Click Perform Actions")
        clickElement(performButton)
        waitForAMoment().withMessage("displayed abort button")
                .until { readyElement(cancelButton).isDisplayed }
        return ManageSearchPage(driver)
    }

    /**
     * Wait for the Perform All Actions button to become available
     * @return new ManageSearchPage
     */
    fun expectActionsToFinish(): ManageSearchPage {
        log.info("Wait: all actions are finished")
        // once the button re-appears, it means the reindex is done.
        readyElement(performButton)
        return ManageSearchPage(driver)
    }

    /**
     * Press the Abort button
     * @return new ManageSearchPage
     */
    fun abort(): ManageSearchPage {
        log.info("Click Abort")
        clickElement(abortButton)
        return ManageSearchPage(driver)
    }

    /**
     * Check if the 'No Operations Running' label is displayed
     * @return boolean of label displayed
     */
    fun noOperationsRunningIsDisplayed(): Boolean {
        log.info("Query No Operations")
        return readyElement(noOpsLabel).isDisplayed
    }

    /**
     * Check if the 'All Operations Completed' label is displayed
     * @return boolean of label displayed
     */
    fun completedIsDisplayed(): Boolean {
        log.info("Query is action completed")
        return readyElement(completedLabel).isDisplayed
    }

    /**
     * Check if the 'Operations Aborted' label is displayed
     * @return boolean of label displayed
     */
    fun abortedIsDisplayed(): Boolean {
        log.info("Query is action aborted")
        return readyElement(abortedLabel).isDisplayed
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ManageSearchPage::class.java)
        private const val SELECT_ALL_COLUMN = 0
    }
}
