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
public class ProjectSourceDocumentsPage extends BasePage
{
   @FindBy(id = "iterationDocumentsForm:data_table:tb")
   private WebElement documentTableTBody;

   @FindBy(id = "uploadSidebar:uploadDocumentButton")
   private WebElement uploadDocumentButton;

   @FindBy(id = "uploadDocForm:uploadFilename")
   private WebElement uploadFilenameInput;

   @FindBy(id = "uploadDocForm:uploadButton")
   private WebElement uploadButton;

   @FindBy(id = "uploadDocForm:cancelUploadButton")
   private WebElement cancelUploadButton;

   public ProjectSourceDocumentsPage(final WebDriver driver)
   {
      super(driver);
   }

   public ProjectSourceDocumentsPage pressUploadFileButton()
   {
      uploadDocumentButton.click();
      return new ProjectSourceDocumentsPage(getDriver());
   }

   public ProjectSourceDocumentsPage enterFilePath(String filePath)
   {
      uploadFilenameInput.sendKeys(filePath);
      return new ProjectSourceDocumentsPage(getDriver());
   }

   public ProjectSourceDocumentsPage cancelUpload()
   {
      cancelUploadButton.click();
      return new ProjectSourceDocumentsPage(getDriver());
   }

   public ProjectSourceDocumentsPage submitUpload()
   {
      uploadButton.click();
      return new ProjectSourceDocumentsPage(getDriver());
   }

   private List<WebElement> getDocumentTableRows()
   {
      return documentTableTBody.findElements(By.tagName("tr"));
   }


   public boolean sourceDocumentsContains(String document)
   {
      List<WebElement> documentTableRows = getDocumentTableRows();
      for (WebElement tableRow : documentTableRows)
      {
         if (tableRow.findElements(By.tagName("td")).get(1).getText().equals(document))
         {
            return true;
         }
      }
      return false;
   }

}
