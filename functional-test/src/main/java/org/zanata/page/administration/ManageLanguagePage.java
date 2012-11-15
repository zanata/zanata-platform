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
package org.zanata.page.administration;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManageLanguagePage extends AbstractPage
{

   public static final int LOCALE_COLUMN = 0;
   @FindBy(id = "main_body_content")
   private WebElement mainBody;
   private final WebElement languageTable;

   public ManageLanguagePage(WebDriver driver)
   {
      super(driver);
      languageTable = mainBody.findElement(By.xpath(".//table"));
   }

   public AddLanguagePage addNewLanguage()
   {
      getDriver().findElement(By.linkText("Add New Language")).click();
      return new AddLanguagePage(getDriver());
   }

   public ManageLanguageTeamMemberPage manageTeamMembersFor(final String localeId)
   {
      TableRow matchedRow = findRowByLocale(localeId);

      log.debug("for locale [{}] found table row: {}", localeId, matchedRow);
      List<WebElement> cells = matchedRow.getCells();
      int teamMemberCellIndex = cells.size() - 1;
      WebElement teamMemberCell = cells.get(teamMemberCellIndex);
      WebElement teamMemberButton = teamMemberCell.findElement(By.xpath(".//input[@value='Team Members']"));
      teamMemberButton.click();
      return new ManageLanguageTeamMemberPage(getDriver());
   }

   private TableRow findRowByLocale(final String localeId)
   {
      TableRow matchedRow = waitForSeconds(getDriver(), 20).until(new Function<WebDriver, TableRow>()
      {
         @Override
         public TableRow apply(WebDriver driver)
         {
            List<TableRow> tableRows = WebElementUtil.getTableRows(languageTable);
            Collection<TableRow> matchedRow = Collections2.filter(tableRows, new Predicate<TableRow>()
            {
               @Override
               public boolean apply(TableRow input)
               {
                  List<String> cellContents = input.getCellContents();
                  String localeCell = cellContents.get(LOCALE_COLUMN).trim();
                  return localeCell.equalsIgnoreCase(localeId);
               }
            });

            log.debug("for locale [{}] found table row: {}", localeId, matchedRow);
            //we keep looking for the locale until timeout
            return (matchedRow.size() == 1) ? matchedRow.iterator().next() : null;
         }
      });
      return matchedRow;
   }

   public ManageLanguagePage joinLanguageTeam()
   {
      // Waiting 10 seconds for an element to be present on the page, checking
      // for its presence once every 1 second.
      WebElement joinLanguageTeamLink = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(By.linkText("Join Language Team"));
         }
      });
      joinLanguageTeamLink.click();
      return this;
   }

   public ManageLanguagePage enableLanguageByDefault(String localeId)
   {
      TableRow matchedRow = findRowByLocale(localeId);

      WebElement enabledCell = matchedRow.getCells().get(3);
      WebElement enabledCheckbox = enabledCell.findElement(By.tagName("input"));
      if(!enabledCheckbox.isSelected())
      {
         enabledCheckbox.click();
      }

      return this;
   }

   public List<String> getLanguageLocales()
   {
      List<TableRow> languages = WebElementUtil.getTableRows(languageTable);
      return WebElementUtil.getColumnContents(languages, LOCALE_COLUMN);
   }
}
