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
package org.zanata.workflow;

import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.utility.HomePage;
import org.zanata.page.WebDriverFactory;

public class AbstractWebWorkFlow {
    protected final WebDriver driver;
    protected final String hostUrl;

    public AbstractWebWorkFlow() {
        String baseUrl = WebDriverFactory.INSTANCE.getHostUrl();
        hostUrl = appendTrailingSlash(baseUrl);
        driver = WebDriverFactory.INSTANCE.getDriver();
        WebDriverFactory.INSTANCE.ignoringDswid(() ->
                driver.get(hostUrl));
    }

    public HomePage goToHome() {
        return WebDriverFactory.INSTANCE.ignoringDswid(() -> {
            driver.get(hostUrl);
            new BasePage(driver).waitForAMoment().until(
                    (Predicate<WebDriver>) input ->
                            new HomePage(driver).isPageValid());
            return new HomePage(driver);
        });
    }

    public DashboardBasePage goToDashboard() {
        return WebDriverFactory.INSTANCE.ignoringDswid(() -> {
            driver.get(hostUrl + "dashboard");
            return new DashboardBasePage(driver);
        });
    }

    private static String appendTrailingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl;
        }
        return baseUrl + "/";
    }

}
