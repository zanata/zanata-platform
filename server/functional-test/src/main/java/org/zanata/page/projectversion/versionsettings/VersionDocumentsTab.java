/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.page.projectversion.versionsettings;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projectversion.VersionBasePage;
import org.zanata.page.utility.NamedPredicate;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class VersionDocumentsTab extends VersionBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VersionDocumentsTab.class);
    public static final String UNSUPPORTED_FILETYPE =
            " is not a supported file type.";
    private By uploadButton = By.id("file-upload-component-toggle-button");
    private By startUploadButton = By.id("file-upload-component-start-upload");
    private By cancelUploadButton =
            By.id("file-upload-component-cancel-upload");
    private By fileUploadInput = By.id("file-upload-component-file-input");
    private By fileUploadDone = By.id("file-upload-component-done-upload");
    private By filesListPanel = By.className("js-files-panel");
    private By fileUploadPanel = By.id("file-upload-component");

    public VersionDocumentsTab(WebDriver driver) {
        super(driver);
    }

    public VersionDocumentsTab pressUploadFileButton() {
        log.info("Click Upload file button");
        clickLinkAfterAnimation(readyElement(uploadButton));
        return new VersionDocumentsTab(getDriver());
    }

    /**
     * Query for the status of the upload button in the submit dialog
     *
     * @return boolean can submit file upload
     */
    public boolean canSubmitDocument() {
        log.info("Query can start upload");
        return existingElement(startUploadButton).isEnabled();
    }

    public VersionDocumentsTab cancelUpload() {
        log.info("Click Cancel");
        clickElement(cancelUploadButton);
        waitForAMoment().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver input) {
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
        getExecutor().executeScript(
                "arguments[0].style.visibility = \'visible\'; arguments[0].style.height = \'1px\'; arguments[0].style.width = \'1px\'; arguments[0].style.opacity = 1",
                existingElement(fileUploadInput));
        // Don't clear, inject text, don't check
        enterText(readyElement(fileUploadInput), filePath, false, true, false);
        return new VersionDocumentsTab(getDriver());
    }

    public VersionDocumentsTab submitUpload() {
        log.info("Click Submit upload");
        clickElement(startUploadButton);
        return new VersionDocumentsTab(getDriver());
    }

    public VersionDocumentsTab clickUploadDone() {
        log.info("Click upload Done button");
        clickElement(fileUploadDone);
        return new VersionDocumentsTab(getDriver());
    }

    public boolean sourceDocumentsContains(String document) {
        log.info("Query source documents contain {}", document);
        for (String documentLabel : waitForAMoment()
                .until(new Function<WebDriver, List<String>>() {

                    @Override
                    public List<String> apply(WebDriver input) {
                        List<WebElement> elements =
                                existingElement(By.id("settings-document_form"))
                                        .findElement(By.tagName("ul"))
                                        .findElements(By.xpath(
                                                ".//li/label[@class=\'form__checkbox__label\']"));
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
            filenames.add(
                    element.findElement(By.className("list__title")).getText());
        }
        return filenames;
    }

    public VersionDocumentsTab clickRemoveOn(String filename) {
        log.info("Click remove on {}", filename);
        for (WebElement element : getUploadListElements()) {
            if (element.findElement(By.className("list__title")).getText()
                    .equals(filename)) {
                element.findElement(By.className("list__item__actions"))
                        .findElement(By.className("cancel")).click();
            }
        }
        return new VersionDocumentsTab(getDriver());
    }

    public void expectSomeUploadItems() {
        waitForAMoment().until(new NamedPredicate("expectUploadItem") {

            @Override
            public boolean apply(WebDriver input) {
                return !getUploadListElements().isEmpty();
            }
        });
    }

    private List<WebElement> getUploadListElements() {
        @SuppressWarnings("unchecked")
        List<WebElement> liElements = (List<WebElement>) getExecutor()
                .executeScript("return $(\'div.js-files-panel ul li\')");
        return liElements;
    }

    public String getUploadError() {
        log.info("Query upload error message");
        return readyElement(existingElement(fileUploadPanel),
                By.className("message--danger")).getText();
    }
}
