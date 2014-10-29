package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class EditRoleAssignmentPage extends BasePage {

    private By policySelect = By.id("projectForm:policyNameField:policyName");
    private By patternField = By.id("projectForm:identityPatternField:identityPattern");
    private By roleSelect = By.id("projectForm:roleField:roles");
    private By saveButton = By.id("projectForm:save");
    private By cancelButton = By.id("projectForm:cancel");

    public EditRoleAssignmentPage(WebDriver driver) {
        super(driver);
    }

    public EditRoleAssignmentPage selectPolicy(String policy) {
        log.info("Select policy {}", policy);
        new Select(waitForWebElement(policySelect)).selectByValue(policy);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage enterIdentityPattern(String pattern) {
        log.info("Enter identity pattern {}", pattern);
        waitForWebElement(patternField).clear();
        waitForWebElement(patternField).sendKeys(pattern);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage selectRole(String role) {
        log.info("Select role {}", role);
        new Select(waitForWebElement(roleSelect)).selectByValue(role);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage saveRoleAssignment() {
        log.info("Click Save");
        waitForWebElement(saveButton).click();
        return new EditRoleAssignmentPage(getDriver());
    }

    public RoleAssignmentsPage cancelEditRoleAssignment() {
        log.info("Click Cancel");
        waitForWebElement(cancelButton).click();
        return new RoleAssignmentsPage(getDriver());
    }
}
