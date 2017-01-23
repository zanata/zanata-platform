/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ManageUserPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ManageUserPage.class);
    private By userTable = By.id("usermanagerForm");

    public ManageUserPage(WebDriver driver) {
        super(driver);
    }

    public ManageUserAccountPage editUserAccount(String username) {
        log.info("Click edit on {}", username);
        clickElement(findRowByUserName(username));
        return new ManageUserAccountPage(getDriver());
    }

    private WebElement findRowByUserName(final String username) {
        for (WebElement listItem : getRows()) {
            if (getListItemUsername(listItem).equals(username)) {
                // TODO this is ugly but seems to work in firefox
                List<WebElement> linksUnderneath =
                        listItem.findElements(By.tagName("a"));
                for (WebElement link : linksUnderneath) {
                    String onclickCallback = link.getAttribute("onclick");
                    if (onclickCallback != null
                            && onclickCallback.contains("editUser")) {
                        return link;
                    }
                }
                return listItem;
            }
        }
        return null;
    }

    public List<WebElement> getRows() {
        return readyElement(userTable)
                .findElements(By.className("list__item--actionable"));
    }

    public String getListItemUsername(WebElement listItem) {
        String listItemText = listItem.findElement(By.tagName("h3")).getText();
        return listItemText
                .substring(0,
                        listItemText.lastIndexOf(getListItemRoles(listItem)))
                .trim();
    }

    public String getListItemRoles(WebElement listItem) {
        return listItem.findElement(By.tagName("h3"))
                .findElement(By.className("txt--meta")).getText();
    }

    public List<String> getUserList() {
        log.info("Query user list");
        List<String> names = new ArrayList<>();
        for (WebElement element : getRows()) {
            names.add(getListItemUsername(element));
        }
        return names;
    }
}
