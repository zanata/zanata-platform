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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

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

   public static List<TableRow> getTableRows(WebDriver driver, By byQueryForTable)
   {
      WebElement table = driver.findElement(byQueryForTable);
      Preconditions.checkArgument(table.getTagName().equalsIgnoreCase("table"), "By query must return a table");

      List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
      return ImmutableList.copyOf(Collections2.transform(rows, WebElementTableRowFunction.FUNCTION));
   }

   public static List<String> getColumnContents(List<TableRow> tableRows, final int columnIndex)
   {
      return ImmutableList.copyOf(Collections2.transform(tableRows, new Function<TableRow, String>()
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
