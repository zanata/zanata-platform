package org.zanata.page.administration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ServerConfigurationPage extends BasePage {
    @FindBy(id = "serverConfigForm:urlField")
    private WebElement urlField;

    @FindBy(id = "serverConfigForm:rateLimitField:rateLimitEml")
    private WebElement rateLimitField;

    @FindBy(id = "serverConfigForm:save")
    private WebElement saveButton;

    public ServerConfigurationPage(WebDriver driver) {
        super(driver);
    }

    public ServerConfigurationPage inputRateLimit(int limit) {
        rateLimitField.clear();
        rateLimitField.sendKeys(limit + "");
        return this;
    }

    public String getRateLimit() {
        return rateLimitField.getAttribute("value");
    }

    public AdministrationPage save() {
        saveButton.click();
        return new AdministrationPage(getDriver());
    }
}
