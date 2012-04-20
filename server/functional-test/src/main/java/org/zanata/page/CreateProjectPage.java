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

public class CreateProjectPage extends AbstractPage
{
   @FindBy(id = "projectForm:slugField:slug")
   private WebElement projectIdField;

   @FindBy(id = "projectForm:nameField:name")
   private WebElement projectNameField;

   @FindBy(id = "projectForm:descriptionField:description")
   private WebElement descriptionField;

   @FindBy(id = "projectForm:homeContentField:homeContentTextArea")
   private WebElement homeContentTextArea;

   @FindBy(name = "projectForm:statusField:j_id130")
   private WebElement statusSelection;

   @FindBy(id = "projectForm:save")
   private WebElement saveButton;

   public CreateProjectPage(final WebDriver driver)
   {
      super(driver);
   }

   public CreateProjectPage inputProjectId(String projectId)
   {
      projectIdField.sendKeys(projectId);
      return this;
   }

   public CreateProjectPage inputProjectName(String projectName)
   {
      projectNameField.sendKeys(projectName);
      return this;
   }

   public CreateProjectPage selectStatus(String status)
   {
      new Select(statusSelection).selectByVisibleText(status);
      return this;
   }

   public ProjectPage saveProject()
   {
      saveButton.click();
      return new ProjectPage(getDriver());
   }
}
