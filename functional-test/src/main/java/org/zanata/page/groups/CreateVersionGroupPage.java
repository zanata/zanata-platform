package org.zanata.page.groups;


import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class CreateVersionGroupPage extends BasePage
{
   @FindBy(id = "projectForm:slugField:slug")
   private WebElement groupSlugField;

   @FindBy(id = "projectForm:nameField:name")
   private WebElement groupNameField;

   @FindBy(id = "projectForm:descriptionField:description")
   private WebElement groupDescriptionField;

   @FindBy(tagName = "Select")
   private WebElement groupStatusSelection;

   @FindBy(id = "projectForm:save")
   private WebElement saveButton;

   @FindBy(className = "errors")
   private WebElement groupSlugFieldError;

   public CreateVersionGroupPage(WebDriver driver)
   {
      super(driver);
      List<String> elements = new ArrayList<String>();
      elements.add("projectForm:slugField:slug");
      elements.add("projectForm:nameField:name");
      elements.add("projectForm:descriptionField:description");
      elements.add("projectForm:save");
      waitForPage(elements);
   }

   public CreateVersionGroupPage inputGroupId(String groupId)
   {
      groupSlugField.sendKeys(groupId);
      return this;
   }

   public CreateVersionGroupPage inputGroupName(String groupName)
   {
      groupNameField.sendKeys(groupName);
      return this;
   }

   public CreateVersionGroupPage inputGroupDescription(String desc)
   {
      groupDescriptionField.sendKeys(desc);
      return this;
   }

   public CreateVersionGroupPage selectStatus(String status)
   {
      new Select(groupStatusSelection).selectByVisibleText(status);
      return this;
   }

   public VersionGroupsPage saveGroup()
   {
      clickAndCheckErrors(saveButton);
      return new VersionGroupsPage(getDriver());
   }

   public CreateVersionGroupPage saveGroupFailure()
   {
      clickAndExpectErrors(saveButton);
      return this;
   }

   public CreateVersionGroupPage clearFields()
   {
      groupSlugField.clear();
      groupNameField.clear();
      groupDescriptionField.clear();
      return new CreateVersionGroupPage(getDriver());
   }
}
