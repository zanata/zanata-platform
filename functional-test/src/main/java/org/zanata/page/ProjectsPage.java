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
package org.zanata.page;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;

public class ProjectsPage extends AbstractPage
{
   @FindBy(id = "main_content")
   private WebElement mainContentDiv;

   public ProjectsPage(final WebDriver driver)
   {
      super(driver);
   }

   public CreateProjectPage clickOnCreateProjectLink()
   {
      WebElement createProjectActionLink = waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         @Override
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(By.linkText("Create project"));
         }
      });
      createProjectActionLink.click();
      return new CreateProjectPage(getDriver());
   }

   public ProjectPage goToProject(String projectName)
   {
      //TODO this can't handle project on different page
      WebElement link = getDriver().findElement(By.linkText(projectName));
      link.click();
      return new ProjectPage(getDriver());
   }

   public List<String> getProjectNamesOnCurrentPage()
   {
      if (mainContentDiv.getText().contains("No project exists"))
      {
         return Collections.emptyList();
      }
      WebElement table = getDriver().findElement(By.className("rich-table"));
      List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr/td[1]"));
      return WebElementUtil.elementsToText(rows);
   }
}
