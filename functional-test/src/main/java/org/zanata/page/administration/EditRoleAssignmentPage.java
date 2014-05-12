package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EditRoleAssignmentPage extends BasePage {

    public EditRoleAssignmentPage(WebDriver driver) {
        super(driver);
    }

    public EditRoleAssignmentPage selectPolicy(String policy) {
        Select policySelect = new Select(getDriver().findElement(
                By.id("projectForm:policyNameField:policyName")));
        policySelect.selectByValue(policy);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage enterIdentityPattern(String pattern) {
        WebElement patternField = getDriver().findElement(
                By.id("projectForm:identityPatternField:identityPattern"));
        patternField.clear();
        patternField.sendKeys(pattern);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage selectRole(String role) {
        Select roleSelect = new Select(getDriver().findElement(
                By.id("projectForm:roleField:roles")));
        roleSelect.selectByValue(role);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage saveRoleAssignment() {
        getDriver().findElement(By.id("projectForm:save")).click();
        return new EditRoleAssignmentPage(getDriver());
    }

    public RoleAssignmentsPage cancelEditRoleAssignment() {
        getDriver().findElement(By.id("projectForm:cancel")).click();
        return new RoleAssignmentsPage(getDriver());
    }
}
