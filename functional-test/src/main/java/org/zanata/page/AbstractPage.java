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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.zanata.util.WebElementUtil;

import java.util.List;
import java.util.Set;

@Slf4j
public class AbstractPage
{
   private final WebDriver driver;
   private final FluentWait<WebDriver> ajaxWaitForTenSec;

   public void deleteCookiesAndRefresh()
   {
      getDriver().manage().deleteAllCookies();
      Set<Cookie> cookies = getDriver().manage().getCookies();
      if (cookies.size() > 0)
      {
         log.warn("Failed to delete cookies: {}", cookies);
      }
      getDriver().navigate().refresh();
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

   public String getUrl()
   {
      return driver.getCurrentUrl();
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
      refreshPageUntil(this, new Predicate<WebDriver>()
      {
         @Override
         public boolean apply(WebDriver input)
         {
            return getErrors().size() > 0;
         }
      });
   }

   public List<String> getErrors()
   {
      List<WebElement> errorSpans = getDriver().findElements(By.xpath("//span[@class='errors']"));
      return WebElementUtil.elementsToText(errorSpans);
   }

   /**
    * Wait until expected number of errors presented on page or timeout.
    *
    * @param expectedNumber expected number of errors on page
    * @return list of error message
    */
   public List<String> getErrors(final int expectedNumber)
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

   /**
    * Wait for all necessary elements to be available on page.
    *
    * @param elementBys selenium search criteria for locating elements
    */
   public void waitForPage(List<By> elementBys)
   {
      for (final By by : elementBys)
      {
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

   public Alert switchToAlert()
   {
      return waitForTenSec().until(new Function<WebDriver, Alert>() {
         @Override
         public Alert apply(WebDriver driver) {
            try {
               return getDriver().switchTo().alert();
            }
            catch (NoAlertPresentException noAlertPresent)
            {
               return null;
            }
         }
      });
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
