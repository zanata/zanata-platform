package org.zanata.page.projectversion.versionsettings;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projectversion.VersionBasePage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class VersionTranslationTab extends VersionBasePage {

    public VersionTranslationTab(WebDriver driver) {
        super(driver);
    }

    private Map validationNames = getValidationMapping();

    public boolean isValidationLevel(String optionName, String level) {
        log.info("Query is {}  validation at level {}", optionName, level);
        String optionElementID = validationNames
                .get(optionName).toString().concat(level);

        return getDriver()
                .findElement(By.id(optionElementID))
                .getAttribute("checked")
                .equals("true");
    }

    public VersionTranslationTab setValidationLevel(String optionName,
                                                    String level) {
        log.info("Click to set {} validation to {}", optionName, level);
        final String optionElementID = validationNames
                .get(optionName).toString().concat(level);

        WebElement option =  getDriver().findElement(
                By.id("settings-translation-validation-form"))
                .findElement(By.id(optionElementID));

        ((JavascriptExecutor) getDriver())
                .executeScript("arguments[0].click();", option);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            // Wait for half a second before continuing
        }
        return new VersionTranslationTab(getDriver());
    }

    private Map<String, String> getValidationMapping() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("HTML/XML tags", "HTML_XML-");
        map.put("Java variables", "JAVA_VARIABLES-");
        map.put("Leading/trailing newline (\\n)", "NEW_LINE-");
        map.put("Positional printf (XSI extension)", "PRINTF_XSI_EXTENSION-");
        map.put("Printf variables", "PRINTF_VARIABLES-");
        map.put("Tab characters (\\t)", "TAB-");
        map.put("XML entity reference", "XML_ENTITY-");
        return map;
    }
}
