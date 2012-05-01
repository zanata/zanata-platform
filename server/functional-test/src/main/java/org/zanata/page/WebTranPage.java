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
