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
package org.zanata.page.dashboard;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardActivityTab extends DashboardBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardActivityTab.class);
    private By activityList = By.id("activity-list");
    private By moreActivityButton = By.id("moreActivity");

    public DashboardActivityTab(WebDriver driver) {
        super(driver);
    }

    public List<WebElement> getMyActivityList() {
        log.info("Query activity list");
        return readyElement(activityList).findElements(By.xpath("./li"));
    }

    /**
     * Click on the activity list's "More Activity element".
     *
     * @return true, if the list has increased, false otherwise.
     */
    public boolean clickMoreActivity() {
        log.info("Click More Activity button");
        final int activityListOrigSize = getMyActivityList().size();
        clickElement(moreActivityButton);
        return waitForAMoment()
                .until((Function<WebDriver, Boolean>) webDriver -> getMyActivityList()
                        .size() > activityListOrigSize);
    }

    public boolean isMoreActivity() {
        return getDriver().findElements(moreActivityButton).size() > 0;
    }
}
