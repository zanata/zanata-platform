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
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class HomePage extends AbstractPage
{
   private static final Logger LOGGER = LoggerFactory.getLogger(HomePage.class);

   @FindBy(id = "Projects")
   private WebElement projectsLink;

   @FindBy(id = "header_top_right")
   private WebElement headerTopRightDiv;

   public HomePage(final WebDriver driver)
   {
      super(driver);
   }

   public SignInPage clickSignInLink()
   {
      LOGGER.info("header text: {}", headerTopRightDiv.getText());
      List<WebElement> links = headerTopRightDiv.findElements(By.tagName("a"));
      WebElement firstLink = links.get(0);
      if (firstLink.getText().equalsIgnoreCase("Sign In"))
      {
          firstLink.click();
      }
      //else already signed in, no op
      return PageFactory.initElements(getDriver(), SignInPage.class);
   }

   public boolean hasLoggedIn()
   {
      LOGGER.info("header text: {}", headerTopRightDiv.getText());
      return headerTopRightDiv.getText().contains("Sign Out");
   }

   public String loggedInAs()
   {
      String[] parts = headerTopRightDiv.getText().split("\\s");
      return parts[0];
   }

   public ProjectsPage goToProjects()
   {
      projectsLink.click();
      return PageFactory.initElements(getDriver(), ProjectsPage.class);
   }
}
