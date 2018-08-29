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
package org.zanata.page.dashboard

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

/**
 * @author Carlos Munoz [camunoz@redhat.com](mailto:camunoz@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class DashboardActivityTab(driver: WebDriver) : DashboardBasePage(driver) {
    private val activityList = By.id("activity-list")
    private val moreActivityButton = By.id("moreActivity")

    val myActivityList: List<WebElement>
        get() {
            log.info("Query activity list")
            return readyElement(activityList).findElements(By.xpath("./li"))
        }

    val isMoreActivity: Boolean
        get() = driver.findElements(moreActivityButton).size > 0

    /**
     * Click on the activity list's "More Activity element".
     *
     * @return true, if the list has increased, false otherwise.
     */
    fun clickMoreActivity(): Boolean {
        log.info("Click More Activity button")
        val activityListOrigSize = myActivityList.size
        clickElement(moreActivityButton)
        return waitForAMoment().withMessage("activity list size increased")
                .until { myActivityList.size > activityListOrigSize }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(DashboardActivityTab::class.java)
    }
}
