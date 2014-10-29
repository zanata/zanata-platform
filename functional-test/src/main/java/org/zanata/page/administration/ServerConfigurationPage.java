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

    private By urlField = By.id("serverConfigForm:urlField");
    private By maxConcurrentField = By.id("serverConfigForm:maxConcurrentPerApiKeyField:maxConcurrentPerApiKeyEml");
    private By maxActiveField = By.id("serverConfigForm:maxActiveRequestsPerApiKeyField:maxActiveRequestsPerApiKeyEml");
    private By saveButton = By.id("serverConfigForm:save");

    public ServerConfigurationPage(WebDriver driver) {
        super(driver);
    }

    public ServerConfigurationPage inputMaxConcurrent(int max) {
        log.info("Enter maximum concurrent API requests {}", max);
        waitForWebElement(maxConcurrentField).clear();
        waitForWebElement(maxConcurrentField).sendKeys(max + "");
        return new ServerConfigurationPage(getDriver());
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        log.info("Query maximum concurrent API requests");
        return waitForWebElement(maxConcurrentField).getAttribute("value");
    }

    public ServerConfigurationPage inputMaxActive(int max) {
        log.info("Enter maximum active API requests {}", max);
        waitForWebElement(maxActiveField).clear();
        waitForWebElement(maxActiveField).sendKeys(max + "");
        return new ServerConfigurationPage(getDriver());
    }

    public String getMaxActiveRequestsPerApiKey() {
        log.info("Query maximum active API requests");
        return waitForWebElement(maxActiveField).getAttribute("value");
    }

    public AdministrationPage save() {
        log.info("Click Save");
        waitForWebElement(saveButton).click();
        return new AdministrationPage(getDriver());
    }
}
