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
package org.zanata.workflow

import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.WebDriverFactory
import org.zanata.page.dashboard.DashboardBasePage
import org.zanata.page.utility.HomePage
import java.util.function.Supplier

open class AbstractWebWorkFlow {
    protected val driver: WebDriver
    protected val hostUrl: String

    init {
        val baseUrl = WebDriverFactory.INSTANCE.hostUrl
        hostUrl = appendTrailingSlash(baseUrl)
        driver = WebDriverFactory.INSTANCE.getDriver()
        WebDriverFactory.INSTANCE.ignoringDswid(Runnable { driver.get(hostUrl) })
    }

    fun goToHome(): HomePage {
        return WebDriverFactory.INSTANCE.ignoringDswid(Supplier {
            driver.get(hostUrl)
            BasePage(driver).waitForAMoment()
                    .withMessage("home page is valid")
                    .until { HomePage(driver).isPageValid }
            return@Supplier HomePage(driver)
        })
    }

    fun goToDashboard(): DashboardBasePage {
        return WebDriverFactory.INSTANCE.ignoringDswid(Supplier {
            driver.get(hostUrl + "dashboard")
            return@Supplier DashboardBasePage(driver)
        })
    }

    private fun appendTrailingSlash(baseUrl: String): String {
        return if (baseUrl.endsWith("/")) {
            baseUrl
        } else "$baseUrl/"
    }

}
