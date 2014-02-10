package org.zanata.page.utility;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HelpPage extends BasePage {
    @FindBy(linkText = "Contact Admin")
    private WebElement contactAdminLink;

    public HelpPage(WebDriver driver) {
        super(driver);
    }

    public ContactAdminFormPage clickContactAdmin() {
        contactAdminLink.click();
        return new ContactAdminFormPage(getDriver());
    }
}
