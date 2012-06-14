package org.zanata.page.groups;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.AbstractPage;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CreateVersionGroupPage extends AbstractPage
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

   public CreateVersionGroupPage(WebDriver driver)
   {
      super(driver);
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
      new Select(groupDescriptionField).selectByVisibleText(status);
      return this;
   }

   public VersionGroupsPage saveGroup()
   {
      clickSaveAndCheckErrors(saveButton);
      return new VersionGroupsPage(getDriver());
   }

}
