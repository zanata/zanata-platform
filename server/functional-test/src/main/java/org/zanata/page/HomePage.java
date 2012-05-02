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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      // else already signed in, no op
      return new SignInPage(getDriver());
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
      return new ProjectsPage(getDriver());
   }

   public AdministrationPage goToAdministration()
   {
      WebElement adminLink = getDriver().findElement(By.id("Administration"));
      adminLink.click();
      return new AdministrationPage(getDriver());
   }
}
