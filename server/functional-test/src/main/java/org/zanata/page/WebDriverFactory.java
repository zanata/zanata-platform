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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

public enum WebDriverFactory
{
   INSTANCE;

   private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverFactory.class);

   private WebDriver driver;
   private Properties properties;

   public WebDriver getDriver()
   {
      if (driver == null)
      {
         synchronized (this)
         {
            if (driver == null)
            {
               loadProperties();
               driver = createDriver();
            }
         }
      }
      return driver;
   }

   private void loadProperties()
   {
      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("setup.properties");
      properties = new Properties();
      try
      {
         properties.load(inputStream);
      } catch (IOException e)
      {
         LOGGER.error("can't load setup.properties");
         throw new IllegalStateException("can't load setup.properties");
      }
   }

   public String getHostUrl()
   {
      if (properties == null || driver == null)
      {
         getDriver();
      }
      return properties.getProperty("zanata.instance.url");
   }

   private WebDriver createDriver()
   {
      String driverType = properties.getProperty("webdriver.type", "htmlUnit");
      if (driverType.equalsIgnoreCase("chrome"))
      {
         return configureChromeDriver();
      } else if (driverType.equalsIgnoreCase("fireFox"))
      {
         return configureFirefoxDriver();
      } else
      {
         return configureHtmlDriver();
      }
   }

   private WebDriver configureHtmlDriver()
   {
      return new HtmlUnitDriver(true);
   }

   private WebDriver configureChromeDriver()
   {
      System.getProperties().put("webdriver.chrome.bin", "/opt/google/chrome/google-chrome");
      return new ChromeDriver();
   }

   private WebDriver configureFirefoxDriver()
   {
//      final String pathToFirefox = Strings.emptyToNull(props.getProperty("functionaltest.browser.path"));

      FirefoxBinary firefoxBinary = null;
//      if (pathToFirefox != null)
//      {
//         firefoxBinary = new FirefoxBinary(new File(pathToFirefox));
//      } else
//      {
      firefoxBinary = new FirefoxBinary();
//      }

//      return new FirefoxDriver(firefoxBinary, makeFirefoxProfile());
      return new FirefoxDriver();
   }


   private FirefoxProfile makeFirefoxProfile()
   {
      if (!Strings.isNullOrEmpty(System.getProperty("webdriver.firefox.profile")))
      {
         throw new RuntimeException("webdriver.firefox.profile is ignored");
         // TODO - look at FirefoxDriver.getProfile().
      }
      final FirefoxProfile firefoxProfile = new FirefoxProfile();
//        firefoxProfile.setPreference("browser.safebrowsing.malware.enabled", false); // disables connection to sb-ssl.google.com
      firefoxProfile.setAlwaysLoadNoFocusLib(true);
      firefoxProfile.setEnableNativeEvents(true);
      return firefoxProfile;
   }

   static enum BrowserType
   {
      Chrome, FireFox, HtmlUnit
   }
}
