package org.zanata.page.utility;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ContactAdminFormPage extends BasePage {

    private By subjectField = By.id("contactAdminForm:subjectField:subject");
    private By messageField = By.id("contactAdminForm:messageField:message");
    private By sendButton = By.id("contactAdminForm:send");

    public ContactAdminFormPage(WebDriver driver) {
        super(driver);
    }

    public ContactAdminFormPage inputSubject(String subject) {
        log.info("Enter subject {}", subject);
        waitForWebElement(subjectField).clear();
        waitForWebElement(subjectField).sendKeys(subject);
        return new ContactAdminFormPage(getDriver());
    }

    public ContactAdminFormPage inputMessage(String message) {
        log.info("Enter message {}", message);
        WebElementUtil.setRichTextEditorContent(
                getDriver(),
                waitForWebElement(messageField),
                message);
        return new ContactAdminFormPage(getDriver());
    }

    public HelpPage send() {
        log.info("Click Send");
        waitForWebElement(sendButton).click();
        return new HelpPage(getDriver());
    }
}
