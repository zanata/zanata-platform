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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class AbstractPage
{
   public enum PageContext
   {
      jsf, webTran
   }
   private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPage.class);

   private final WebDriver driver;
   private final FluentWait<WebDriver> ajaxWaitForTenSec;
   private List<WebElement> navMenuItems = Collections.emptyList();

   public AbstractPage(final WebDriver driver)
   {
      this(driver, PageContext.jsf);
   }

   public AbstractPage(final  WebDriver driver, PageContext pageContext)
   {
      PageFactory.initElements(new AjaxElementLocatorFactory(driver, 30), this);
      this.driver = driver;
      ajaxWaitForTenSec = createWaitForAjax(driver, 10);
      if (pageContext == PageContext.jsf)
      {
         //webTran and jsp don't share same page layout
         navMenuItems = driver.findElement(By.id("tabs-menu")).findElements(By.tagName("a"));
      }
   }

   public static FluentWait<WebDriver> createWaitForAjax(WebDriver webDriver, int durationInSec)
   {
      return new FluentWait<WebDriver>(webDriver).withTimeout(durationInSec, SECONDS).pollingEvery(1, SECONDS).ignoring(NoSuchElementException.class);
   }

   public WebDriver getDriver()
   {
      return driver;
   }

   public String getTitle()
   {
      return driver.getTitle();
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
      LOGGER.info("click {} and go to page {}", navLinkText, pageClass.getName());
      List<String> navigationMenuItems = getNavigationMenuItems();
      int menuItemIndex = navigationMenuItems.indexOf(navLinkText);

      Preconditions.checkState(menuItemIndex >= 0, navLinkText + " is not available in navigation menu");

      navMenuItems.get(menuItemIndex).click();
      return PageFactory.initElements(driver, pageClass);
   }

   // TODO this doesn't seem useful
   public <P> P goToUrl(String url, P page)
   {
      LOGGER.info("go to url: {}", url);
      driver.get(url);
      PageFactory.initElements(new AjaxElementLocatorFactory(driver, 30), page);
      return page;
   }

   public FluentWait<WebDriver> waitForTenSec()
   {
      return ajaxWaitForTenSec;
   }
}
