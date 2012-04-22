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
package org.zanata.action;

import org.openqa.selenium.WebDriver;
import org.zanata.page.HomePage;
import org.zanata.page.WebDriverFactory;

public class AbstractAction
{
   protected final WebDriver driver;
   protected final String hostUrl;

   public AbstractAction()
   {
      String baseUrl = WebDriverFactory.INSTANCE.getHostUrl();
      hostUrl = appendTrailingSlash(baseUrl);
      driver = WebDriverFactory.INSTANCE.getDriver();
      driver.get(hostUrl);
   }

   public HomePage goToHome()
   {
      driver.get(hostUrl);
      return new HomePage(driver);
   }

   private static String appendTrailingSlash(String baseUrl)
   {
      if (baseUrl.endsWith("/"))
      {
         return baseUrl;
      }
      return baseUrl + "/";
   }

   public String toUrl(String relativeUrl)
   {
      return hostUrl + removeLeadingSlash(relativeUrl);
   }

   private static String removeLeadingSlash(String relativeUrl)
   {
      if (relativeUrl.startsWith("/"))
      {
         return relativeUrl.substring(1, relativeUrl.length());
      }
      return relativeUrl;
   }
}
