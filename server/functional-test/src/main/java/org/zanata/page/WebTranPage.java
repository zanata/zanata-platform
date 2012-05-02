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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class WebTranPage extends AbstractPage
{
   private static final Logger LOGGER = LoggerFactory.getLogger(WebTranPage.class);
   @FindBy(xpath = "//table[@class = 'DocumentListTable']")
   private WebElement documentListTable;

//   @FindBy(id = "gwt-debug-loadingIndicator")
//   private WebElement loadingIndicator;

   public WebTranPage(final WebDriver driver)
   {
      //web tran doesn't share same page layout as other page
      super(driver, PageContext.webTran);
   }

   public List<List<String>> getDocumentListTableContent()
   {

      List<WebElement> trs = null;
      trs = createWaitForAjax(getDriver(), 20).until(new Function<WebDriver, List<WebElement>>()
      {
         @Override
         public List<WebElement> apply(WebDriver from)
         {
//            if (loadingIndicator.isDisplayed())
//            {
//               return null;
//            }
            List<WebElement> trs = getDriver().findElements(By.xpath("//table[@class = 'DocumentListTable']/.//tr"));
            LOGGER.info("trs: {}", WebElementUtil.elementsToText(trs));
            //we assume there is at least one document
            return trs.size() < 3 || Strings.isNullOrEmpty(trs.get(0).getText()) ? null : trs;
         }
      });

      ImmutableList.Builder<List<String>> rowsBuilder = ImmutableList.builder();
      for (int i = 1, trsSize = trs.size(); i < trsSize; i++)
      {
         WebElement tr = trs.get(i);
         LOGGER.info("document list table row: {}", tr.getText());
         List<WebElement> tds = tr.findElements(By.xpath(".//td"));
         LOGGER.info("tds: {} for row:{}", tds, i);
         rowsBuilder.add(WebElementUtil.elementsToText(tds));
      }
      return rowsBuilder.build();
   }
}
