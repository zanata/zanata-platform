package org.zanata.page.projectversion.versionsettings;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projectversion.VersionBasePage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class VersionDocumentsTab extends VersionBasePage {

    public static final String UNSUPPORTED_FILETYPE =
            " is not a supported file type.";

    private By uploadButton = By.id("file-upload-component-toggle-button");
    private By startUploadButton = By.id("file-upload-component-start-upload");
    private By cancelUploadButton = By.id("file-upload-component-cancel-upload");
    private By fileUploadInput = By.id("file-upload-component-file-input");
    private By fileUploadDone = By.id("file-upload-component-done-upload");
    private By filesListPanel = By.className("js-files-panel");
    private By fileUploadPanel = By.id("file-upload-component");

    public VersionDocumentsTab(WebDriver driver) {
        super(driver);
    }

    public VersionDocumentsTab pressUploadFileButton() {
        log.info("Click Upload file button");
        clickLinkAfterAnimation(waitForWebElement(uploadButton));
        return new VersionDocumentsTab(getDriver());
    }

    /**
     * Query for the status of the upload button in the submit dialog
     *
     * @return boolean can submit file upload
     */
    public boolean canSubmitDocument() {
        log.info("Query can start upload");
        return waitForElementExists(startUploadButton).isEnabled();
    }

    public VersionDocumentsTab cancelUpload() {
        log.info("Click Cancel");
        waitForWebElement(cancelUploadButton).click();
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply( WebDriver input) {
                return !getDriver().findElement(By.id("file-upload-component"))
                        .isDisplayed();
            }
        });
        slightPause();
        waitForPageSilence();
        return new VersionDocumentsTab(getDriver());
    }

    public VersionDocumentsTab enterFilePath(String filePath) {
        log.info("Enter file path {}", filePath);
        // Make the hidden input element slightly not hidden
        ((JavascriptExecutor)getDriver())
                .executeScript("arguments[0].style.visibility = 'visible'; " +
                        "arguments[0].style.height = '1px'; " +
                        "arguments[0].style.width = '1px'; " +
                        "arguments[0].style.opacity = 1",
                        waitForElementExists(fileUploadInput));

        waitForWebElement(fileUploadInput).sendKeys(filePath);
        return new VersionDocumentsTab(getDriver());
    }

    public VersionDocumentsTab submitUpload() {
        log.info("Click Submit upload");
        waitForWebElement(startUploadButton).click();
        return new VersionDocumentsTab(getDriver());
    }

    public VersionDocumentsTab clickUploadDone() {
        log.info("Click upload Done button");
        waitForWebElement(fileUploadDone).click();
        return new VersionDocumentsTab(getDriver());
    }

    public boolean sourceDocumentsContains(String document) {
        log.info("Query source documents contain {}", document);
        for(String documentLabel : waitForAMoment()
                .until(new Function<WebDriver, List<String>>() {
                    @Override
                    public List<String> apply(WebDriver input) {
                        List<WebElement> elements = waitForElementExists(
                                By.id("settings-document_form"))
                                .findElement(By.tagName("ul"))
                                .findElements(By.xpath(".//li/label[@class='" +
                                        "form__checkbox__label']"));
                        List<String> namesList = new ArrayList<String>();
                        for (WebElement element : elements) {
                            namesList.add(element.getText());
                        }
                        return namesList;
                    }
                })) {
            if (documentLabel.contains(document)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getUploadList() {
        log.info("Query upload list");
        List<String> filenames = new ArrayList<String>();
        for (WebElement element : getUploadListElements()) {
            filenames.add(element.findElement(By.className("list__title"))
                    .getText());
        }
        return filenames;
    }

    public VersionDocumentsTab clickRemoveOn(String filename) {
        log.info("Click remove on {}", filename);
        for (WebElement element : getUploadListElements()) {
            if (element.findElement(By.className("list__title"))
                    .getText().equals(filename)) {
                element.findElement(By.className("list__item__actions"))
                        .findElement(By.className("cancel"))
                        .click();
            }
        }
        return new VersionDocumentsTab(getDriver());
    }

    private List<WebElement> getUploadListElements() {
        return waitForWebElement(filesListPanel).findElement(By.tagName("ul"))
                .findElements(By.tagName("li"));
    }

    public String getUploadError() {
        log.info("Query upload error message");
        return waitForWebElement(waitForElementExists(fileUploadPanel),
                By.className("message--danger")).getText();
    }
}
