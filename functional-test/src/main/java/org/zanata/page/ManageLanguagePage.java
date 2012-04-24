/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.page;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class ManageLanguagePage extends AbstractPage
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ManageLanguagePage.class);

   public ManageLanguagePage(WebDriver driver)
   {
      super(driver);
   }

   public AddLanguagePage addNewLanguage()
   {
      getDriver().findElement(By.linkText("Add New Language")).click();
      return new AddLanguagePage(getDriver());
   }

   public ManageLanguagePage manageTeamMembersFor(String localeId)
   {
      List<WebElement> languageTableRows = getDriver().findElements(By.className("rich-table-row"));
      for (int i = 0, languageTableRowsSize = languageTableRows.size(); i < languageTableRowsSize; i++)
      {
         WebElement row = languageTableRows.get(i);
         LOGGER.info("tabel row is: {}", row.getText());
         if (row.getText().contains(localeId))
         {
            LOGGER.info("about to click team members button #{}", i);
            List<WebElement> teamMembersButtons = row.findElements(By.xpath("//input[@value='Team Members']"));
            teamMembersButtons.get(i).click();
            return this;
         }
      }
      throw new RuntimeException("can't find localeId");
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

   public List<String> getMemberUsernames()
   {
      List<WebElement> elements = getDriver().findElements(By.className("rich-table-row"));
      Collection<String> rows = Collections2.transform(elements, new Function<WebElement, String>()
      {
         @Override
         public String apply(WebElement from)
         {
            String text = from.getText();
            return text.split("\\s")[0];
         }
      });
      return ImmutableList.copyOf(rows);
   }

}
