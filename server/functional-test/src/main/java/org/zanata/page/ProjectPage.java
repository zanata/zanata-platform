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
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ProjectPage extends AbstractPage
{
   @FindBy(id = "main_content")
   private WebElement mainContent;
   private final List<WebElement> h1;

   @FindBy(linkText = "Create Version")
   private WebElement createVersionLink;

   @FindBy(className = "version_link", tagName = "a")
   private List<WebElement> versionLinks;

   public ProjectPage(final WebDriver driver)
   {
      super(driver);
      h1 = mainContent.findElements(By.tagName("h1"));
      Preconditions.checkState(h1.size() >= 2, "should have at least 2 <h1> under main content");
   }

   public String getProjectId()
   {
      return h1.get(0).getText();
   }

   public String getProjectName()
   {
      return h1.get(1).getText();
   }

   public CreateVersionPage clickCreateVersionLink()
   {
      createVersionLink.click();
      return new CreateVersionPage(getDriver());
   }

   public ProjectVersionPage goToActiveVersion(final String versionId)
   {
      Preconditions.checkState(versionLinks != null && !versionLinks.isEmpty(), "no version links available");
      Collection<WebElement> found = Collections2.filter(versionLinks, new Predicate<WebElement>()
      {
         @Override
         public boolean apply(WebElement input)
         {
            return input.getText().equals(versionId);
         }
      });
      Preconditions.checkState(found.size() != 1, versionId + " not found");
      found.iterator().next().click();
      return new ProjectVersionPage(getDriver());
   }
}
