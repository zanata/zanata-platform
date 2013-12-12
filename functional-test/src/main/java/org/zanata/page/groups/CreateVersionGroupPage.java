package org.zanata.page.groups;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class CreateVersionGroupPage extends BasePage {

    @FindBy(id = "group-form:descriptionField:description")
    private WebElement groupDescriptionField;

    @FindBy(id = "group-form:group-create-new")
    private WebElement saveButton;

    public CreateVersionGroupPage(WebDriver driver) {
        super(driver);
        List<By> elementBys =
                ImmutableList.<By> builder()
                        .add(By.id("group-form:slugField:slug"))
                        .add(By.id("group-form:nameField:name"))
                        .add(By.id("group-form:descriptionField:description"))
                        .add(By.id("group-form:group-create-new")).build();
        waitForPage(elementBys);
    }

    public CreateVersionGroupPage inputGroupId(String groupId) {
        getGroupSlugField().sendKeys(groupId);
        return new CreateVersionGroupPage(getDriver());
    }

    private WebElement getGroupSlugField() {
        return getDriver().findElement(By.id("group-form:slugField:slug"));
    }

    public String getGroupIdValue() {
        return getGroupSlugField().getAttribute("value");
    }

    public CreateVersionGroupPage inputGroupName(String groupName) {
        getGroupNameField().sendKeys(groupName);
        return new CreateVersionGroupPage(getDriver());
    }

    private WebElement getGroupNameField() {
        return getDriver().findElement(By.id("group-form:nameField:name"));
    }

    public CreateVersionGroupPage inputGroupDescription(String desc) {
        groupDescriptionField.sendKeys(desc);
        return this;
    }

    public VersionGroupsPage saveGroup() {
        clickAndCheckErrors(saveButton);
        return new VersionGroupsPage(getDriver());
    }

    public CreateVersionGroupPage saveGroupFailure() {
        clickAndCheckErrors(saveButton);
        return new CreateVersionGroupPage(getDriver());
    }

    public CreateVersionGroupPage clearFields() {
        getGroupSlugField().clear();
        getGroupNameField().clear();
        groupDescriptionField.clear();
        return new CreateVersionGroupPage(getDriver());
    }

    public List<String> getFieldValidationErrors() {
        List<WebElement> elements =
                getDriver().findElements(By.className("message--danger"));
        List<String> errors = new ArrayList<String>();
        for (WebElement element : elements) {
            errors.add(element.getText());
        }
        return errors;
    }
}
