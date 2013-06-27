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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ManageUserPage extends AbstractPage
{
   public static final int USERNAME_COLUMN = 0;
   @FindBy(id = "usermanagerForm:threads")
   private WebElement userTable;

   private By userTableBy = By.id("usermanagerForm:threads");

   public ManageUserPage(WebDriver driver)
   {
      super(driver);
   }

   public ManageUserAccountPage editUserAccount(String username) {
      TableRow userRow = findRowByUserName(username);
      List<WebElement> cells = userRow.getCells();
      WebElement editCell = cells.get(cells.size() - 1);
      WebElement editButton = editCell.findElement(By.xpath(".//input[@value='Edit']"));
      editButton.click();
      return new ManageUserAccountPage(getDriver());
   }

   private TableRow findRowByUserName(final String username)
   {
      TableRow matchedRow = waitForTenSec().until(new Function<WebDriver, TableRow>()
      {
         @Override
         public TableRow apply(WebDriver driver)
         {
            List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(), userTable);
            Optional<TableRow> matchedRow = Iterables.tryFind(tableRows, new Predicate<TableRow>()
            {
               @Override
               public boolean apply(TableRow input)
               {
                  List<String> cellContents = input.getCellContents();
                  String localeCell = cellContents.get(USERNAME_COLUMN).trim();
                  return localeCell.equalsIgnoreCase(username);
               }
            });

            //we keep looking for the user until timeout
            return matchedRow.isPresent() ? matchedRow.get() : null;
         }
      });
      return matchedRow;
   }

   public List<String> getUserList()
   {
      return WebElementUtil.getColumnContents(getDriver(), userTableBy, USERNAME_COLUMN);
   }
}
