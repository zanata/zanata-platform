/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.editor

import java.util.ArrayList
import java.util.HashSet

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.CorePage

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ReactEditorPage(driver: WebDriver) : CorePage(driver) {

    private val headerElement = By.ById("editor-header")
    private val transUnitText = By.ByClassName("TransUnit-text")

    // TODO: find out why there's a background request, and either amend or override waitForPageSilence
    override val expectedBackgroundRequests: Int
        get() = 1

    val isReactEditor: Boolean
        get() {
            log.info("Query is React editor visible")
            return driver.findElements(headerElement).size > 0
        }

    val transunitTargets: List<WebElement>
        get() {
            log.info("Query Transunit targets")
            val targets = ArrayList<WebElement>()
            for (element in textUnits) {
                if (element.tagName.trim { it <= ' ' } == "textarea") {
                    targets.add(element)
                }
            }
            return targets
        }

    private val textUnits: List<WebElement>
        get() = driver.findElements(transUnitText)

    fun switchToEditorWindow(): ReactEditorPage {
        log.info("Switching to new window (from {})", driver.windowHandle)
        val mainHandle = driver.windowHandle
        waitForAMoment().withMessage("second window to be present")
                .until { allWindowHandles.size > 1 }
        val allWindowHandles = HashSet(allWindowHandles)
        log.info("main window handle: {}", mainHandle)
        val iter = allWindowHandles.iterator()
        while (iter.hasNext()) {
            val handle = iter.next()
            if (handle == mainHandle) {
                log.info("stripping main handle: {}", mainHandle)
                iter.remove()
            }
        }
        val handle = allWindowHandles.stream()
                .filter { it -> it != mainHandle }
                .findAny()
                .get()
        log.info("found target window: {}", handle)
        waitForAMoment().withMessage("waiting for window").until {
            driver.switchTo().window(handle)
            driver.windowHandle == handle
        }
        waitForPageSilence()
        waitForAMoment().withMessage("waiting for editor").until {
            if (isReactEditor) {
                log.info("React Editor Window: {}", driver.windowHandle)
            }
            isReactEditor
        }
        return ReactEditorPage(driver)
    }

    fun expectNumberOfTargets(expected: Int) {
        log.info("Expect number of translation target is {}", expected)
        waitForAMoment().withMessage("Expected number of targets is shown")
                .until { transunitTargets.size == expected }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ReactEditorPage::class.java)
    }
}
