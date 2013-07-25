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
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractPage
{
   private final WebDriver driver;
   private final FluentWait<WebDriver> ajaxWaitForTenSec;

   public void deleteCookies()
   {
      getDriver().manage().deleteAllCookies();
   }

   public AbstractPage(final WebDriver driver)
   {
      PageFactory.initElements(new AjaxElementLocatorFactory(driver, 10), this);
      this.driver = driver;
      ajaxWaitForTenSec = WebElementUtil.waitForTenSeconds(driver);
   }

   public WebDriver getDriver()
   {
      return driver;
   }

   public String getTitle()
   {
      return driver.getTitle();
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
   
   protected void clickAndCheckErrors(WebElement button)
   {
      button.click();
      List<String> errors = getErrors();
      if (!errors.isEmpty())
      {
         throw new RuntimeException(Joiner.on(";").join(errors));
      }
   }

   protected void clickAndExpectErrors(WebElement button)
   {
      button.click();
      List<String> errors = getErrors();
      if (errors.isEmpty())
      {
         throw new RuntimeException("Errors expected, none found.");
      }
   }

   public List<String> getErrors()
   {
      List<WebElement> errorSpans = getDriver().findElements(By.xpath("//span[@class='errors']"));
      return WebElementUtil.elementsToText(errorSpans);
   }

   public List<String> getExpectedNumberOfErrors(final int expectedNumber)
   {
      refreshPageUntil(this, new Predicate<WebDriver>()
      {
         @Override
         public boolean apply(WebDriver input)
         {
            return getErrors().size() == expectedNumber;
         }
      });
      return getErrors();
   }

   /*
    * Wait for all necessary entities to be available
    */
   public void waitForPage(List<By> bys) {
      for (final By by : bys) {
         waitForTenSec().until(new Function<WebDriver, WebElement>()
         {
            @Override
            public WebElement apply(WebDriver driver)
            {
               return getDriver().findElement(by);
            }
         });
      }
   }

   protected <P extends AbstractPage> P refreshPageUntil(P currentPage, Predicate<WebDriver> predicate)
   {
      waitForTenSec().until(predicate);
      PageFactory.initElements(driver, currentPage);
      return currentPage;
   }

   protected <P extends AbstractPage, T> T refreshPageUntil(P currentPage, Function<WebDriver, T> function)
   {
      T done = waitForTenSec().until(function);
      PageFactory.initElements(driver, currentPage);
      return done;
   }

}
