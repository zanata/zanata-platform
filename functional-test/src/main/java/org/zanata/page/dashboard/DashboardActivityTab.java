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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.zanata.util.WebElementUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardActivityTab extends DashboardBasePage {

    public DashboardActivityTab(WebDriver driver) {
        super(driver);
    }

    public List<WebElement> getMyActivityList() {
        WebElement listWrapper =
                getDriver().findElement(By.id("activityList"));

        if (listWrapper != null) {
            return listWrapper.findElements(By.xpath("./li"));
        }
        return new ArrayList<WebElement>();
    }

    public void clickMoreActivity() {
        WebElement moreActivity = getMoreActivityElement();
        if (moreActivity != null) {
            moreActivity.click();
            WebElementUtil.waitForTenSeconds(getDriver()).until(
                    ExpectedConditions.invisibilityOfElementLocated(By
                            .className("loader__spinner")));
        }
    }

    public WebElement getMoreActivityElement() {
        return getDriver().findElement(By.id("moreActivity"));
    }
}
