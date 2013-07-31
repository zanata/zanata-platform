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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.zanata.page.account.MyAccountPage;
import org.zanata.page.account.RegisterPage;
import org.zanata.page.account.SignInPage;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.page.utility.HomePage;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 *
 * This is a basic page that contains features all pages of Zanata share, eg.
 * the main navigation menu.
 */
@Slf4j
public class BasePage extends AbstractPage
{
   private List<WebElement> navMenuItems = Collections.emptyList();

   @FindBy(id = "nav-main")
   WebElement navBar;

   @FindBy(id = "projects_link")
   private WebElement projectsLink;

   @FindBy(id = "version-groups_link")
   private WebElement groupsLink;

   @FindBy(id = "languages_link")
   private WebElement languagesLink;

   @FindBy(id = "user_avatar")
   private WebElement userAvatar;
   
   @FindBy(id = "home")
   private WebElement homeLink;
   
   private static final By BY_SIGN_IN = By.id("signin_link");
   private static final By BY_SIGN_OUT = By.id("right_menu_sign_out_link");
   private static final By BY_PROFILE_LINK = By.id("profile");
   private static final By BY_ADMINISTRATION_LINK = By.id("administration");
   
   public BasePage(final WebDriver driver)
   {
      super(driver);
      navMenuItems = navBar.findElements(By.tagName("a"));
   }

   public MyAccountPage goToMyProfile()
   {
      userAvatar.click();
      waitForSideMenuOpened();
      
      clickLinkAfterAnimation(BY_PROFILE_LINK);

      return new MyAccountPage(getDriver());
   }
   
   public HomePage goToHomePage()
   {
      homeLink.click();
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
      userAvatar.click();
      waitForSideMenuOpened();
      
      clickLinkAfterAnimation(BY_ADMINISTRATION_LINK);

      return new AdministrationPage(getDriver());
   }

   public RegisterPage goToRegistration()
   {
	Preconditions.checkArgument(!hasLoggedIn(),
            "User has logged in! You should sign out or delete cookie first in your test.");
	
      WebElement registerLink = getDriver().findElement(By.id("register_link_internal_auth"));
      registerLink.click();
      return new RegisterPage(getDriver());
   }

   public SignInPage clickSignInLink()
   {
      WebElement signInLink = getDriver().findElement(BY_SIGN_IN);
      signInLink.click();
      return new SignInPage(getDriver());
   }

   public boolean hasLoggedIn()
   {
      List<WebElement> signInLink = getDriver().findElements(BY_SIGN_IN);
      return signInLink.size() == 0;
   }

   public String loggedInAs()
   {
      return userAvatar.getAttribute("title");
   }

   public HomePage logout()
   {
      userAvatar.click();

      waitForSideMenuOpened();

      clickLinkAfterAnimation(BY_SIGN_OUT);
      return new HomePage(getDriver());
   }

   public List<String> getBreadcrumbLinks()
   {
      List<WebElement> breadcrumbs = getDriver().findElement(By.id("breadcrumbs_panel")).findElements(By.className("breadcrumbs_link"));
      return WebElementUtil.elementsToText(breadcrumbs);
   }

   public String getLastBreadCrumbText()
   {
      WebElement breadcrumb = getDriver().findElement(By.id("breadcrumbs_panel")).findElement(By.className("breadcrumbs_display"));
      return breadcrumb.getText();
   }

   public <P> P clickBreadcrumb(final String link, Class<P> pageClass)
   {
      List<WebElement> breadcrumbs = getDriver().findElement(By.id("breadcrumbs_panel")).findElements(By.className("breadcrumbs_link"));
      Predicate<WebElement> predicate = new Predicate<WebElement>()
      {
         @Override
         public boolean apply(WebElement input)
         {
            return input.getText().equals(link);
         }
      };
      Optional<WebElement> breadcrumbLink = Iterables.tryFind(breadcrumbs, predicate);
      if (breadcrumbLink.isPresent())
      {
         breadcrumbLink.get().click();
         return PageFactory.initElements(getDriver(), pageClass);
      }
      throw new RuntimeException("can not find " + link + " in breadcrumb: " + WebElementUtil.elementsToText(breadcrumbs));
   }

   public List<String> getNavigationMenuItems()
   {
      Collection<String> linkTexts = Collections2.transform(navMenuItems, new Function<WebElement, String>() {
         @Override
         public String apply(WebElement link) {
            return link.getText();
         }
      });
      return ImmutableList.copyOf(linkTexts);
   }

   public <P> P goToPage(String navLinkText, Class<P> pageClass)
   {
      log.info("click {} and go to page {}", navLinkText, pageClass.getName());
      List<String> navigationMenuItems = getNavigationMenuItems();
      int menuItemIndex = navigationMenuItems.indexOf(navLinkText);

      Preconditions.checkState(menuItemIndex >= 0, navLinkText + " is not available in navigation menu");

      navMenuItems.get(menuItemIndex).click();
      return PageFactory.initElements(getDriver(), pageClass);
   }

   public String getNotificationMessage()
   {
      List<WebElement> messages = getDriver().findElements(By.id("messages"));
      return messages.size() > 0 ? messages.get(0).getText() : "";
   }

   public List<String> waitForErrors()
   {
      waitForTenSec().until(new Function<WebDriver, WebElement>()
      {
         @Override
         public WebElement apply(WebDriver driver)
         {
            return getDriver().findElement(By.xpath("//span[@class='errors']"));
         }
      });
      return getErrors();
   }
   
   /**
    * This is a workaround for https://code.google.com/p/selenium/issues/detail?id=2766
    * Elemenet not clickable at point due to the change coordinate of element in page.
    * @param locator
    */
   public void clickLinkAfterAnimation(By locator)
   {
      JavascriptExecutor executor = (JavascriptExecutor) getDriver();
      executor.executeScript("arguments[0].click();", getDriver().findElement(locator));
   }

   public void waitForSideMenuClosed()
   {
      WebElementUtil.waitForTenSeconds(getDriver()).until(
            ExpectedConditions.invisibilityOfElementLocated(By.className("off-canvas--right-under")));
   }
   
   public void waitForSideMenuOpened()
   {
      WebElementUtil.waitForTenSeconds(getDriver()).until(
            ExpectedConditions.visibilityOfElementLocated(By.className("off-canvas--right-under")));
   }

   public void assertNoCriticalErrors()
   {
      List<WebElement> errors = getDriver().findElements(By.id("errorMessage"));
      if (errors.size() > 0)
      {
         throw new RuntimeException("Critical error: \n"+errors.get(0).getText());
      }
   }

}
