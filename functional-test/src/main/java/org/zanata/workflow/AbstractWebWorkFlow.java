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
package org.zanata.workflow;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.zanata.page.AbstractPage;
import org.zanata.page.HomePage;
import org.zanata.page.WebDriverFactory;

public class AbstractWebWorkFlow
{
   protected final WebDriver driver;
   protected final String hostUrl;

   public AbstractWebWorkFlow()
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

   public <P extends AbstractPage> P getCurrentPage(Class<P> pageClass)
   {
      return PageFactory.initElements(driver, pageClass);
   }
}
