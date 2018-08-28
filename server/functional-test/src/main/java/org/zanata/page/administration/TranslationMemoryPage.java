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

import org.openqa.selenium.*;
import org.zanata.page.BasePage;
import org.zanata.page.CorePage;

import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class TranslationMemoryPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationMemoryPage.class);

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
    private By uploadButton = By.id("tm-import-button");
    private By listDropDownMenu = By.className("dropdown__toggle");
    private By listImportButton = By.linkText("Import");
    private By listClearButton = By.linkText("Clear");
    private By listDeleteButton = By.linkText("Delete");
    private By deleteConfirmation = By.id("deleteTMModal");
    private By clearConfirmation = By.id("clearTMModal");
    private By uploadNotification = By.id("uploadTMNotification");
    private By okConfirmation = By.id("confirm-ok-button");
    private By cancelConfirmation = By.id("confirm-cancel-button");

    public TranslationMemoryPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Press the Create New button in the dropdown menu
     * @return new TranslationMemoryEditPage
     */
    public TranslationMemoryEditPage clickCreateNew() {
        log.info("Click Create New");
        clickElement(dropDownMenu);
        clickLinkAfterAnimation(createTmLink);
        return new TranslationMemoryEditPage(getDriver());
    }

    /**
     * Press the dropdown menu for a specific TM entry
     * @param tmName of entry to press the menu for
     * @return
     */
    public TranslationMemoryPage clickOptions(String tmName) {
        log.info("Click Options dropdown for {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listDropDownMenu));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Press the Import menu option for a specific TM entry
     * The dropdown option menu should be opened before this action
     * @param tmName of entry to press Import for
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage clickImport(String tmName) {
        log.info("Click Import");
        clickElement(readyElement(findRowByTMName(tmName), listImportButton));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Enter a filename of a TM to import directly into the import dialog
     * @param importFileName of file to import
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage enterImportFileName(String importFileName) {
        log.info("Enter import TM filename {}", importFileName);
        // Don't clear, inject text, do not check value
        enterText(readyElement(filenameInput), importFileName, false, true,
                false);
        slightPause();
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Press the Upload button on the import dialog
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage clickUploadButtonAndAcknowledge() {
        log.info("Click and accept Upload button");
        clickElement(uploadButton);
        WebElement notificationDialog = readyElement(uploadNotification);
        slightPause();
        clickElement(readyElement(notificationDialog, okConfirmation));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Determine if the Upload button is enabled
     * @return boolean button is enabled
     */
    public boolean isImportButtonEnabled() {
        return existingElement(uploadButton).isEnabled();
    }

    /**
     * Press the Clear button, then press the Accept button, for a specific TM
     * @param tmName of TM to clear
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage clickClearTMAndAccept(String tmName) {
        log.info("Click and accept Clear {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listClearButton));
        clickElement(readyElement(existingElement(clearConfirmation), okConfirmation));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Press the Clear button, then press the Cancel button, for a specific TM
     * @param tmName of TM to press clear and cancel for
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage clickClearTMAndCancel(String tmName) {
        log.info("Click and Cancel Clear {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listClearButton));
        clickElement(readyElement(existingElement(clearConfirmation), cancelConfirmation));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Press the Delete button, then press the Accept button, for a specific TM
     * @param tmName of TM to delete
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage clickDeleteTmAndAccept(String tmName) {
        log.info("Click and accept Delete {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listDeleteButton));
        slightPause();
        clickElement(readyElement(existingElement(deleteConfirmation), okConfirmation));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Press the Delete button, then press the Cancel button, for a specific TM
     * @param tmName of TM to press delete and cancel for
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage clickDeleteTmAndCancel(String tmName) {
        log.info("Click and cancel Delete {}", tmName);
        clickElement(readyElement(findRowByTMName(tmName), listDeleteButton));
        clickElement(readyElement(existingElement(deleteConfirmation), cancelConfirmation));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Dismiss the Import Error dialog
     * @return new TranslationMemoryPage
     */
    public TranslationMemoryPage dismissError() {
        log.info("Dismiss error dialog");
        clickElement(readyElement(existingElement(uploadNotification), okConfirmation));
        return new TranslationMemoryPage(getDriver());
    }

    /**
     * Retrieve a list of the TM entries
     * @return String list of TM names
     */
    public List<String> getListedTranslationMemorys() {
        log.info("Query translation memory names");
        List<String> names = new ArrayList<>();
        for (WebElement listElement : getTMList()) {
            names.add(getListEntryName(listElement));
        }
        return names;
    }

    /**
     * Query a specific TM description
     * @param tmName name of TM to query
     * @return description String
     */
    public String getDescription(String tmName) {
        log.info("Query description {}", tmName);
        return getText(existingElement(findRowByTMName(tmName), listItemDescription));
    }

    /**
     * Query a specific TM's number of entries
     * @param tmName name of TM to query
     * @return number of TM entries as String
     */
    public String getNumberOfEntries(String tmName) {
        log.info("Query number of entries {}", tmName);
        waitForPageSilence();
        return getListEntryCount(findRowByTMName(tmName));
    }

    // TODO Remove this when stable
    public void expectNumberOfEntries(int number, String tmName) {
        waitForAMoment().withMessage("Workaround: wait for number of entries")
                .until(it -> Integer.valueOf(getNumberOfEntries(tmName))
                        .equals(number));
    }

    /**
     * Query a TM for the delete button being enabled
     * @param tmName name of TM to query
     * @return boolean delete button is available
     */
    public boolean canDelete(String tmName) {
        log.info("Query can delete {}", tmName);
        String disabled = getAttribute(existingElement(findRowByTMName(tmName),
                listDeleteButton), "disabled");
        return isBlank(disabled) || disabled.equals("false");
    }

    /*
     * Check to see if the TM list is empty
     */
    private boolean noTmsCreated() {
        for (WebElement element : readyElement(tmList)
                .findElements(CorePage.Companion.getParagraph())) {
            if (getText(element).equals(NO_MEMORIES)) {
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
        throw new RuntimeException("Unable to find TM row " + tmName);
    }

    // Get a web element list of all TM entries
    private List<WebElement> getTMList() {
        if (noTmsCreated()) {
            log.info("TM list is empty");
            return new ArrayList<>();
        }
        return readyElement(readyElement(tmList), By.className("list--stats"))
                .findElements(By.className("list__item--actionable"));
    }

    // Get the name substring of a TM entry
    private String getListEntryName(WebElement listElement) {
        String title = getText(existingElement(listElement,
                CorePage.Companion.getH3Header())).trim();
        return title
                .substring(0, title.lastIndexOf(getListEntryCount(listElement)))
                .trim();
    }

    // Get the entry count substring for a TM entry
    private String getListEntryCount(WebElement listElement) {
        return getText(existingElement(
                existingElement(listElement,
                        CorePage.Companion.getH3Header()), listItemCount));
    }
}
