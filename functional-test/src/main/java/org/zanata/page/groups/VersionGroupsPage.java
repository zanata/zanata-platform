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
package org.zanata.page.groups;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class VersionGroupsPage extends BasePage {
    public static final int GROUP_NAME_COLUMN = 0;
    public static final int GROUP_DESCRIPTION_COLUMN = 1;
    public static final int GROUP_TIMESTAMP_COLUMN = 2;
    public static final int GROUP_STATUS_COLUMN = 3;

    @FindBy(id = "groupForm:groupTable")
    private WebElement groupTable;

    @FindBy(className = "infomsg.icon-info-circle-2")
    private WebElement infomsg;

    public VersionGroupsPage(WebDriver driver) {
        super(driver);
    }

    public List<String> getGroupNames() {
        By by = By.id("groupForm:groupTable");
        return WebElementUtil.getColumnContents(getDriver(), by,
                GROUP_NAME_COLUMN);
    }

    public CreateVersionGroupPage createNewGroup() {
        log.info("Click New Group button");
        WebElement createLink =
                getDriver().findElement(By.id("group-create"));
        createLink.click();
        return new CreateVersionGroupPage(getDriver());
    }

    public VersionGroupPage goToGroup(String groupName) {
        log.info("Click group {}", groupName);
        groupTable.findElement(By.linkText(groupName)).click();
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupsPage toggleObsolete(final boolean show) {
        WebElement showObsolete =
                getDriver().findElement(By.id("groupForm:showObsolete"));
        if (show != showObsolete.isSelected()) {
            showObsolete.click();
        }
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                WebElement table =
                        input.findElement(By.id("groupForm:groupTable"));
                return table.findElements(By.className("obsolete_link"))
                        .isEmpty() == !show;
            }
        });
        return new VersionGroupsPage(getDriver());
    }

    public String getInfoMessage() {
        log.info("Test info msg");
        log.info(getDriver().findElement(
                By.className("infomsg.icon-info-circle-2")).getText());
        return getDriver().findElement(
                By.className("infomsg.icon-info-circle-2")).getText();
    }

}
