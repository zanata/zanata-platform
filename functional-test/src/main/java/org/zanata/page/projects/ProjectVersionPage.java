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
package org.zanata.page.projects;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.webtrans.DocumentsViewPage;

import java.util.List;

@Slf4j
public class ProjectVersionPage extends BasePage
{
   @FindBy(id = "iterationLanguageForm:data_table:tb")
   private WebElement localeTableTBody;

   public ProjectVersionPage(final WebDriver driver)
   {
      super(driver);
   }

   public String getVersionId()
   {
      return getLastBreadCrumbText();
   }
   
   @SuppressWarnings("unused")
   public List<String> getTranslatableLocales()
   {
      List<WebElement> tableRows = getLocaleTableRows();
      List<String> rows = Lists.transform(tableRows, new Function<WebElement, String>()
      {
         @Override
         public String apply(WebElement tr)
         {
            log.debug("table row: {}", tr.getText());
            List<WebElement> links = tr.findElements(By.tagName("a"));
            return getLocaleLinkText(links.get(0));
         }
      });

      return ImmutableList.copyOf(rows);
   }

   private List<WebElement> getLocaleTableRows()
   {
      return localeTableTBody.findElements(By.tagName("tr"));
   }

   @SuppressWarnings("unused")
   public List<String> getTranslatableLanguages()
   {
      List<WebElement> tableRows = getLocaleTableRows();
      List<String> rows = Lists.transform(tableRows, new Function<WebElement, String>()
      {
         @Override
         public String apply(WebElement tr)
         {
            log.debug("table row: {}", tr.getText());
            WebElement nativeName = tr.findElement(By.className("nativeName"));
            return nativeName.getText();
         }
      });

      return ImmutableList.copyOf(rows);
   }

   private static String getLocaleLinkText(WebElement languageLink)
   {
      String nativeName = languageLink.findElement(By.className("nativeName")).getText();
      return languageLink.getText().replace(nativeName, "");
   }

   public DocumentsViewPage translate(String locale)
   {
      List<WebElement> localeTableRows = getLocaleTableRows();
      for (WebElement tableRow : localeTableRows)
      {
         List<WebElement> links = tableRow.findElements(By.tagName("a"));
         WebElement localeCell = links.get(0);
         if (getLocaleLinkText(localeCell).equals(locale))
         {
            localeCell.click();
            return new DocumentsViewPage(getDriver());
         }
      }
      throw new IllegalArgumentException("can not translate locale: " + locale);
   }
}
