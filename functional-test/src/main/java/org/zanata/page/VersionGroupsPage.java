package org.zanata.page;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class VersionGroupsPage extends AbstractPage
{
   public static final int GROUP_NAME_COLUMN = 0;

   @FindBy(id = "main_body_content")
   private WebElement mainContentDiv;

   public VersionGroupsPage(WebDriver driver)
   {
      super(driver);
   }

   public List<String> getGroupNames()
   {
      if (mainContentDiv.getText().contains("No group exists"))
      {
         return Collections.emptyList();
      }

      List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(), By.tagName("table"));
      return WebElementUtil.getColumnContents(tableRows, GROUP_NAME_COLUMN);
   }

   public CreateVersionGroupPage createNewGroup()
   {
      WebElement createLink = getDriver().findElement(By.linkText("Create New Group"));
      createLink.click();
      return new CreateVersionGroupPage(getDriver());
   }

   public VersionGroupPage goToGroup(String groupName)
   {
      WebElement groupLink = getDriver().findElement(By.linkText(groupName));
      groupLink.click();
      return new VersionGroupPage(getDriver());
   }
}
