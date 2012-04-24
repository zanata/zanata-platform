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
package org.zanata.util;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class WebElementUtil
{
   private WebElementUtil()
   {
   }

   private static final Function<WebElement, String> ELEMENT_TO_TEXT_FUNCTION = new Function<WebElement, String>()
   {
      @Override
      public String apply(WebElement from)
      {
         return from.getText();
      }
   };

   public static List<String> elementsToText(Collection<WebElement> webElements)
   {
      return ImmutableList.copyOf(Collections2.transform(webElements, ELEMENT_TO_TEXT_FUNCTION));
   }

   public static String getInnerHTML(WebDriver driver, WebElement element)
   {
      return (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", element);
   }

   public static List<String> elementsToInnerHTML(WebDriver driver, Collection<WebElement> webElements)
   {
      return ImmutableList.copyOf(Collections2.transform(webElements, new WebElementInnerHTMLFunction(driver)));
   }

   static class WebElementInnerHTMLFunction implements Function<WebElement, String>
   {
      private final WebDriver driver;

      private WebElementInnerHTMLFunction(WebDriver driver)
      {
         this.driver = driver;
      }

      @Override
      public String apply(WebElement from)
      {
         return getInnerHTML(driver, from);
      }

   }
}
