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
package org.zanata.page.utility;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.administration.EditHomeCodePage;
import org.zanata.page.administration.EditHomeContentPage;

@Slf4j
public class HomePage extends BasePage
{

   @FindBy(id = "main_body_content")
   private WebElement mainBodyContent;

   public HomePage(final WebDriver driver)
   {
      super(driver);
   }

   @Deprecated
   // home page replace by dashboard
   public EditHomeContentPage goToEditPageContent()
   {
      getDriver().findElement(By.linkText("Edit Page Content")).click();
      return new EditHomeContentPage(getDriver());
   }

   @Deprecated
   // home page replace by dashboard
   public EditHomeCodePage goToEditPageCode()
   {
      getDriver().findElement(By.linkText("Edit Page Code")).click();
      return new EditHomeCodePage(getDriver());
   }

   @Deprecated
   // home page replace by dashboard
   public String getMainBodyContent()
   {
      return mainBodyContent.getText();
   }

   public boolean containActivityListSection()
   {
      return getDriver().findElement(By.id("activityList")) != null;
   }

   public boolean containMyMaintainedProjectsSection()
   {
      return getDriver().findElement(By.id("maintainedProject")) != null;
   }

   public List<WebElement> getMyActivityList()
   {
      WebElement listWrapper = getDriver().findElement(By.id("activityList")).findElement(By.tagName("ul"));

      if (listWrapper != null)
      {
         return listWrapper.findElements(By.xpath("./li"));
      }
      return new ArrayList<WebElement>();
   }

   public List<WebElement> getMyMaintainedProject()
   {
      WebElement listWrapper = getDriver().findElement(By.id("maintainedProject")).findElement(By.tagName("ul"));

      if (listWrapper != null)
      {
         return listWrapper.findElements(By.xpath("./li"));
      }
      return new ArrayList<WebElement>();
   }
}
