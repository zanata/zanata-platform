/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.zanata.page.BasePage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TranslationMemoryPage extends BasePage {

    public static final String ID_UNAVAILABLE = "This Id is not available";
    public static final String UPLOAD_ERROR =
            "There was an error uploading the file";
    public static final String NO_MEMORIES =
            "No Translation Memories have been created.";

    private By listItemCount = By.className("badge");
    private By listItemDescription = By.className("list__item__meta");
    private By dropDownMenu = By.id("moreActions");
    private By createTmLink = By.id("createTmLink");
    private By tmList = By.id("tmList");
    private By filenameInput = By.name("uploadedFile");
    private By uploadButton = By.name("uploadBtn");
    private By listDropDownMenu = By.className("dropdown__toggle");
    private By listImportButton = By.linkText("Import");
    private By listExportButton = By.linkText("Export");
    private By listClearButton = By.linkText("Clear");
    private By listDeleteButton = By.linkText("Delete");

    public TranslationMemoryPage(WebDriver driver) {
        super(driver);
    }

    public TranslationMemoryEditPage clickCreateNew() {
        log.info("Click Create New");
        clickElement(dropDownMenu);
        clickLinkAfterAnimation(createTmLink);
        return new TranslationMemoryEditPage(getDriver());
    }

    public TranslationMemoryPage clickOptions(String tmName) {
        log.info("Click Options dropdown for {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listDropDownMenu));
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickImport(String tmName) {
        log.info("Click Import");
        clickElement(readyElement(findRowByTMName(tmName), listImportButton));
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage enterImportFileName(String importFileName) {
        log.info("Enter import TM filename {}", importFileName);
        readyElement(filenameInput).sendKeys(importFileName);
        slightPause();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickUploadButtonAndAcknowledge() {
        log.info("Click and accept Upload button");
        clickElement(uploadButton);
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public Alert expectFailedUpload() {
        log.info("Click Upload");
        clickElement(uploadButton);
        return switchToAlert();
    }

    public TranslationMemoryPage clickClearTMAndAccept(String tmName) {
        log.info("Click and accept Clear {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listClearButton));
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickClearTMAndCancel(String tmName) {
        log.info("Click and Cancel Clear {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listClearButton));
        switchToAlert().dismiss();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickDeleteTmAndAccept(String tmName) {
        log.info("Click and accept Delete {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listDeleteButton));
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickDeleteTmAndCancel(String tmName) {
        log.info("Click and cancel Delete {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listDeleteButton));
        switchToAlert().dismiss();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage dismissError() {
        log.info("Dismiss error dialog");
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public List<String> getListedTranslationMemorys() {
        log.info("Query translation memory names");
        List<String> names = new ArrayList<>();
        for (WebElement listElement : getTMList()) {
            names.add(getListEntryName(listElement));
        }
        return names;
    }

    public String getDescription(String tmName) {
        log.info("Query description {}", tmName);
        return getListEntryDescription(findRowByTMName(tmName));
    }

    public String getNumberOfEntries(String tmName) {
        log.info("Query number of entries {}", tmName);
        waitForPageSilence();
        return getListEntryCount(findRowByTMName(tmName));
    }

    public boolean canDelete(String tmName) {
        log.info("Query can delete {}", tmName);
        String disabled = readyElement(
                findRowByTMName(tmName), listDeleteButton)
                .getAttribute("disabled");

        return null == disabled || disabled.equals("false");
    }

    /*
     * Check to see if the TM list is empty
     */
    private boolean noTmsCreated() {
        for (WebElement element : readyElement(tmList)
                .findElements(By.tagName("p"))) {
            if (element.getText().equals(NO_MEMORIES)) {
                return true;
            }
        }
        return false;
    }
    /*
     * Get a row from the TM table that corresponds with tmName
     */
    private WebElement findRowByTMName(final String tmName) {
        for (WebElement listElement : getTMList()) {
            if (getListEntryName(listElement).equals(tmName)) {
                return listElement;
            }
        }
        return null;
    }

    private List<WebElement> getTMList() {
        if (noTmsCreated()) {
            log.info("TM list is empty");
            return new ArrayList<>();
        }
        return readyElement(readyElement(tmList),
                By.className("list--stats"))
                .findElements(By.className("list__item--actionable"));
    }

    private String getListEntryName(WebElement listElement) {
        String title = listElement.findElement(By.tagName("h3")).getText().trim();
        return title.substring(0, title.lastIndexOf(getListEntryCount(listElement))).trim();
    }

    private String getListEntryDescription(WebElement listElement) {
        return readyElement(listElement, listItemDescription).getText();
    }

    private String getListEntryCount(WebElement listElement) {
        return listElement.findElement(By.tagName("h3"))
                .findElement(listItemCount).getText();
    }
}
