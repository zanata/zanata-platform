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
import org.zanata.page.account.RegisterPage;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectsPage;
import com.google.common.base.Function;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HomePage extends AbstractPage
{
   private static final By BY_SIGN_IN = By.id("Sign_in");
   private static final By BY_SIGN_OUT = By.id("Sign_out");

   @FindBy(id = "Projects")
   private WebElement projectsLink;

   @FindBy(id = "version-groups")
   private WebElement groupsLink;

   @FindBy(id = "userCol")
   private WebElement userColumn;

   public HomePage(final WebDriver driver)
   {
      super(driver);
   }

   public SignInPage clickSignInLink()
   {
      log.info("header text: {}", userColumn.getText());
      WebElement signInLink = userColumn.findElement(BY_SIGN_IN);
      signInLink.click();
      return new SignInPage(getDriver());
   }

   public boolean hasLoggedIn()
   {
      List<WebElement> signOutLink = getDriver().findElements(BY_SIGN_IN);
      return signOutLink.size() == 0;
   }

   public String loggedInAs()
   {
      WebElement username = userColumn.findElement(By.className("username"));
      return username.getText().trim();
   }

   public HomePage signOut()
   {
      userColumn.click();
      WebElement signOut = userColumn.findElement(BY_SIGN_OUT);
      signOut.click();
      waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         @Override
         public WebElement apply(WebDriver driver)
         {
            return driver.findElement(BY_SIGN_IN);
         }
      });
      return new HomePage(getDriver());
   }

   public ProjectsPage goToProjects()
   {
      projectsLink.click();
      return new ProjectsPage(getDriver());
   }

   public VersionGroupsPage goToGroups()
   {
      groupsLink.click();
      return new VersionGroupsPage(getDriver());
   }

   public AdministrationPage goToAdministration()
   {
      getDriver().findElement(By.linkText("More")).click();
      WebElement adminLink = getDriver().findElement(By.id("Administration"));
      adminLink.click();
      return new AdministrationPage(getDriver());
   }

   public RegisterPage goToRegistration()
   {
      getDriver().findElement(By.linkText("More")).click();
      WebElement registerLink = getDriver().findElement(By.id("Register"));
      registerLink.click();
      return new RegisterPage(getDriver());
   }
}
