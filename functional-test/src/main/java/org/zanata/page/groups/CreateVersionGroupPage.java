package org.zanata.page.groups;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

import com.google.common.base.Predicate;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class CreateVersionGroupPage extends BasePage {

    public final static String LENGTH_ERROR =
            "value must be shorter than or equal to 100 characters";

    public final static String VALIDATION_ERROR =
            "must start and end with letter or number, and contain only " +
            "letters, numbers, periods, underscores and hyphens.";

    private By groupIdField = By.id("group-form:slugField:slug");
    private By groupNameField = By.id("group-form:nameField:name");
    private By groupDescriptionField = By.id("group-form:descriptionField:description");
    private By saveButton = By.id("group-form:group-create-new");
    private By createNewButton = By.id("group-form:group-create-new");

    public CreateVersionGroupPage(WebDriver driver) {
        super(driver);
    }

    public CreateVersionGroupPage inputGroupId(String groupId) {
        log.info("Enter Group ID {}", groupId);
        waitForWebElement(groupIdField).sendKeys(groupId);
        return new CreateVersionGroupPage(getDriver());
    }

    public String getGroupIdValue() {
        log.info("Query Group ID");
        return waitForWebElement(groupIdField).getAttribute("value");
    }

    public CreateVersionGroupPage inputGroupName(String groupName) {
        log.info("Enter Group name {}", groupName);
        waitForWebElement(groupNameField).sendKeys(groupName);
        return new CreateVersionGroupPage(getDriver());
    }

    public CreateVersionGroupPage inputGroupDescription(String desc) {
        log.info("Enter Group description {}", desc);
        waitForWebElement(groupDescriptionField).sendKeys(desc);
        return this;
    }

    public VersionGroupsPage saveGroup() {
        log.info("Click Save");
        clickAndCheckErrors(waitForWebElement(saveButton));
        return new VersionGroupsPage(getDriver());
    }

    public CreateVersionGroupPage saveGroupFailure() {
        log.info("Click Save");
        waitForWebElement(saveButton).click();
        return new CreateVersionGroupPage(getDriver());
    }

    public CreateVersionGroupPage clearFields() {
        waitForWebElement(groupIdField).clear();
        waitForWebElement(groupNameField).clear();
        waitForWebElement(groupDescriptionField).clear();
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getGroupIdValue().equals("") &&
                        waitForWebElement(groupNameField).getAttribute("value")
                                .equals("");
            }
        });
        return new CreateVersionGroupPage(getDriver());
    }
}
