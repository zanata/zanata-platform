package org.zanata.page.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class GooglePermissionsPage extends AbstractPage {
    public GooglePermissionsPage(WebDriver driver) {
        super(driver);
    }

    public EditProfilePage acceptPermissions() {
        getDriver().findElement(By.id("submit_approve_access")).click();
        return new EditProfilePage(getDriver());
    }
}
