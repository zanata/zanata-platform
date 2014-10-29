package org.zanata.page.utility;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class HelpPage extends BasePage {

    private By contactAdminLink = By.linkText("Contact Admin");

    public HelpPage(WebDriver driver) {
        super(driver);
    }

    public ContactAdminFormPage clickContactAdmin() {
        log.info("Click Contact Admin button");
        waitForWebElement(contactAdminLink).click();
        return new ContactAdminFormPage(getDriver());
    }
}
