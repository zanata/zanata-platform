package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ServerConfigurationPage extends BasePage {
    @FindBy(id = "serverConfigForm:urlField")
    private WebElement urlField;

    @FindBy(id = "serverConfigForm:rateLimitField:rateLimitEml")
    private WebElement rateLimitField;

    @FindBy(
            id = "serverConfigForm:maxConcurrentPerApiKeyField:maxConcurrentPerApiKeyEml")
    private WebElement maxConcurrentField;

    @FindBy(
            id = "serverConfigForm:maxActiveRequestsPerApiKeyField:maxActiveRequestsPerApiKeyEml")
    private WebElement maxActiveField;

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

    public ServerConfigurationPage inputMaxConcurrent(int max) {
        maxConcurrentField.clear();
        maxConcurrentField.sendKeys(max + "");
        return this;
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        return maxConcurrentField.getAttribute("value");
    }

    public ServerConfigurationPage inputMaxActive(int max) {
        maxActiveField.clear();
        maxActiveField.sendKeys(max + "");
        return this;
    }

    public String getMaxActiveRequestsPerApiKey() {
        return maxActiveField.getAttribute("value");
    }

    public AdministrationPage save() {
        saveButton.click();
        return new AdministrationPage(getDriver());
    }

    public ServerConfigurationPage turnRateLimitingOn(boolean onOrOff) {
        if (onOrOff) {
            getDriver().findElement(
                    By.id("serverConfigForm:rateLimitSwitchField:radio:0"))
                    .click();
        } else {
            getDriver().findElement(
                    By.id("serverConfigForm:rateLimitSwitchField:radio:1"))
                    .click();
        }
        return new ServerConfigurationPage(getDriver());
    }
}
