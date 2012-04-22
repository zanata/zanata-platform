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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.google.common.base.Function;

public class ManageLanguagePage extends AbstractPage
{

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
      //FIXME this part is not working
      List<WebElement> languageTableRows = getDriver().findElements(By.className("rich-table-row"));
      for (WebElement row : languageTableRows)
      {
         List<WebElement> tableCells = row.findElements(By.tagName("td"));
         if (tableCells.get(0).getText().contains(localeId))
         {
            WebElement teamMembersButton = tableCells.get(3).findElement(By.xpath("//input[@value='Team Members']"));
            teamMembersButton.click();
            return this;
         }
      }
      throw new RuntimeException("can't find localeId");
   }

   public ManageLanguagePage joinLanguageTeam()
   {
      // Waiting 30 seconds for an element to be present on the page, checking
      // for its presence once every 1 second.
      WebElement joinLanguageTeamLink = ajaxWait().until(new Function<WebDriver, WebElement>()
      {
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(By.linkText("Join Language Team"));
         }
      });
      joinLanguageTeamLink.click();
      return this;
   }

}
