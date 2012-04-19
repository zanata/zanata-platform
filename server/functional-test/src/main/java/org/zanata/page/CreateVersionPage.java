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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class CreateVersionPage extends AbstractPage
{
   @FindBy(id = "iterationForm:slugField:slug")
   private WebElement versionIdField;

   @FindBy(name = "iterationForm:statusField:j_id98")
   private WebElement statusSelection;

   @FindBy(id = "iterationForm:save")
   private WebElement saveButton;

   public CreateVersionPage(final WebDriver driver)
   {
      super(driver);
   }

   public CreateVersionPage setVersionId(String versionId)
   {
      versionIdField.sendKeys(versionId);
      return this;
   }

   public CreateVersionPage selectStatus(String status)
   {
      new Select(statusSelection).selectByVisibleText(status);
      return this;
   }

   public ProjectVersionPage save()
   {
      saveButton.click();
      return PageFactory.initElements(getDriver(), ProjectVersionPage.class);
   }
}
