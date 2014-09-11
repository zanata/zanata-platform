package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ServerConfigurationPage extends BasePage {
    @FindBy(id = "serverConfigForm:urlField")
    private WebElement urlField;

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

    public ServerConfigurationPage inputMaxConcurrent(int max) {
        log.info("Enter maximum concurrent API requests {}", max);
        maxConcurrentField.clear();
        maxConcurrentField.sendKeys(max + "");
        return this;
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        log.info("Query maximum concurrent API requests");
        return maxConcurrentField.getAttribute("value");
    }

    public ServerConfigurationPage inputMaxActive(int max) {
        log.info("Enter maximum active API requests {}", max);
        maxActiveField.clear();
        maxActiveField.sendKeys(max + "");
        return this;
    }

    public String getMaxActiveRequestsPerApiKey() {
        log.info("Query maximum active API requests");
        return maxActiveField.getAttribute("value");
    }

    public AdministrationPage save() {
        log.info("Click Save");
        saveButton.click();
        return new AdministrationPage(getDriver());
    }
}
