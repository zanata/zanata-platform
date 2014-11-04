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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ManageUserPage extends BasePage {

    public static final int USERNAME_COLUMN = 0;
    public static final int EDITBUTTON_COLUMN = 4;

    private By userTable = By.id("usermanagerForm:userList");
    private By userEditButton = By.xpath(".//button[contains(text(), 'Edit')]");

    public ManageUserPage(WebDriver driver) {
        super(driver);
    }

    public ManageUserAccountPage editUserAccount(String username) {
        log.info("Click edit on {}", username);
        WebElement editCell = findRowByUserName(username).getCells()
                .get(EDITBUTTON_COLUMN);
        waitForWebElement(editCell, userEditButton).click();
        return new ManageUserAccountPage(getDriver());
    }

    private TableRow findRowByUserName(final String username) {
        return waitForAMoment().until(new Function<WebDriver, TableRow>() {
            @Override
            public TableRow apply(WebDriver driver) {
                List<TableRow> tableRows = WebElementUtil
                        .getTableRows(getDriver(),
                                waitForWebElement(userTable));
                Optional<TableRow> matchedRow = Iterables.tryFind(tableRows,
                        new Predicate<TableRow>() {
                            @Override
                            public boolean apply(TableRow input) {
                                List<String> cellContents =
                                        input.getCellContents();
                                String localeCell = cellContents
                                        .get(USERNAME_COLUMN)
                                        .trim();
                                return localeCell.equalsIgnoreCase(username);
                            }
                        });

                // we keep looking for the user until timeout
                return matchedRow.isPresent() ? matchedRow.get() : null;
            }
        });
    }

    public List<String> getUserList() {
        log.info("Query user list");
        return WebElementUtil.getColumnContents(getDriver(), userTable,
                USERNAME_COLUMN);
    }
}
