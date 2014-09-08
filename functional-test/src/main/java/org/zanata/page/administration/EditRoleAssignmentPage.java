package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class EditRoleAssignmentPage extends BasePage {

    public EditRoleAssignmentPage(WebDriver driver) {
        super(driver);
    }

    public EditRoleAssignmentPage selectPolicy(String policy) {
        log.info("Select policy {}", policy);
        Select policySelect = new Select(getDriver().findElement(
                By.id("projectForm:policyNameField:policyName")));
        policySelect.selectByValue(policy);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage enterIdentityPattern(String pattern) {
        log.info("Enter identity pattern {}", pattern);
        WebElement patternField = getDriver().findElement(
                By.id("projectForm:identityPatternField:identityPattern"));
        patternField.clear();
        patternField.sendKeys(pattern);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage selectRole(String role) {
        log.info("Select role {}", role);
        Select roleSelect = new Select(getDriver().findElement(
                By.id("projectForm:roleField:roles")));
        roleSelect.selectByValue(role);
        return new EditRoleAssignmentPage(getDriver());
    }

    public EditRoleAssignmentPage saveRoleAssignment() {
        log.info("Click Save");
        getDriver().findElement(By.id("projectForm:save")).click();
        return new EditRoleAssignmentPage(getDriver());
    }

    public RoleAssignmentsPage cancelEditRoleAssignment() {
        log.info("Click Cancel");
        getDriver().findElement(By.id("projectForm:cancel")).click();
        return new RoleAssignmentsPage(getDriver());
    }
}
