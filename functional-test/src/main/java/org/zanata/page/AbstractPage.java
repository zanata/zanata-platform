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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractPage
{
   private final WebDriver driver;
   private final FluentWait<WebDriver> ajaxWaitForTenSec;
   private List<WebElement> navMenuItems = Collections.emptyList();

   @FindBy(className = "navBar")
   WebElement navBar;

   public AbstractPage(final WebDriver driver)
   {
      PageFactory.initElements(new AjaxElementLocatorFactory(driver, 10), this);
      this.driver = driver;
      ajaxWaitForTenSec = WebElementUtil.waitForTenSeconds(driver);
      navMenuItems = navBar.findElements(By.tagName("a"));
   }

   public WebDriver getDriver()
   {
      return driver;
   }

   public String getTitle()
   {
      return driver.getTitle();
   }

   public List<String> getBreadcrumbs()
   {
      List<WebElement> breadcrumbs = driver.findElement(By.id("breadcrumbs_panel")).findElements(By.className("breadcrumbs_display"));
      return WebElementUtil.elementsToText(breadcrumbs);
   }

   public List<String> getNavigationMenuItems()
   {
      Collection<String> linkTexts = Collections2.transform(navMenuItems, new Function<WebElement, String>()
      {
         @Override
         public String apply(WebElement link)
         {
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
      return PageFactory.initElements(driver, pageClass);
   }

   // TODO this doesn't seem useful
   public <P> P goToUrl(String url, P page)
   {
      log.info("go to url: {}", url);
      driver.get(url);
      PageFactory.initElements(new AjaxElementLocatorFactory(driver, 30), page);
      return page;
   }

   public FluentWait<WebDriver> waitForTenSec()
   {
      return ajaxWaitForTenSec;
   }
   
   protected void clickSaveAndCheckErrors(WebElement saveButton)
   {
      saveButton.click();
      List<String> errors = getErrors();
      if (!errors.isEmpty())
      {
         throw new RuntimeException(Joiner.on(";").join(errors));
      }
   }

   protected List<String> getErrors()
   {
      List<WebElement> errorSpans = getDriver().findElements(By.xpath("//span[@class='errors']"));
      return WebElementUtil.elementsToText(errorSpans);
   }

}
