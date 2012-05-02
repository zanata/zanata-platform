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
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class ProjectVersionPage extends AbstractPage
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ProjectVersionPage.class);

   @FindBy(className = "rich-table-row")
   private List<WebElement> localeTableRows;

   public ProjectVersionPage(final WebDriver driver)
   {
      super(driver);
   }

   public List<String> getTranslatableLocales()
   {
//      List<WebElement> tableRows = localeTable.findElements(By.className("rich-table-row"));
      Collection<String> rows = Collections2.transform(localeTableRows, new Function<WebElement, String>()
      {
         @Override
         public String apply(WebElement tr)
         {
            LOGGER.debug("table row: {}", tr.getText());
            List<WebElement> links = tr.findElements(By.tagName("a"));
            return getLocaleLinkText(links);
         }
      });

      return ImmutableList.copyOf(rows);
   }

   private static String getLocaleLinkText(List<WebElement> links)
   {
      return links.get(0).getText();
   }

   public WebTranPage translate(String locale)
   {

      for (WebElement tableRow : localeTableRows)
      {
         List<WebElement> links = tableRow.findElements(By.tagName("a"));
         Preconditions.checkState(links.size() == 4, "each translatable locale row should have 4 links");

         if (getLocaleLinkText(links).equals(locale))
         {
            WebElement translateLink = links.get(2);
            translateLink.click();
            return new WebTranPage(getDriver());
         }
      }
      throw new IllegalArgumentException("can not translate locale: " + locale);
   }
}
