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
package org.zanata.util;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static java.util.concurrent.TimeUnit.*;

public class WebElementUtil
{
   private WebElementUtil()
   {
   }

   public static List<String> elementsToText(Collection<WebElement> webElements)
   {
      return ImmutableList.copyOf(Collections2.transform(webElements, WebElementToTextFunction.FUNCTION));
   }

   public static String getInnerHTML(WebDriver driver, WebElement element)
   {
      return (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", element);
   }

   public static List<String> elementsToInnerHTML(WebDriver driver, Collection<WebElement> webElements)
   {
      return ImmutableList.copyOf(Collections2.transform(webElements, new WebElementToInnerHTMLFunction(driver)));
   }

   public static List<TableRow> getTableRows(WebDriver driver, final By byQueryForTable)
   {
      return waitForTenSeconds(driver).until(new Function<WebDriver, List<TableRow>>()
      {
         @Override
         public List<TableRow> apply(WebDriver webDriver)
         {
            final WebElement table = webDriver.findElement(byQueryForTable);
            List<WebElement> rows = table.findElements(By.xpath(".//tbody[1]/tr"));
            return ImmutableList.copyOf(Lists.transform(rows, WebElementTableRowFunction.FUNCTION));
         }
      });
   }

   public static List<TableRow> getTableRows(WebDriver driver, final WebElement table)
   {
      return waitForTenSeconds(driver).until(new Function<WebDriver, List<TableRow>>()
      {
         @Override
         public List<TableRow> apply(WebDriver webDriver)
         {

            List<WebElement> rows = table.findElements(By.xpath(".//tbody[1]/tr"));
            return ImmutableList.copyOf(Lists.transform(rows, WebElementTableRowFunction.FUNCTION));
         }
      });
   }

   public static ImmutableList<List<String>> transformToTwoDimensionList(List<TableRow> tableRows)
   {
      return ImmutableList.copyOf(Lists.transform(tableRows, new Function<TableRow, List<String>>()
      {
         @Override
         public List<String> apply(TableRow from)
         {
            return from.getCellContents();
         }
      }));
   }

   public static FluentWait<WebDriver> waitForSeconds(WebDriver webDriver, int durationInSec)
   {
      return new FluentWait<WebDriver>(webDriver).withTimeout(durationInSec, SECONDS).pollingEvery(1, SECONDS).ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
   }

   public static FluentWait<WebDriver> waitForTenSeconds(WebDriver webDriver)
   {
      return waitForSeconds(webDriver, 10);
   }

   public static List<String> getColumnContents(WebDriver driver, final By by, final int columnIndex)
   {
      return waitForTenSeconds(driver).until(new Function<WebDriver, List<String>>()
      {
         @Override
         public List<String> apply(WebDriver input)
         {
            WebElement table = input.findElement(by);
            List<WebElement> rows = table.findElements(By.xpath(".//tbody[1]/tr"));
            List<TableRow> tableRows = Lists.transform(rows, WebElementTableRowFunction.FUNCTION);
            return ImmutableList.copyOf(Lists.transform(tableRows, new Function<TableRow, String>()
            {
               @Override
               public String apply(TableRow from)
               {
                  List<String> cellContents = from.getCellContents();
                  Preconditions.checkElementIndex(columnIndex, cellContents.size(), "column index");
                  return cellContents.get(columnIndex);
               }
            }));
         }
      });
   }

   public static List<List<String>> getTwoDimensionList(WebDriver driver, final By by)
   {
      return waitForTenSeconds(driver).until(new Function<WebDriver, List<List<String>>>()
      {
         @Override
         public List<List<String>> apply(WebDriver input)
         {
            final WebElement table = input.findElement(by);
            List<WebElement> rows = table.findElements(By.xpath(".//tbody[1]/tr"));
            List<TableRow> tableRows = Lists.transform(rows, WebElementTableRowFunction.FUNCTION);
            return transformToTwoDimensionList(tableRows);
         }
      });
   }

   private static class WebElementToInnerHTMLFunction implements Function<WebElement, String>
   {
      private final WebDriver driver;

      public WebElementToInnerHTMLFunction(WebDriver driver)
      {
         this.driver = driver;
      }

      @Override
      public String apply(WebElement from)
      {
         return getInnerHTML(driver, from);
      }

   }

   private static enum WebElementTableRowFunction implements Function<WebElement, TableRow>
   {
      FUNCTION;

      @Override
      public TableRow apply(WebElement element)
      {
         return new TableRow(element);
      }
   }

   public static enum WebElementToTextFunction implements Function<WebElement, String>
   {
      FUNCTION;

      @Override
      public String apply(WebElement from)
      {
         return from.getText();
      }
   }
}
